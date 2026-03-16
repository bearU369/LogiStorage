/**
 * Copyright (C) 2026 @bear_369
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.bear369.logistorage.shared.command;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;

public class MEItemCommand {

    @LuaFunction
    public static MethodResult listItems(
            IComputerAccess computer, ILuaContext context, IArguments argument, IActionHost host) throws LuaException {
        if (!MENetworkCommand.validateNode(host.getActionableNode())) throw MENetworkCommand.NODE_NOT_FOUND;

        final List<Hashtable<String, Object>> itemList = new ArrayList<>();
        MENetworkCommand.iterateAEKey(host.getActionableNode(), (stack, key) -> {
            if (key instanceof AEItemKey convertedKey) itemList.add(convertKeyToData(stack, convertedKey));
        });
        return MethodResult.of(itemList);
    }

    @LuaFunction
    public static MethodResult findItem(
            IComputerAccess computer, ILuaContext context, IArguments argument, IActionHost host) throws LuaException {
        final IGridNode gridNode = host.getActionableNode();
        if (!MENetworkCommand.validateNode(gridNode)) throw MENetworkCommand.NODE_NOT_FOUND;
        final KeyCounter gridStack = gridNode.getGrid().getStorageService().getCachedInventory();
        final String itemIDString = argument.getString(0);

        if (itemIDString.isEmpty()) throw new LuaException("bad argument #1 for 'findItem'. (String cannot be empty)");

        final Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemIDString));
        if (item == null || item == Items.AIR) throw new LuaException("invalid ItemID");

        final AEItemKey itemKey = AEItemKey.of(item);
        final Collection<Entry<AEKey>> searchedItems = gridStack.findFuzzy(itemKey, FuzzyMode.IGNORE_ALL);
        final List<Hashtable<String, Object>> itemList = new ArrayList<>();

        for (Entry<AEKey> key : searchedItems) {
            if (key.getKey() instanceof AEItemKey convertedKey) itemList.add(convertKeyToData(gridStack, convertedKey));
        }
        return MethodResult.of(itemList);
    }

    @LuaFunction(mainThread = true)
    public static MethodResult pushItem(
            IComputerAccess computer,
            ILuaContext context,
            IArguments argument,
            IActionHost host,
            IPeripheral triggerPeripheral)
            throws LuaException {
        final IGridNode gridNode = host.getActionableNode();
        if (!MENetworkCommand.validateNode(gridNode)) throw MENetworkCommand.NODE_NOT_FOUND;

        final String targetPeripheralName = argument.getString(0);
        final String targetItemName = argument.getString(1);
        final int targetSlot = argument.optInt(2, 1) - 1; // Lua-aligned indexing

        if (targetPeripheralName.isEmpty()) throw new LuaException("target name cannot be empty");

        // === INITIALIZE TARGET PERIPHERIAL AND ITEM HANDLER ===//
        final IPeripheral targetPeripheral = MENetworkCommand.getPeripheralByName(computer, targetPeripheralName);
        if (targetPeripheral == triggerPeripheral) throw MENetworkCommand.SELF_TARGET_PERIPHERAL;

        final Object targetObject = targetPeripheral.getTarget();
        if (targetObject == null) throw new LuaException("target peripherial does not have target");

        final IItemHandler handler = MEItemCommand.getHandlerWrapper(targetObject);

        // === INITIALIZE TARGET ITEM ===//
        if (targetItemName.isEmpty()) throw new LuaException("target itemID cannot be empty");

        final Item targetItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(targetItemName));
        if (targetItem == null || targetItem == Items.AIR) throw new LuaException("invalid itemID");

        final MEStorage gridStorage = gridNode.getGrid().getStorageService().getInventory();
        final KeyCounter gridStack = gridNode.getGrid().getStorageService().getCachedInventory();

        final AEItemKey itemKey = AEItemKey.of(targetItem);
        final Collection<Entry<AEKey>> searchedItems = gridStack.findFuzzy(itemKey, FuzzyMode.IGNORE_ALL);
        if (searchedItems.size() == 0) return MethodResult.of(0);

        // === PUSH ITEM FROM AE2 to ITEM HANDLER ===//
        if (targetSlot < 0 || targetSlot >= handler.getSlots()) throw new LuaException("target slot is out of range");

        final int pushCount = argument.optInt(3, handler.getSlotLimit(targetSlot));
        long tmpPushCount = pushCount;

        for (Entry<AEKey> entry : searchedItems) {
            if (tmpPushCount <= 0) break;

            AEKey parsedEntry = entry.getKey();
            if (parsedEntry instanceof AEItemKey convertedKey) {
                long count = gridStorage.extract(
                        convertedKey, tmpPushCount, Actionable.MODULATE, IActionSource.ofMachine(host));
                tmpPushCount -= count;

                ItemStack remainingStack = handler.insertItem(targetSlot, convertedKey.toStack((int) count), false);
                if (!remainingStack.isEmpty()) {
                    tmpPushCount += remainingStack.getCount();
                    gridStorage.insert(
                            AEItemKey.of(remainingStack),
                            remainingStack.getCount(),
                            Actionable.MODULATE,
                            IActionSource.empty());
                }
            }
        }

        return MethodResult.of(Math.abs(tmpPushCount - pushCount));
    }

    @LuaFunction(mainThread = true)
    public static MethodResult pullItem(
            IComputerAccess computer,
            ILuaContext context,
            IArguments argument,
            IActionHost host,
            IPeripheral triggerPeripheral)
            throws LuaException {
        final IGridNode gridNode = host.getActionableNode();
        if (!MENetworkCommand.validateNode(gridNode)) throw MENetworkCommand.NODE_NOT_FOUND;
        final MEStorage gridStorage = gridNode.getGrid().getStorageService().getInventory();

        final String targetPeripherialName = argument.getString(0);
        final int targetSlot = argument.optInt(1, 1) - 1; // Lua-aligned indexing
        final int itemCount = argument.optInt(2, 64);

        // === DEFINING TARGET PERIPHERIAL ===//
        final IPeripheral targetPeripheral = MENetworkCommand.getPeripheralByName(computer, targetPeripherialName);
        if (targetPeripheral == triggerPeripheral) throw MENetworkCommand.SELF_TARGET_PERIPHERAL;

        final Object targetObject = targetPeripheral.getTarget();
        if (targetObject == null) throw new LuaException("target object is nil");

        final IItemHandler handler = getHandlerWrapper(targetObject);

        // === PULLING FROM HANDLER TO AE2 STORAGE ===//
        if (targetSlot < 0 || targetSlot >= handler.getSlots()) throw new LuaException("target slot is out of range");

        final ItemStack extractedStack = handler.extractItem(targetSlot, itemCount, false);
        final int extractedCount = extractedStack.getCount();
        if (extractedStack.isEmpty()) return MethodResult.of(0);

        final AEItemKey convertedStack = AEItemKey.of(extractedStack);
        final long insertedCount = gridStorage.insert(
                convertedStack, extractedStack.getCount(), Actionable.MODULATE, IActionSource.ofMachine(host));

        final int remainingCount = extractedCount - (int) insertedCount;
        if (remainingCount > 0) {
            extractedStack.setCount(remainingCount);
            handler.insertItem(targetSlot, extractedStack, false);
        }

        return MethodResult.of(insertedCount);
    }

    private static Hashtable<String, Object> convertKeyToData(KeyCounter service, AEItemKey key) {
        final Hashtable<String, Object> itemProperties = new Hashtable<>();

        final String itemID = key.getId().toString();
        final String itemName = key.getDisplayName().getString();
        final long itemCount = service.get(key);
        final CompoundTag itemTag = key.getTag();

        itemProperties.put("itemID", itemID);
        itemProperties.put("itemName", itemName);
        itemProperties.put("itemCount", itemCount);

        if (itemTag != null) {
            Hashtable<String, Object> metaTable = new Hashtable<>();

            for (String tagName : itemTag.getAllKeys()) {
                if (tagName.isEmpty()) continue;
                final Tag curTag = itemTag.get(tagName);
                if (curTag == null) continue;
                metaTable.put(tagName, unwrapTag(curTag));
            }

            itemProperties.put("metadata", metaTable);
        }

        return itemProperties;
    }

    private static Object unwrapTag(@Nonnull Tag t) {
        final byte tagType = t.getId();
        switch (tagType) {
            // === ALL NUMERICS ===//
            case Tag.TAG_BYTE:
                return ((ByteTag) t).getAsByte();
            case Tag.TAG_SHORT:
                return ((ShortTag) t).getAsShort();
            case Tag.TAG_INT:
                return ((IntTag) t).getAsInt();
            case Tag.TAG_LONG:
                return ((LongTag) t).getAsLong();
            case Tag.TAG_FLOAT:
                return ((FloatTag) t).getAsFloat();
            case Tag.TAG_DOUBLE:
                return ((DoubleTag) t).getAsDouble();
            // === ARRAY TAGS ===//
            case Tag.TAG_BYTE_ARRAY:
                return ((ByteArrayTag) t).getAsByteArray();
            case Tag.TAG_INT_ARRAY:
                return ((IntArrayTag) t).getAsIntArray();
            case Tag.TAG_LONG_ARRAY:
                return ((LongArrayTag) t).getAsLongArray();
            // === LIST TAGS ===//
            case Tag.TAG_LIST:
                final ListTag list = (ListTag) t;
                final List<Object> convertedList = new ArrayList<>();
                for (Tag t2 : list) {
                    if (t2 == null) continue;
                    Object val = unwrapTag(t2);
                    if (val != null) convertedList.add(val);
                }
                return convertedList;
            case Tag.TAG_COMPOUND:
                final CompoundTag compound = (CompoundTag) t;
                final Hashtable<String, Object> compoundTable = new Hashtable<>();
                for (String keyName : compound.getAllKeys()) {
                    if (keyName.isEmpty()) continue;
                    Tag curTag = compound.get(keyName);
                    if (curTag == null) continue;
                    Object val = unwrapTag(curTag);
                    if (val != null) compoundTable.put(keyName, val);
                }
                return compoundTable;
            // === MISC ===//
            case Tag.TAG_STRING:
                return ((StringTag) t).getAsString();
            case Tag.TAG_END:
                return null;
        }
        return null;
    }

    private static IItemHandler getHandlerWrapper(@Nonnull Object object) throws LuaException {
        IItemHandler handler;
        try {
            handler = getHandler(object);
        } catch (NullPointerException exception) {
            throw new LuaException("target object is not an item handler");
        }

        if (handler == null) throw new LuaException("target object does not have an inventory");
        return handler;
    }

    private static @Nullable IItemHandler getHandler(@Nonnull Object object) {
        if (object instanceof ICapabilityProvider provider) {
            LazyOptional<IItemHandler> invCap = provider.getCapability(ForgeCapabilities.ITEM_HANDLER);
            return invCap.orElseThrow(NullPointerException::new);
        }

        if (object instanceof Container container) return new InvWrapper(container);
        return null;
    }
}

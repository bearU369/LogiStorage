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
package com.bear369.logistorage.shared.ae2.services;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartItem;
import appeng.api.stacks.GenericStack;
import appeng.api.util.DimensionalBlockPos;
import appeng.helpers.InterfaceLogicHost;
import appeng.parts.AEBasePart;
import com.bear369.logistorage.core.Main;
import com.bear369.logistorage.shared.ae2.ICraftingProcessListener;
import com.bear369.logistorage.shared.ae2.IInterfaceLogic;
import com.bear369.logistorage.shared.ae2.util.CraftingSignalEvent;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

public class CraftingEventService implements IGridService, IGridServiceProvider {

    private final Set<ICraftingProcessListener> craftingListeners = new HashSet<>();

    public static CraftingEventService getService(@Nonnull IGridNode node) {
        Objects.requireNonNull(node);
        return node.getGrid().getService(CraftingEventService.class);
    }

    public static String getTriggeringNameFromSource(@Nonnull IActionSource src) {
        Objects.requireNonNull(src);
        if (src.machine().isPresent()) {
            return CraftingEventService.getTriggeringNameFromMachine(
                    src.machine().orElseThrow());
        } else if (src.player().isPresent()) {
            final Player player = src.player().orElseThrow();
            return player.getName().getString();
        }
        Main.LOGGER.warn("Unidentified source found: {}", src.getClass().toString());
        return src.getClass().toString();
    }

    private static String getTriggeringNameFromMachine(IActionHost machine) {
        if (machine instanceof AEBasePart basePart) { // ME Exporter, ME Level Emitter, etc.
            ResourceLocation key = IPartItem.getId(basePart.getPartItem());
            if (key != null) {
                return key.toString();
            } else {
                Main.LOGGER.info(
                        "WARNING: Failed to find IPartItem of {}, resort to using its block entity.",
                        basePart.getClass().toString());
                final Item item =
                        basePart.getBlockEntity().getBlockState().getBlock().asItem();
                return ForgeRegistries.ITEMS.getKey(item).toString();
            }
        }
        if (machine instanceof IInterfaceLogic interfaceLogic) { // ME Interfaces - Block/Cable
            final InterfaceLogicHost host = interfaceLogic.getHost();
            if (host instanceof AEBasePart basePart) { // ME Interface - Cable
                ResourceLocation key = IPartItem.getId(basePart.getPartItem());
                if (key != null) return key.toString();
            } else { // ME Interfaces - Block
                final DimensionalBlockPos dPos = interfaceLogic.getLocation();
                final BlockEntity blockEntity =
                        Objects.requireNonNull(dPos.getLevel().getBlockEntity(dPos.getPos()));
                final Item item = blockEntity.getBlockState().getBlock().asItem();
                return ForgeRegistries.ITEMS.getKey(item).toString();
            }
        }
        if (machine instanceof ItemMenuHost host) { // ME Wireless Terminal
            final Item item = host.getItemStack().getItem();
            return ForgeRegistries.ITEMS.getKey(item).toString();
        }
        if (machine instanceof BlockEntity blockEntity) { // Others
            final Item item = blockEntity.getBlockState().getBlock().asItem();
            return ForgeRegistries.ITEMS.getKey(item).toString();
        }
        Main.LOGGER.warn("Unidentified machine found: {}", machine.getClass().toString());
        return machine.getClass().toString();
    }

    public CraftingEventService(IGrid grid) {}

    public void invokeCraftingEvent(GenericStack stack, String src, CraftingSignalEvent event) {
        Objects.nonNull(src);
        for (ICraftingProcessListener listener : craftingListeners) {
            switch (event) {
                case CRAFTING_START:
                    listener.onCraftingBegin(stack, src);
                    break;
                case CRAFTING_COMPLETE:
                    listener.onCraftingFinish(stack, src);
                    break;
                case CRAFTING_CANCEL:
                    listener.onCraftingCancel(stack, src);
                    break;
            }
        }
    }

    public void addNode(IGridNode gridNode, @Nullable CompoundTag savedData) {
        var craftingListener = gridNode.getService(ICraftingProcessListener.class);
        if (craftingListener != null) {
            craftingListeners.add(craftingListener);
        }
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        var craftingListener = gridNode.getService(ICraftingProcessListener.class);
        if (craftingListener != null) {
            craftingListeners.remove(craftingListener);
        }
    }
}

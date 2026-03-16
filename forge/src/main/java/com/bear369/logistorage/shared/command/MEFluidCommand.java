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
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.registries.ForgeRegistries;

public class MEFluidCommand {

    @LuaFunction
    public static MethodResult listFluids(
            IComputerAccess computer, ILuaContext context, IArguments argument, IActionHost host) throws LuaException {
        if (!MENetworkCommand.validateNode(host.getActionableNode())) throw MENetworkCommand.NODE_NOT_FOUND;

        final List<Hashtable<String, Object>> fluidList = new ArrayList<>();
        MENetworkCommand.iterateAEKey(host.getActionableNode(), (counter, key) -> {
            if (key instanceof AEFluidKey convertedKey) fluidList.add(convertKeyToData(counter, convertedKey));
        });

        return MethodResult.of(fluidList);
    }

    @LuaFunction
    public static MethodResult findFluid(
            IComputerAccess computer, ILuaContext context, IArguments arguments, IActionHost host) throws LuaException {
        if (!MENetworkCommand.validateNode(host.getActionableNode())) throw MENetworkCommand.NODE_NOT_FOUND;

        final KeyCounter gridCounter =
                host.getActionableNode().getGrid().getStorageService().getCachedInventory();
        final String targetIDString = arguments.getString(0);

        if (targetIDString.isEmpty()) throw new LuaException("target fluid ID cannot be empty");

        final Fluid targetFluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(targetIDString));
        if (targetFluid == null || targetFluid == Fluids.EMPTY) throw new LuaException("invalid fluid ID");
        final AEFluidKey fluidKey = AEFluidKey.of(targetFluid);

        final Collection<Entry<AEKey>> searchedFluids = gridCounter.findFuzzy(fluidKey, FuzzyMode.IGNORE_ALL);
        final List<Hashtable<String, Object>> fluidList = new ArrayList<>();
        for (Entry<AEKey> iterator : searchedFluids) {
            final AEKey key = iterator.getKey();
            if (key instanceof AEFluidKey convertedKey) fluidList.add(convertKeyToData(gridCounter, convertedKey));
        }

        return MethodResult.of(fluidList);
    }

    @LuaFunction(mainThread = true)
    public static MethodResult pushFluid(
            IComputerAccess computer,
            ILuaContext context,
            IArguments arguments,
            IActionHost host,
            IPeripheral triggerPeripheral)
            throws LuaException {
        if (!MENetworkCommand.validateNode(host.getActionableNode())) throw MENetworkCommand.NODE_NOT_FOUND;

        final String targetPeripherialName = arguments.getString(0);
        final String targetFluidName = arguments.getString(1);

        // === DEFINING PERIPHERIAL ===//
        final IPeripheral targetPeripheral = MENetworkCommand.getPeripheralByName(computer, targetPeripherialName);
        if (targetPeripheral == triggerPeripheral) throw MENetworkCommand.SELF_TARGET_PERIPHERAL;
        final Object targetObject = targetPeripheral.getTarget();
        if (targetObject == null) throw new LuaException("target object is not found");
        final IFluidHandler handler = MEFluidCommand.getHandlerWrapper(targetObject);

        // === DEFINING FLUID TARGET ===//
        if (targetFluidName.isEmpty()) throw new LuaException("target fluidID cannot be empty");
        final Fluid targetFluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(targetFluidName));
        if (targetFluid == null || targetFluid == Fluids.EMPTY) throw new LuaException("invalid fluidID");
        final MEStorage gridStorage =
                host.getActionableNode().getGrid().getStorageService().getInventory();

        final int fluidCount = arguments.optInt(2, handler.getTankCapacity(0));
        final long fluidExtracted = gridStorage.extract(
                AEFluidKey.of(targetFluid), fluidCount, Actionable.MODULATE, IActionSource.ofMachine(host));
        if (fluidExtracted == 0) return MethodResult.of(0);

        final int fluidInserted = handler.fill(new FluidStack(targetFluid, (int) fluidExtracted), FluidAction.EXECUTE);

        final int remainingFluid = (int) fluidExtracted - fluidInserted;
        if (remainingFluid > 0) {
            gridStorage.insert(AEFluidKey.of(targetFluid), remainingFluid, Actionable.MODULATE, IActionSource.empty());
        }

        return MethodResult.of(fluidInserted);
    }

    @LuaFunction(mainThread = true)
    public static MethodResult pullFluid(
            IComputerAccess computer,
            ILuaContext context,
            IArguments arguments,
            IActionHost host,
            IPeripheral triggerPeripheral)
            throws LuaException {
        if (!MENetworkCommand.validateNode(host.getActionableNode())) throw MENetworkCommand.NODE_NOT_FOUND;
        final String targetPeripherialName = arguments.getString(0);

        // === DEFINING TARGET PERIPHERIAL ===//
        final IPeripheral targetPeripheral = MENetworkCommand.getPeripheralByName(computer, targetPeripherialName);
        if (targetPeripheral == triggerPeripheral) throw MENetworkCommand.SELF_TARGET_PERIPHERAL;
        final Object targetObject = targetPeripheral.getTarget();
        if (targetObject == null) throw new LuaException("target object is not found");
        final IFluidHandler handler = MEFluidCommand.getHandlerWrapper(targetObject);

        // === DEFINING TARGET FLUID ===//
        final int fluidCount = arguments.optInt(1, handler.getTankCapacity(0));
        final FluidStack targetFluid = handler.drain(fluidCount, FluidAction.EXECUTE);
        if (targetFluid == null || targetFluid.isEmpty()) return MethodResult.of(0);
        final int extractedCount = targetFluid.getAmount();

        final MEStorage gridStorage =
                host.getActionableNode().getGrid().getStorageService().getInventory();

        final long insertedCount = gridStorage.insert(
                AEFluidKey.of(targetFluid),
                targetFluid.getAmount(),
                Actionable.MODULATE,
                IActionSource.ofMachine(host));
        final int remainingCount = extractedCount - (int) insertedCount;

        if (remainingCount > 0) {
            targetFluid.setAmount(remainingCount);
            handler.fill(targetFluid, FluidAction.EXECUTE);
        }
        return MethodResult.of(insertedCount);
    }

    private static Hashtable<String, Object> convertKeyToData(KeyCounter counter, AEFluidKey fluidKey) {
        final Hashtable<String, Object> fluidTable = new Hashtable<>();
        fluidTable.put("fluidID", fluidKey.getId().toString());
        fluidTable.put("fluidName", fluidKey.getDisplayName().getString());
        fluidTable.put("mB", counter.get(fluidKey));

        return fluidTable;
    }

    private static IFluidHandler getHandlerWrapper(@Nonnull Object object) throws LuaException {
        final IFluidHandler handler;
        try {
            handler = getHandler(object);
        } catch (NullPointerException exception) {
            throw new LuaException("target object is not a fluid handler");
        }

        if (handler == null) throw new LuaException("target object does not have tank");
        return handler;
    }

    private static @Nullable IFluidHandler getHandler(@Nonnull Object object) {
        if (object instanceof ICapabilityProvider provider) {
            LazyOptional<IFluidHandler> fluidCap = provider.getCapability(ForgeCapabilities.FLUID_HANDLER);
            return fluidCap.orElseThrow(NullPointerException::new);
        }

        if (object instanceof IFluidHandler handler) return handler;
        return null;
    }
}

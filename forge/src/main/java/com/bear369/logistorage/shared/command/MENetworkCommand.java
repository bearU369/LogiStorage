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

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class MENetworkCommand {
    public static LuaException NODE_NOT_FOUND;
    public static LuaException CMD_NOT_DEFINED;
    public static LuaException SELF_TARGET_PERIPHERAL;

    @LuaFunction
    public static MethodResult getNetworkInfo(
            IComputerAccess computer, ILuaContext context, IArguments arguments, IGridNode gridNode)
            throws LuaException {
        if (!validateNode(gridNode, false)) throw MENetworkCommand.NODE_NOT_FOUND;
        final IEnergyService energyService = gridNode.getGrid().getEnergyService();
        final Hashtable<String, Object> infoTable = new Hashtable<>();

        infoTable.put("isActive", gridNode.isActive());
        if (gridNode.isActive()) {
            infoTable.put("powerIdleUsage", energyService.getIdlePowerUsage());
            infoTable.put("powerAvgUsage", energyService.getAvgPowerUsage());
        }

        return MethodResult.of(infoTable);
    }

    public static void iterateAEKey(IGridNode gridNode, BiConsumer<KeyCounter, AEKey> func) {
        final IGrid grid = gridNode.getGrid();
        final KeyCounter currentStack = grid.getStorageService().getCachedInventory();

        Iterator<Entry<AEKey>> currentIterator = currentStack.iterator();

        while (currentIterator.hasNext()) {
            func.accept(currentStack, currentIterator.next().getKey());
        }
    }

    public static IPeripheral getPeripheralByName(IComputerAccess computer, String peripheralName) throws LuaException {
        IPeripheral targetPeripheral = computer.getAvailablePeripheral(peripheralName);
        if (targetPeripheral == null) throw new LuaException("target peripheral is not found");
        return targetPeripheral;
    }

    public static boolean validateNode(IGridNode gridNode, boolean requiredPower) {
        if (!gridNode.isActive() && requiredPower) return false;
        return true;
    }

    public static boolean validateNode(IGridNode gridNode) {
        return validateNode(gridNode, true);
    }

    public static void init() {
        MENetworkCommand.NODE_NOT_FOUND = new LuaException("AE2 node is not found");
        MENetworkCommand.CMD_NOT_DEFINED = new LuaException("command is not yet defined");
        MENetworkCommand.SELF_TARGET_PERIPHERAL = new LuaException("target peripheral cannot be itself");
    }
}

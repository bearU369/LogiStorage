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
package com.bear369.logistorage.mixin.computercraft;

import com.bear369.logistorage.shared.computercraft.peripherial.MEProcessInterfacePeripherialLogic;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "dan200.computercraft.shared.peripheral.generic.methods.InventoryMethods", remap = false)
@Pseudo
public abstract class InventoryMethodsMixin {

    @WrapOperation(
            method = "pullItems",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Ldan200/computercraft/shared/peripheral/generic/methods/InventoryMethods;extractHandler(Ldan200/computercraft/api/peripheral/IPeripheral;)Lnet/minecraftforge/items/IItemHandler;"),
            remap = false)
    private IItemHandler onPullItems(IPeripheral location, Operation<IItemHandler> org) {
        if (location instanceof MEProcessInterfacePeripherialLogic periperal) return periperal.getItemHandler();
        return org.call(location);
    }
}

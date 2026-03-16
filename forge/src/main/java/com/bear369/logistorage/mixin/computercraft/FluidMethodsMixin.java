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
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "dan200.computercraft.shared.peripheral.generic.methods.FluidMethods", remap = false)
@Pseudo
public abstract class FluidMethodsMixin {

    @WrapOperation(
            method = "pullFluid",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Ldan200/computercraft/shared/peripheral/generic/methods/FluidMethods;extractHandler(Ldan200/computercraft/api/peripheral/IPeripheral;)Lnet/minecraftforge/fluids/capability/IFluidHandler;"),
            remap = false)
    private IFluidHandler onPullFluid(IPeripheral peripheral, Operation<IFluidHandler> org) {
        if (peripheral instanceof MEProcessInterfacePeripherialLogic logic) return logic.getFluidStackHandler();
        return org.call(peripheral);
    }
}

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
package com.bear369.logistorage.network;

import com.bear369.logistorage.menu.AbstractMixContainerMenu;
import com.bear369.logistorage.util.FluidContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

public class FluidSlotInitial {
    private final List<FluidStack> fluidStackList;

    public FluidSlotInitial(List<FluidStack> fluidStackList) {
        this.fluidStackList = fluidStackList;
    }

    public static void encode(FluidSlotInitial msg, FriendlyByteBuf buf) {
        buf.writeShort(msg.fluidStackList.size());
        for (int i = 0; i < msg.fluidStackList.size(); i++) {
            buf.writeFluidStack(msg.fluidStackList.get(i));
        }
    }

    public static FluidSlotInitial decode(FriendlyByteBuf buf) {
        final int size = buf.readShort();
        final List<FluidStack> fluidStackList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) fluidStackList.add(buf.readFluidStack());
        return new FluidSlotInitial(fluidStackList);
    }

    public static void handle(FluidSlotInitial msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            final Minecraft mc = Minecraft.getInstance();
            final LocalPlayer player = Objects.requireNonNull(mc.player);

            if (mc.player != null && player.containerMenu instanceof AbstractMixContainerMenu menu) {
                final FluidContainer fluidContainer = menu.fluidSlots.get(0).container;
                for (int i = 0; i < fluidContainer.getTanks(); i++) {
                    fluidContainer.setFluid(i, msg.fluidStackList.get(i));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

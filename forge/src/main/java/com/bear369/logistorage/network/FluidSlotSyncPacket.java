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
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

public class FluidSlotSyncPacket {
    private final int slotIndex;
    private final FluidStack fluidStack;

    public FluidSlotSyncPacket(int slotIndex, FluidStack fluidStack) {
        this.slotIndex = slotIndex;
        this.fluidStack = fluidStack;
    }

    public static void encode(FluidSlotSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slotIndex);
        msg.fluidStack.writeToPacket(buf);
    }

    public static FluidSlotSyncPacket decode(FriendlyByteBuf buf) {
        return new FluidSlotSyncPacket(buf.readInt(), FluidStack.readFromPacket(buf));
    }

    public static void handle(FluidSlotSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            final Minecraft mc = Minecraft.getInstance();
            final LocalPlayer player = Objects.requireNonNull(mc.player);

            if (mc.player != null && player.containerMenu instanceof AbstractMixContainerMenu menu) {
                final FluidContainer container = menu.fluidSlots.get(msg.slotIndex).container;
                container.setFluid(msg.slotIndex, msg.fluidStack);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

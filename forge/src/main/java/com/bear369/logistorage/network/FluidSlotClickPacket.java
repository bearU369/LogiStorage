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
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.network.NetworkEvent;

public class FluidSlotClickPacket {
    private final int slotIndex;

    public FluidSlotClickPacket(int index) {
        this.slotIndex = index;
    }

    public static void encode(FluidSlotClickPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slotIndex);
    }

    public static FluidSlotClickPacket decode(FriendlyByteBuf buf) {
        return new FluidSlotClickPacket(buf.readInt());
    }

    public static void handle(FluidSlotClickPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof AbstractMixContainerMenu menu) {
                ItemStack carried = menu.getCarried();
                FluidUtil.getFluidHandler(carried).ifPresent((handler) -> {
                    final FluidContainer fluidContainer = menu.fluidSlots.get(msg.slotIndex).container;
                    final FluidStack containerSimulate =
                            fluidContainer.drainInSlot(msg.slotIndex, Integer.MAX_VALUE, FluidAction.SIMULATE);
                    final FluidStack handlerSimulate = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
                    if (!containerSimulate.isEmpty()) { // CONTAINER --> BUCKET
                        final int fill = handler.fill(containerSimulate, FluidAction.EXECUTE);
                        fluidContainer.drainInSlot(msg.slotIndex, fill, FluidAction.EXECUTE);
                        menu.setCarried(handler.getContainer());
                    } else { // CONTAINER <-- BUCKET
                        final int fill = fluidContainer.fillInSlot(msg.slotIndex, handlerSimulate, FluidAction.EXECUTE);
                        handler.drain(fill, FluidAction.EXECUTE);
                        menu.setCarried(handler.getContainer());
                    }
                    NetworkHandler.sendToPlayer(
                            new FluidSlotSyncPacket(msg.slotIndex, fluidContainer.getFluid(msg.slotIndex)), player);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

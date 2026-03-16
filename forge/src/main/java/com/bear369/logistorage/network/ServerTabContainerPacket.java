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

import com.bear369.logistorage.menu.AbstractTabContainerMenu;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ServerTabContainerPacket {
    private final int containerId;
    private final int selectedTabIndex;

    public ServerTabContainerPacket(int containerId, int selectedTabIndex) {
        this.containerId = containerId;
        this.selectedTabIndex = selectedTabIndex;
    }

    public static void encode(ServerTabContainerPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.containerId);
        buf.writeInt(msg.selectedTabIndex);
    }

    public static ServerTabContainerPacket decode(FriendlyByteBuf buf) {
        return new ServerTabContainerPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(ServerTabContainerPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            final ServerPlayer player = ctx.get().getSender();

            if (player != null
                    && player.containerMenu instanceof AbstractTabContainerMenu containerMenu
                    && player.containerMenu.containerId == msg.containerId) {
                containerMenu.swapTabById(msg.selectedTabIndex);
                NetworkHandler.sendToPlayer(
                        new ClientTabContainerPacket(msg.containerId, msg.selectedTabIndex), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

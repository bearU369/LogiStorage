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
import com.bear369.logistorage.menu.screen.AbstractTabContainerScreen;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClientTabContainerPacket {
    private final int containerId;
    private final int selectedTabIndex;

    public ClientTabContainerPacket(int containerId, int selectedTabIndex) {
        this.containerId = containerId;
        this.selectedTabIndex = selectedTabIndex;
    }

    public static void encode(ClientTabContainerPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.containerId);
        buf.writeInt(msg.selectedTabIndex);
    }

    public static ClientTabContainerPacket decode(FriendlyByteBuf buf) {
        return new ClientTabContainerPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(ClientTabContainerPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            final Minecraft mc = Minecraft.getInstance();
            final boolean checkFlag = checkMenuStat(mc);
            final LocalPlayer player = Objects.requireNonNull(mc.player);

            if (checkFlag
                    && mc.screen instanceof AbstractTabContainerScreen screen
                    && player.containerMenu instanceof AbstractTabContainerMenu menu) {
                menu.swapTabById(msg.selectedTabIndex);
                screen.refreshTab();
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean checkMenuStat(Minecraft instance) {
        final LocalPlayer player = Objects.requireNonNull(instance.player);
        return player.containerMenu != null && instance.screen != null;
    }
}

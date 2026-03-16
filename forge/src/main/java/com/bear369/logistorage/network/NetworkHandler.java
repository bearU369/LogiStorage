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

import com.bear369.logistorage.core.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    private static SimpleChannel INSTANCE;

    private static int packetID = 0;

    private static int id() {
        return packetID++;
    }

    protected NetworkHandler() {}

    public static void register() {
        SimpleChannel channel = NetworkRegistry.ChannelBuilder.named(
                        ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "message"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = channel;

        channel.messageBuilder(FluidSlotClickPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(FluidSlotClickPacket::decode)
                .encoder(FluidSlotClickPacket::encode)
                .consumerMainThread(FluidSlotClickPacket::handle)
                .add();

        channel.messageBuilder(ServerTabContainerPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ServerTabContainerPacket::decode)
                .encoder(ServerTabContainerPacket::encode)
                .consumerMainThread(ServerTabContainerPacket::handle)
                .add();

        channel.messageBuilder(FluidSlotSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FluidSlotSyncPacket::decode)
                .encoder(FluidSlotSyncPacket::encode)
                .consumerMainThread(FluidSlotSyncPacket::handle)
                .add();

        channel.messageBuilder(ClientTabContainerPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientTabContainerPacket::decode)
                .encoder(ClientTabContainerPacket::encode)
                .consumerMainThread(ClientTabContainerPacket::handle)
                .add();

        channel.messageBuilder(FluidSlotInitial.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FluidSlotInitial::decode)
                .encoder(FluidSlotInitial::encode)
                .consumerMainThread(FluidSlotInitial::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}

/*
 * Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
 * Copyright (C) 2023-present WildfireRomeo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wildfire.main.networking;

import com.wildfire.main.WildfireGender;
import com.wildfire.main.networking.packets.hello.AbstractSyncHelloPacket;
import com.wildfire.main.networking.packets.hello.ClientboundSyncHelloPacket;
import com.wildfire.main.networking.packets.hello.ServerboundSyncHelloPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.util.TriState;

/*package-private*/ final class WildfireConfigNetworking {
    /*package-private*/ static void register() {
        PayloadTypeRegistry.serverboundConfiguration().register(ClientboundSyncHelloPacket.TYPE, ClientboundSyncHelloPacket.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(ClientboundSyncHelloPacket.TYPE, ClientboundSyncHelloPacket.CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(ServerboundSyncHelloPacket.TYPE, ServerboundSyncHelloPacket.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(ServerboundSyncHelloPacket.TYPE, ServerboundSyncHelloPacket.CODEC);

        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Registering server-side config phase receiver");
        ServerConfigurationNetworking.registerGlobalReceiver(ServerboundSyncHelloPacket.TYPE, WildfireConfigNetworking::handleServerbound);
    }

    @Environment(EnvType.CLIENT)
    /*package-private*/ static void registerClient() {
        ClientConfigurationConnectionEvents.INIT.register(WildfireConfigNetworking::initClient);
        ClientConfigurationConnectionEvents.START.register(WildfireConfigNetworking::startClient);
    }

    @Environment(EnvType.CLIENT)
    private static void initClient(ClientConfigurationPacketListener listener, Minecraft client) {
        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Registering client-side config phase receiver");
        ClientConfigurationNetworking.registerReceiver(ClientboundSyncHelloPacket.TYPE, WildfireConfigNetworking::handleClientbound);
    }

    @Environment(EnvType.CLIENT)
    private static void startClient(ClientConfigurationPacketListener listener, Minecraft client) {
        if(ClientConfigurationNetworking.canSend(ServerboundSyncHelloPacket.TYPE)) {
            WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Sending hello packet to server");
            ClientConfigurationNetworking.send(new ServerboundSyncHelloPacket());
        } else {
            WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Server does not accept hello packet");
        }
    }

    private static void handleServerbound(ServerboundSyncHelloPacket packet, ServerConfigurationNetworking.Context context) {
        context.responseSender().sendPacket(new ClientboundSyncHelloPacket());
        int version = packet.version();
        int expected = AbstractSyncHelloPacket.VERSION;

        if(packet.version() == expected) {
            WildfireGender.LOGGER.info(WildfireSync.MARKER, "Received hello packet from client with protocol version {}", version);
            context.packetContext().set(WildfireSync.MATCHING_VERSION, TriState.TRUE);
        } else {
            WildfireGender.LOGGER.warn(
                WildfireSync.MARKER,
                "Client reported an unsupported sync protocol version! Client supports version {} but we expect {}",
                version, expected
            );
            context.packetContext().set(WildfireSync.MATCHING_VERSION, TriState.FALSE);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void handleClientbound(ClientboundSyncHelloPacket packet, ClientConfigurationNetworking.Context context) {
        int version = packet.version();
        int expected = AbstractSyncHelloPacket.VERSION;

        if(version == expected) {
            WildfireGender.LOGGER.info(WildfireSync.MARKER, "Received hello response from server with protocol version {}", version);
            context.packetContext().set(WildfireSync.MATCHING_VERSION, TriState.TRUE);
        } else {
            WildfireGender.LOGGER.warn(
                WildfireSync.MARKER,
                "Server reported an unsupported sync protocol version! Server supports version {} but we expect {}",
                version, expected
            );
            context.packetContext().set(WildfireSync.MATCHING_VERSION, TriState.FALSE);
        }
    }
}

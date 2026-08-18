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

package com.wildfire.common.networking;

import com.wildfire.common.WildfireGender;
import com.wildfire.common.networking.packets.hello.AbstractHelloConfigurationTask;
import com.wildfire.common.networking.packets.hello.ClientboundSyncHelloPacket;
import com.wildfire.common.networking.packets.hello.ServerboundSyncHelloPacket;
import com.wildfire.common.networking.packets.sync.ClientboundSyncPacket;
import com.wildfire.common.networking.packets.sync.ServerboundSyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.jetbrains.annotations.ApiStatus;

public class FabricSync {

    @ApiStatus.Internal
    public static void register() {
        //Note: Fabric requires registering packets on both client and server side, even if it is a single directional packet

        //Configuration
        PayloadTypeRegistry.serverboundConfiguration().register(ClientboundSyncHelloPacket.TYPE, ClientboundSyncHelloPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(ClientboundSyncHelloPacket.TYPE, ClientboundSyncHelloPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(ServerboundSyncHelloPacket.TYPE, ServerboundSyncHelloPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(ServerboundSyncHelloPacket.TYPE, ServerboundSyncHelloPacket.STREAM_CODEC);

        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Registering server-side config phase receiver");
        ServerConfigurationNetworking.registerGlobalReceiver(ServerboundSyncHelloPacket.TYPE, (packet, context) -> {
            packet.handle(version -> context.packetContext().set(FabricNetworking.VERSION, version));
            context.packetListener().completeTask(AbstractHelloConfigurationTask.TYPE);
        });

        ServerConfigurationConnectionEvents.CONFIGURE.register((listener, _) -> {
            //TODO - Neo: This currently returns false from neo clients due to https://github.com/neoforged/NeoForge/issues/1913
            // Once https://github.com/neoforged/NeoForge/pull/3417 is merged and backported to 26.1, we should bump the min Neo versions
            if (ServerConfigurationNetworking.canSend(listener, ClientboundSyncHelloPacket.TYPE)) {
                listener.addTask(new HelloConfigurationTask());
            } else {
                WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Client does not accept hello packet");
            }
        });

        //Play
        PayloadTypeRegistry.serverboundPlay().register(ClientboundSyncPacket.TYPE, ClientboundSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundSyncPacket.TYPE, ClientboundSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundSyncPacket.TYPE, ServerboundSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerboundSyncPacket.TYPE, ServerboundSyncPacket.STREAM_CODEC);

        ServerPlayConnectionEvents.INIT.register((listener, _) -> {
            if (WildfireNetworking.INSTANCE.versionMatches(listener.connection)) {
                ServerPlayNetworking.registerReceiver(listener, ServerboundSyncPacket.TYPE, (packet, context) -> packet.handle(context.server(), context.player()));
            } else {
                WildfireGender.LOGGER.debug(WildfireSync.MARKER, "{} is not using a supported sync protocol version (or doesn't have the mod), not registering receivers",
                    listener.getPlayer()
                );
            }
        });
    }

    /// @apiNote Only call on the client
    @ApiStatus.Internal
    public static void registerClient() {
        //Configuration
        ClientConfigurationConnectionEvents.INIT.register((_, _) -> {
            WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Registering client-side config phase receiver");
            ClientConfigurationNetworking.registerReceiver(ClientboundSyncHelloPacket.TYPE, (packet, context) ->
                packet.handle(context.responseSender()::sendPacket, version -> context.packetContext().set(FabricNetworking.VERSION, version))
            );
        });

        //Play
        ClientPlayConnectionEvents.INIT.register((listener, _) -> {
            if (WildfireNetworking.INSTANCE.versionMatches(listener.getConnection())) {
                ClientPlayNetworking.registerReceiver(ClientboundSyncPacket.TYPE, (packet, context) -> packet.handle(context.player().getUUID()));
            } else {
                WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Server is not using a supported sync protocol version (or doesn't have the mod), not registering receivers");
            }
        });
    }
}

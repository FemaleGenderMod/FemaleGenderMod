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

package com.wildfire.neoforge.common.networking;

import com.wildfire.api.WildfireAPI;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.networking.WildfireSync;
import com.wildfire.common.networking.packets.hello.AbstractHelloConfigurationTask;
import com.wildfire.common.networking.packets.hello.ClientboundSyncHelloPacket;
import com.wildfire.common.networking.packets.hello.ServerboundSyncHelloPacket;
import com.wildfire.common.networking.packets.sync.ClientboundSyncPacket;
import com.wildfire.common.networking.packets.sync.ServerboundSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NeoSync {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterConfigurationTasksEvent.class, event -> {
            if (event.getListener().hasChannel(ClientboundSyncHelloPacket.TYPE)) {
                event.register(new HelloConfigurationTask());
            } else {
                WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Server does not accept hello packet");
            }
        });
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, NeoSync::registerPackets);
    }

    private static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(WildfireAPI.MODID).optional();
        //Client to server
        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Registering server-side config phase receiver");
        registrar.configurationToServer(ServerboundSyncHelloPacket.TYPE, ServerboundSyncHelloPacket.STREAM_CODEC, (packet, context) -> {
            packet.handle(version -> context.connection().channel().attr(NeoNetworking.VERSION).set(version));
            context.finishCurrentTask(AbstractHelloConfigurationTask.TYPE);
        });
        //Note: We register this regardless of the sync hello packet status, as Neo collects these initially and then registers them to the channel.
        // The packet will only be sent to the server if the client's version matches the one from the server in the sync hello, so registering the channel is harmless
        registrar.playToServer(ServerboundSyncPacket.TYPE, ServerboundSyncPacket.STREAM_CODEC, (packet, context) -> {
            if (context.player() instanceof ServerPlayer player) {//Should always be true
                packet.handle(player.level().getServer(), player);
            } else {
                WildfireGender.LOGGER.warn(WildfireSync.MARKER, "Server received a sync packet but the player wasn't a server player? This shouldn't be possible.");
            }
        });
        //Server to client
        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Registering client-side config phase receiver");
        registrar.configurationToClient(ClientboundSyncHelloPacket.TYPE, ClientboundSyncHelloPacket.STREAM_CODEC, (packet, context) ->
            packet.handle(context::reply, version -> context.connection().channel().attr(NeoNetworking.VERSION).set(version))
        );

        //Note: We register this regardless of the sync hello packet status, as Neo collects these initially and then registers them to the channel.
        // The packet will only be sent to the client if the server's version matches the one from the client in the sync hello, so registering the channel is harmless
        registrar.playToClient(ClientboundSyncPacket.TYPE, ClientboundSyncPacket.STREAM_CODEC, (packet, context) -> packet.handle(context.player().getUUID()));
    }
}

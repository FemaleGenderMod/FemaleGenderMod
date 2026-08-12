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
import com.wildfire.main.networking.packets.hello.ClientboundSyncHelloPacket;
import com.wildfire.main.networking.packets.hello.ServerboundSyncHelloPacket;
import com.wildfire.main.networking.packets.hello.SyncHelloPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;

/*package-private*/ final class WildfireConfigNetworking {

    /*package-private*/ static void register() {
        PayloadTypeRegistry.serverboundConfiguration().register(ClientboundSyncHelloPacket.TYPE, ClientboundSyncHelloPacket.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(ClientboundSyncHelloPacket.TYPE, ClientboundSyncHelloPacket.CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(ServerboundSyncHelloPacket.TYPE, ServerboundSyncHelloPacket.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(ServerboundSyncHelloPacket.TYPE, ServerboundSyncHelloPacket.CODEC);

        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Registering server-side config phase receiver");
        ServerConfigurationNetworking.registerGlobalReceiver(ServerboundSyncHelloPacket.TYPE, WildfireConfigNetworking::handleServerbound);
    }

    private static void handleServerbound(ServerboundSyncHelloPacket packet, ServerConfigurationNetworking.Context context) {
        context.responseSender().sendPacket(new ClientboundSyncHelloPacket());
        int version = packet.version();
        int expected = SyncHelloPacket.VERSION;

        context.packetContext().set(WildfireSync.VERSION, version);
        if (packet.version() == expected) {
            WildfireGender.LOGGER.info(WildfireSync.MARKER, "Received hello packet from client with protocol version {}", version);
        } else {
            WildfireGender.LOGGER.warn(WildfireSync.MARKER, "Client reported an unsupported sync protocol version! Client supports version {} but we expect {}",
                version, expected
            );
        }
    }
}

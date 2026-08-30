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
import com.wildfire.main.entitydata.PlayerConfigHolder;
import com.wildfire.main.networking.packets.sync.ClientboundSyncPacket;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
/// @apiNote Only call this on the client
/*package-private*/ final class WildfireClientPlayNetworking {

    /*package-private*/ static void registerClient() {
        ClientPlayConnectionEvents.INIT.register(WildfireClientPlayNetworking::initClient);
    }

    private static void initClient(ClientPacketListener listener, Minecraft client) {
        if (WildfireSync.versionMatches(listener)) {
            ClientPlayNetworking.registerReceiver(ClientboundSyncPacket.TYPE, WildfireClientPlayNetworking::handleClientbound);
        } else {
            WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Server is not using a supported sync protocol version (or doesn't have the mod), not registering receivers");
        }
    }

    private static void handleClientbound(ClientboundSyncPacket packet, ClientPlayNetworking.Context context) {
        UUID uuid = packet.uuid();
        if (context.player().getUUID().equals(uuid)) {
            WildfireGender.LOGGER.warn("Ignoring sync packet referring to the client player");
            return;
        }
        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Received player data for player {}", uuid);
        PlayerConfigHolder plr = WildfireGender.getOrAddPlayerById(uuid);
        plr.updateFromPacket(packet.config(), true);
    }
}

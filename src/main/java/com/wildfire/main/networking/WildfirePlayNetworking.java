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
import com.wildfire.main.networking.packets.sync.ServerboundSyncPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/*package-private*/ final class WildfirePlayNetworking {

    /*package-private*/ static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ClientboundSyncPacket.TYPE, ClientboundSyncPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundSyncPacket.TYPE, ClientboundSyncPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundSyncPacket.TYPE, ServerboundSyncPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerboundSyncPacket.TYPE, ServerboundSyncPacket.CODEC);

        ServerPlayConnectionEvents.INIT.register(WildfirePlayNetworking::initServer);
    }

    private static void initServer(ServerGamePacketListenerImpl listener, MinecraftServer server) {
        if (WildfireSync.versionMatches(listener)) {
            ServerPlayNetworking.registerReceiver(listener, ServerboundSyncPacket.TYPE, WildfirePlayNetworking::handleServerbound);
        } else {
            WildfireGender.LOGGER.debug(WildfireSync.MARKER, "{} is not using a supported sync protocol version (or doesn't have the mod), not registering receivers",
                listener.getPlayer()
            );
        }
    }

    private static void handleServerbound(ServerboundSyncPacket packet, ServerPlayNetworking.Context context) {
        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Received player data from player {}", context.player());
        ServerPlayer player = context.player();
        PlayerConfigHolder plr = WildfireGender.getOrAddPlayerById(player.getUUID());
        if (!context.server().isSingleplayerOwner(player.nameAndId())) {
            //Note: We skip bothering to update the config if the server is an integrated server hosted by the player who sent it
            // In that case the actual backing config will have already been updated because of it being stored in a static field
            // which has the side effect of reaching across logical sides and updating both the server and client at once.
            plr.updateFromPacket(packet.config(), false);
        }
        WildfireSync.sendToAllClients(player, plr);
    }
}

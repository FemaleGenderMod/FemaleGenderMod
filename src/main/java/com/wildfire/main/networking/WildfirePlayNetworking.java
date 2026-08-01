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
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.packets.sync.ClientboundSyncPacket;
import com.wildfire.main.networking.packets.sync.ServerboundSyncPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.TriState;

/*package-private*/ final class WildfirePlayNetworking {
    /*package-private*/ static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ClientboundSyncPacket.TYPE, ClientboundSyncPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundSyncPacket.TYPE, ClientboundSyncPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundSyncPacket.TYPE, ServerboundSyncPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerboundSyncPacket.TYPE, ServerboundSyncPacket.CODEC);

        ServerPlayConnectionEvents.INIT.register(WildfirePlayNetworking::initServer);
    }

    @Environment(EnvType.CLIENT)
    /*package-private*/ static void registerClient() {
        ClientPlayConnectionEvents.INIT.register(WildfirePlayNetworking::initClient);
    }

    private static void initServer(ServerGamePacketListenerImpl listener, MinecraftServer server) {
        if(listener.getPacketContext().orElse(WildfireSync.MATCHING_VERSION, TriState.DEFAULT).toBoolean(false)) {
            ServerPlayNetworking.registerReceiver(listener, ServerboundSyncPacket.TYPE, WildfirePlayNetworking::handleServerbound);
        } else {
            WildfireGender.LOGGER.debug(
                WildfireSync.MARKER,
                "{} is not using a supported sync protocol version (or doesn't have the mod), not registering receivers",
                listener.getPlayer()
            );
        }
    }

    @Environment(EnvType.CLIENT)
    private static void initClient(ClientPacketListener listener, Minecraft client) {
        if(listener.getPacketContext().orElse(WildfireSync.MATCHING_VERSION, TriState.DEFAULT).toBoolean(false)) {
            ClientPlayNetworking.registerReceiver(ClientboundSyncPacket.TYPE, WildfirePlayNetworking::handleClientbound);
        } else {
            WildfireGender.LOGGER.debug(
                WildfireSync.MARKER,
                "Server is not using a supported sync protocol version (or doesn't have the mod), not registering receivers"
            );
        }
    }

    private static void handleServerbound(ServerboundSyncPacket packet, ServerPlayNetworking.Context context) {
        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Received player data from player {}", context.player());
        ServerPlayer player = context.player();
        PlayerConfig plr = WildfireGender.getOrAddPlayerById(player.getUUID());
        packet.updatePlayerFromPacket(plr);
        WildfireSync.sendToAllClients(player, plr);
    }

    @Environment(EnvType.CLIENT)
    private static void handleClientbound(ClientboundSyncPacket packet, ClientPlayNetworking.Context context) {
        if(context.player().getUUID().equals(packet.uuid)) {
            WildfireGender.LOGGER.warn("Ignoring sync packet referring to the client player");
            return;
        }

        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Received player data for player {}", packet.uuid);
        PlayerConfig plr = WildfireGender.getOrAddPlayerById(packet.uuid);
        packet.updatePlayerFromPacket(plr);
        plr.syncStatus = PlayerConfig.SyncStatus.SYNCED;
    }
}

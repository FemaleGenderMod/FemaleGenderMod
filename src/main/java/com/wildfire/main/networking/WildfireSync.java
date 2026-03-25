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

import com.mojang.logging.LogUtils;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

public final class WildfireSync {
    static final Logger LOGGER = LogUtils.getLogger();

    private WildfireSync() {
        throw new UnsupportedOperationException();
    }

    @ApiStatus.Internal
    public static void register() {
        // note that each packet has to be registered on both sides for receiving and sending, regardless
        // of if the current side is actually supposed to be doing either action for a given packet.
        PayloadTypeRegistry.serverboundPlay().register(ClientboundSyncPacket.ID, ClientboundSyncPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundSyncPacket.ID, ClientboundSyncPacket.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(ServerboundSyncPacket.ID, ServerboundSyncPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerboundSyncPacket.ID, ServerboundSyncPacket.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(SyncHelloPacket.Clientbound.ID, SyncHelloPacket.Clientbound.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncHelloPacket.Clientbound.ID, SyncHelloPacket.Clientbound.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncHelloPacket.Serverbound.ID, SyncHelloPacket.Serverbound.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncHelloPacket.Serverbound.ID, SyncHelloPacket.Serverbound.CODEC);

        ServerPlayConnectionEvents.INIT.register((handler, _) -> {
            ServerPlayNetworking.registerReceiver(handler, ServerboundSyncPacket.ID, ServerboundSyncPacket::handle);
            ServerPlayNetworking.registerReceiver(handler, SyncHelloPacket.Serverbound.ID, SyncHelloPacket.Serverbound::handle);
        });
    }

    @ApiStatus.Internal
    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            ClientPlayNetworking.registerReceiver(ClientboundSyncPacket.ID, ClientboundSyncPacket::handle);
            ClientPlayNetworking.registerReceiver(SyncHelloPacket.Clientbound.ID, SyncHelloPacket.Clientbound::handle);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if(ClientPlayNetworking.canSend(SyncHelloPacket.Serverbound.ID)) {
                LOGGER.debug("Sending hello packet to server");
                sender.sendPacket(new SyncHelloPacket.Serverbound());
            }
        });
    }

    /**
     * Sync a player's configuration to all nearby connected players
     *
     * @param toSync       The {@link ServerPlayer player} to sync
     * @param playerConfig The {@link PlayerConfig configuration} for the target player
     */
    public static void sendToAllClients(ServerPlayer toSync, PlayerConfig playerConfig) {
        PlayerLookup.tracking(toSync).stream()
                .filter(player -> !player.equals(toSync))
                .filter(ClientboundSyncPacket::canSend)
                .forEach(player -> ServerPlayNetworking.send(player, new ClientboundSyncPacket(playerConfig)));
    }

    /**
     * Sync a player's configuration to another connected player
     *
     * @param sendTo The {@link ServerPlayer player} to send the sync to
     * @param toSync The {@link PlayerConfig configuration} for the player being synced
     */
    public static void sendToClient(ServerPlayer sendTo, PlayerConfig toSync) {
        if(ClientboundSyncPacket.canSend(sendTo)) {
            ServerPlayNetworking.send(sendTo, new ClientboundSyncPacket(toSync));
        }
    }

    /**
     * Send the client player's configuration to the server for syncing to other players
     *
     * @param plr The {@link PlayerConfig configuration} for the client player
     */
    @Environment(EnvType.CLIENT)
    public static void sendToServer(PlayerConfig plr) {
        if(!plr.needsSync || !ServerboundSyncPacket.canSend()) return;

        ClientPlayNetworking.send(new ServerboundSyncPacket(plr));
        plr.needsSync = false;
    }
}

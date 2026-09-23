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
import com.wildfire.common.entities.avatars.AbstractAvatarConfigHolder;
import com.wildfire.common.entities.avatars.AvatarConfig;
import com.wildfire.common.entities.avatars.MannequinConfigHolder;
import com.wildfire.common.entities.players.PlayerConfigHolder;
import com.wildfire.common.networking.packets.mannequins.ClientboundMannequinDataPacket;
import com.wildfire.common.networking.packets.mannequins.ServerboundMannequinDataPacket;
import com.wildfire.common.networking.packets.sync.ClientboundSyncPacket;
import com.wildfire.common.networking.packets.sync.ServerboundSyncPacket;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import java.util.Objects;

public final class WildfireSync {

    public static final Marker MARKER = MarkerFactory.getMarker("SYNC");

    private WildfireSync() {
        throw new UnsupportedOperationException();
    }

    /// Sync a player or mannequin's configuration to all nearby connected players
    ///
    /// @param toSync The [Avatar] to sync
    /// @param config The [`configuration`][AbstractAvatarConfigHolder] for the target avatar
    /// @param except Optional player to exclude from being sent a sync packet
    public static void sendToAllClients(Avatar toSync, AbstractAvatarConfigHolder config, @Nullable ServerPlayer except) {
        final CustomPacketPayload.Type<?> type = clientboundSyncType(config);
        int sent = 0;

        for(ServerPlayer player : WildfireNetworking.INSTANCE.playersTracking(toSync)) {
            if(Objects.equals(player, except) || player.equals(toSync)) {
                continue;
            }

            if(WildfireNetworking.INSTANCE.canSendToPlayer(player, type)) {
                WildfireNetworking.INSTANCE.sendToClient(player, clientboundSyncPacket(config));
                sent++;
            }
        }

        if(sent > 0) {
            WildfireGender.LOGGER.debug(MARKER, "Sent sync packet for {} to {} connected player(s)", toSync, sent);
        }
    }

    /// Sync a player or mannequin's configuration to all nearby connected players
    ///
    /// @param toSync The [Avatar] to sync
    /// @param config The [`configuration`][AbstractAvatarConfigHolder] for the target avatar
    public static void sendToAllClients(Avatar toSync, AbstractAvatarConfigHolder config) {
        sendToAllClients(toSync, config, null);
    }

    /// Sync a player or mannequin's configuration to another connected player
    ///
    /// @param sendTo The [`player`][ServerPlayer] to send the sync to
    /// @param toSync The [`configuration`][AvatarConfig] for the player being synced
    public static void sendToClient(ServerPlayer sendTo, AbstractAvatarConfigHolder toSync) {
        if(WildfireNetworking.INSTANCE.canSendToPlayer(sendTo, clientboundSyncType(toSync))) {
            WildfireGender.LOGGER.debug(MARKER, "Sending profile for {} to other player {}", toSync.uuid, sendTo.getUUID());
            WildfireNetworking.INSTANCE.sendToClient(sendTo, clientboundSyncPacket(toSync));
        }
    }

    /// Send the client player's or a mannequin's config to the server for syncing to other players
    ///
    /// @param config The [`configuration`][AvatarConfig] for the client player or target mannequin
    ///
    /// @return `true` if the provided config was synced to the connected server
    ///
    /// @apiNote Only call on the client
    public static boolean sendToServer(Connection connection, AbstractAvatarConfigHolder config) {
        if(WildfireNetworking.INSTANCE.canSendToServer(connection, serverboundSyncType(config))) {
            WildfireGender.LOGGER.debug(MARKER, "Sending data to server for {}", config);
            WildfireNetworking.INSTANCE.sendToServer(serverboundSyncPacket(config));
            return true;
        }
        return false;
    }

    private static CustomPacketPayload.Type<?> clientboundSyncType(AbstractAvatarConfigHolder config) {
        return switch(config) {
            case MannequinConfigHolder _ -> ClientboundMannequinDataPacket.TYPE;
            case PlayerConfigHolder _ -> ClientboundSyncPacket.TYPE;
            default -> throw new IllegalArgumentException("Cannot sync a config of type " + config.getClass());
        };
    }

    private static CustomPacketPayload clientboundSyncPacket(AbstractAvatarConfigHolder config) {
        return switch(config) {
            case MannequinConfigHolder conf -> new ClientboundMannequinDataPacket(conf);
            case PlayerConfigHolder conf -> new ClientboundSyncPacket(conf);
            default -> throw new IllegalArgumentException("Cannot sync a config of type " + config.getClass());
        };
    }

    private static CustomPacketPayload.Type<?> serverboundSyncType(AbstractAvatarConfigHolder config) {
        return switch(config) {
            case MannequinConfigHolder _ -> ServerboundMannequinDataPacket.TYPE;
            case PlayerConfigHolder _ -> ServerboundSyncPacket.TYPE;
            default -> throw new IllegalArgumentException("Cannot sync a config of type " + config.getClass());
        };
    }

    private static CustomPacketPayload serverboundSyncPacket(AbstractAvatarConfigHolder config) {
        return switch(config) {
            case MannequinConfigHolder conf -> new ServerboundMannequinDataPacket(conf);
            case PlayerConfigHolder conf -> new ServerboundSyncPacket(conf);
            default -> throw new IllegalArgumentException("Cannot sync a config of type " + config.getClass());
        };
    }
}

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
import com.wildfire.main.entitydata.PlayerConfigHolder;
import com.wildfire.main.networking.packets.sync.ClientboundSyncPacket;
import com.wildfire.main.networking.packets.sync.ServerboundSyncPacket;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class WildfireSync {

    public static final Marker MARKER = MarkerFactory.getMarker("SYNC");

    private WildfireSync() {
        throw new UnsupportedOperationException();
    }

    /// Sync a player's configuration to all nearby connected players
    ///
    /// @param toSync       The [`player`][ServerPlayer] to sync
    /// @param playerConfig The [`configuration`][PlayerConfigHolder] for the target player
    public static void sendToAllClients(ServerPlayer toSync, PlayerConfigHolder playerConfig) {
        int sent = 0;
        for (ServerPlayer player : WildfireNetworking.INSTANCE.playersTracking(toSync)) {
            if (!player.equals(toSync) && WildfireNetworking.INSTANCE.canSyncToPlayer(player)) {
                sent++;
                WildfireNetworking.INSTANCE.syncToPlayer(player, new ClientboundSyncPacket(playerConfig));
            }
        }
        if (sent > 0) {
            WildfireGender.LOGGER.debug(MARKER, "Sent sync packet for {} to {} connected player(s)", toSync, sent);
        }
    }

    /// Sync a player's configuration to another connected player
    ///
    /// @param sendTo The [`player`][ServerPlayer] to send the sync to
    /// @param toSync The [`configuration`][PlayerConfig] for the player being synced
    public static void sendToClient(ServerPlayer sendTo, PlayerConfigHolder toSync) {
        if (WildfireNetworking.INSTANCE.canSyncToPlayer(sendTo)) {
            WildfireGender.LOGGER.debug(MARKER, "Sending profile for {} to other player {}", toSync.uuid, sendTo.getUUID());
            WildfireNetworking.INSTANCE.syncToPlayer(sendTo, new ClientboundSyncPacket(toSync));
        }
    }

    /// Send the client player's configuration to the server for syncing to other players
    ///
    /// @param plr The [`configuration`][PlayerConfig] for the client player
    ///
    /// @apiNote Only call on the client
    public static void sendToServer(Connection connection, PlayerConfigHolder plr) {
        if (plr.needsSync && WildfireNetworking.INSTANCE.canSyncToServer(connection)) {
            WildfireGender.LOGGER.debug(MARKER, "Sending player data to server");
            WildfireNetworking.INSTANCE.syncToServer(new ServerboundSyncPacket(plr.config()));
            plr.needsSync = false;
        }
    }
}

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
import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.entitydata.PlayerConfigHolder;
import com.wildfire.main.networking.packets.SyncHelloPacket;
import com.wildfire.main.networking.packets.sync.ClientboundSyncPacket;
import com.wildfire.main.networking.packets.sync.ServerboundSyncPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

public final class WildfireSync {
    public static final PacketContext.Key<TriState> MATCHING_VERSION = PacketContext.key(WildfireGender.id("matching_version"));
    public static final Logger LOGGER = LogUtils.getLogger();

    private WildfireSync() {
        throw new UnsupportedOperationException();
    }

    @ApiStatus.Internal
    public static void register() {
        WildfireConfigNetworking.register();
        WildfirePlayNetworking.register();
    }

    @ApiStatus.Internal
    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        WildfireConfigNetworking.registerClient();
        WildfirePlayNetworking.registerClient();
    }

    /// Sync a player's configuration to all nearby connected players
    ///
    /// @param toSync       The [`player`][ServerPlayer] to sync
    /// @param playerConfig The [`configuration`][PlayerConfigHolder] for the target player
    public static void sendToAllClients(ServerPlayer toSync, PlayerConfigHolder playerConfig) {
        int sent = 0;
        for(var player : PlayerLookup.tracking(toSync)) {
            if(player.equals(toSync) || !ClientboundSyncPacket.canSend(player)) {
                continue;
            }

            sent++;
            ServerPlayNetworking.send(player, new ClientboundSyncPacket(playerConfig));
        }

        if(sent > 0) {
            LOGGER.debug("Sent sync packet for {} to {} connected player(s)", toSync, sent);
        }
    }

    /// Sync a player's configuration to another connected player
    ///
    /// @param sendTo The [`player`][ServerPlayer] to send the sync to
    /// @param toSync The [`configuration`][PlayerConfig] for the player being synced
    public static void sendToClient(ServerPlayer sendTo, PlayerConfigHolder toSync) {
        if(ClientboundSyncPacket.canSend(sendTo)) {
            LOGGER.debug("Sending profile for {} to other player {}", toSync.uuid, sendTo.getUUID());
            ServerPlayNetworking.send(sendTo, new ClientboundSyncPacket(toSync));
        }
    }

    /// Send the client player's configuration to the server for syncing to other players
    ///
    /// @param plr The [`configuration`][PlayerConfig] for the client player
    @Environment(EnvType.CLIENT)
    public static void sendToServer(PlayerConfigHolder plr) {
        if (plr.needsSync && ServerboundSyncPacket.canSend()) {
            LOGGER.debug("Sending player data to server");
            ClientPlayNetworking.send(new ServerboundSyncPacket(plr.config()));
            plr.needsSync = false;
        }
    }
}

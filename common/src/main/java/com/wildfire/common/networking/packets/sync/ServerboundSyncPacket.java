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

package com.wildfire.common.networking.packets.sync;

import com.wildfire.api.server.WildfireServerAPI;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.entities.avatars.AvatarConfig;
import com.wildfire.common.entities.players.PlayerConfigHolder;
import com.wildfire.common.networking.WildfireSync;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundSyncPacket(AvatarConfig config) implements CustomPacketPayload {

    public static final Type<ServerboundSyncPacket> TYPE = WildfireGender.serverBoundPacket("sync");
    public static final StreamCodec<ByteBuf, ServerboundSyncPacket> STREAM_CODEC = AvatarConfig.COMPACT_STREAM_CODEC.map(ServerboundSyncPacket::new, ServerboundSyncPacket::config);

    public ServerboundSyncPacket(PlayerConfigHolder holder) {
        this(holder.config());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player) {
        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Received player data from player {}", player);
        PlayerConfigHolder plr = WildfireServerAPI.players().getOrCreate(player);
        plr.updateFromPacket(config);
        WildfireSync.sendToAllClients(player, plr);
    }
}

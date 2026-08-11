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

import com.wildfire.common.WildfireGender;
import com.wildfire.common.entitydata.PlayerConfig;
import com.wildfire.common.entitydata.PlayerConfigHolder;
import com.wildfire.common.networking.WildfireSync;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundSyncPacket(UUID uuid, PlayerConfig config) implements CustomPacketPayload {

    public static final Type<ClientboundSyncPacket> TYPE = WildfireGender.clientBoundPacket("sync");
    public static final StreamCodec<ByteBuf, ClientboundSyncPacket> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, p -> p.uuid,
        PlayerConfig.COMPACT_STREAM_CODEC, p -> p.config,
        ClientboundSyncPacket::new
    );

    public ClientboundSyncPacket(PlayerConfigHolder plr) {
        this(plr.uuid, plr.config());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(UUID contextUUID) {
        if (contextUUID.equals(uuid)) {
            WildfireGender.LOGGER.warn(WildfireSync.MARKER, "Ignoring sync packet referring to the client player");
            return;
        }
        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Received player data for player {}", uuid);
        PlayerConfigHolder plr = WildfireGender.getOrAddPlayerById(uuid);
        plr.updateFromPacket(config, true);
    }
}

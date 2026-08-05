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
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record ClientboundSyncPacket(UUID uuid, PlayerConfig config) implements CustomPacketPayload {

    public static final Type<ClientboundSyncPacket> ID = new CustomPacketPayload.Type<>(WildfireGender.id("sync"));
    public static final StreamCodec<ByteBuf, ClientboundSyncPacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, p -> p.uuid,
        PlayerConfig.STREAM_CODEC, p -> p.config,
        ClientboundSyncPacket::new
    );

    public ClientboundSyncPacket(PlayerConfigHolder plr) {
        this(plr.uuid, plr.config());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static boolean canSend(ServerPlayer player) {
        return ServerPlayNetworking.canSend(player, ID);
    }

    @Environment(EnvType.CLIENT)
    public void handle(ClientPlayNetworking.Context context) {
        if(context.player().getUUID().equals(uuid)) {
            WildfireGender.LOGGER.warn("Ignoring sync packet referring to the client player");
            return;
        }

        PlayerConfigHolder plr = WildfireGender.getOrAddPlayerById(uuid);
        plr.updateFromPacket(config, true);
    }
}

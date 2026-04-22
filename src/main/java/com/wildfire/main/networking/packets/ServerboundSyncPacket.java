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

package com.wildfire.main.networking.packets;

import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.WildfireSync;
import com.wildfire.main.entitydata.PlayerConfigHolder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundSyncPacket(PlayerConfig config) implements CustomPacketPayload {

    public static final Type<ServerboundSyncPacket> ID = new CustomPacketPayload.Type<>(WildfireGender.id("serverbound/sync"));
    public static final StreamCodec<ByteBuf, ServerboundSyncPacket> CODEC = PlayerConfig.COMPACT_STREAM_CODEC.map(ServerboundSyncPacket::new, ServerboundSyncPacket::config);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public static boolean canSend() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if(connection == null) {
            return false;
        }
        TriState matchingVersion = connection.getPacketContext().orElse(WildfireSync.MATCHING_VERSION, TriState.DEFAULT);
        return ClientPlayNetworking.canSend(ID) && matchingVersion.toBoolean(false);
    }

    public void handle(ServerPlayNetworking.Context context) {
        WildfireSync.LOGGER.debug("Received player data from player {}", context.player().getUUID());
        ServerPlayer player = context.player();
        PlayerConfigHolder plr = WildfireGender.getOrAddPlayerById(player.getUUID());
        if (!context.server().isSingleplayerOwner(player.nameAndId())) {
            //Note: We skip bothering to update the config if the server is an integrated server hosted by the player who sent it
            // In that case the actual backing config will have already been updated because of it being stored in a static field
            // which has the side effect of reaching across logical sides and updating both the server and client at once.
            plr.updateFromPacket(config, false);
        }
        WildfireSync.sendToAllClients(player, plr);
    }
}

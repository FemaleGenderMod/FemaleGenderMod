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
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.PlayerConfig;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class ServerboundSyncPacket extends AbstractSyncPacket implements CustomPacketPayload {

    public static final Type<ServerboundSyncPacket> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(WildfireGender.MODID, "send_gender_info"));
    public static final StreamCodec<ByteBuf, ServerboundSyncPacket> CODEC = codec(ServerboundSyncPacket::new);

    public ServerboundSyncPacket(PlayerConfig plr) {
        super(plr);
    }

    private ServerboundSyncPacket(UUID uuid, Gender gender, float bustSize, boolean hurtSounds, float voicePitch, BreastPhysics physics, Breasts breasts, UVLayouts uvLayouts) {
        super(uuid, gender, bustSize, hurtSounds, voicePitch, physics, breasts, uvLayouts);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public static boolean canSend() {
        return ClientPlayNetworking.canSend(ID);
    }

    public void handle(ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        PlayerConfig plr = WildfireGender.getOrAddPlayerById(player.getUUID());
        updatePlayerFromPacket(plr);
        WildfireSync.sendToAllClients(player, plr);
    }
}

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

package com.wildfire.common.networking.packets.mannequins;

import com.wildfire.api.client.WildfireClientAPI;
import com.wildfire.api.server.WildfireServerAPI;
import com.wildfire.client.gui.screen.WardrobeBrowserScreen;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.entities.avatars.AvatarConfig;
import com.wildfire.common.entities.avatars.MannequinConfigHolder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.decoration.Mannequin;

public record ClientboundEditMannequinPacket(UUID uuid, AvatarConfig config) implements CustomPacketPayload {
    public static final Type<ClientboundEditMannequinPacket> TYPE = WildfireGender.clientBoundPacket("mannequin/edit");
    public static final StreamCodec<ByteBuf, ClientboundEditMannequinPacket> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, ClientboundEditMannequinPacket::uuid,
        AvatarConfig.STREAM_CODEC, ClientboundEditMannequinPacket::config,
        ClientboundEditMannequinPacket::new
    );

    public ClientboundEditMannequinPacket(Mannequin mannequin) {
        MannequinConfigHolder config = WildfireServerAPI.mannequins().getOrCreate(mannequin);
        this(mannequin.getUUID(), config.config());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /// @apiNote Only call on the client
    public void handle() {
        final MannequinConfigHolder config = WildfireClientAPI.mannequins().getOrCreate(uuid());
        final Minecraft client = Minecraft.getInstance();
        config.setConfig(config());
        client.execute(() -> WardrobeBrowserScreen.open(client, config));
    }
}

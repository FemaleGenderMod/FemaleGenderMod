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
import com.wildfire.client.gui.screen.WardrobeBrowserScreen;
import com.wildfire.common.WildfireGender;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundEditMannequinPacket(UUID uuid) implements CustomPacketPayload {
    public static final Type<ClientboundEditMannequinPacket> TYPE = WildfireGender.clientBoundPacket("edit_mannequin");
    public static final StreamCodec<ByteBuf, ClientboundEditMannequinPacket> STREAM_CODEC = UUIDUtil.STREAM_CODEC
        .map(ClientboundEditMannequinPacket::new, ClientboundEditMannequinPacket::uuid);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /// @apiNote Only call on the client
    public void handle() {
        var config = WildfireClientAPI.mannequins().getOrCreate(uuid());
        WardrobeBrowserScreen.open(Minecraft.getInstance(), config);
    }
}

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

package com.wildfire.main.networking.packets.hello;

import com.wildfire.main.WildfireGender;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class ClientboundSyncHelloPacket extends AbstractSyncHelloPacket {
    public static final Type<ClientboundSyncHelloPacket> TYPE = WildfireGender.packet("clientbound/hello");
    public static final StreamCodec<ByteBuf, ClientboundSyncHelloPacket> CODEC = codec(ClientboundSyncHelloPacket::new);

    private final int version;

    public ClientboundSyncHelloPacket() {
        this(VERSION);
    }

    public ClientboundSyncHelloPacket(int version) {
        this.version = version;
    }

    @Override
    public int version() {
        return version;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

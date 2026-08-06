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

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public abstract sealed class AbstractSyncHelloPacket implements CustomPacketPayload permits ClientboundSyncHelloPacket, ServerboundSyncHelloPacket {
    /// Denotes the current sync protocol version
    ///
    /// This version handshake is initiated by the connecting client, with the server only then responding
    /// with its own protocol version.
    ///
    /// If the server doesn't respond or responds with a different value, then the server is assumed to not support
    /// syncing over the current protocol, and will not send or receive any sync packets, and vice versa.
    ///
    /// | Protocol Version   | Changes                                                                                                                          |
    /// | ------------------ | -------------------------------------------------------------------------------------------------------------------------------- |
    /// | `1` (5.0.0-Beta.2) | Initial versioned protocol                                                                                                       |
    /// | `2` (TBD)          | Hello packet is now sent during config phase and is now required, sync packets are now identified as `{client,server}bound/sync` |
    public static final int VERSION = 2;//TODO: Do we want to try to make it so that if it detects version 1 it syncs using that format?

    public abstract int version();

    protected static <T extends AbstractSyncHelloPacket> StreamCodec<ByteBuf, T> codec(Function<Integer, T> constructor) {
        return StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AbstractSyncHelloPacket::version,
            constructor
        );
    }
}

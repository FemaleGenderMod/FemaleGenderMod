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

package com.wildfire.common.networking.packets.hello;

import com.wildfire.common.WildfireGender;
import com.wildfire.common.networking.WildfireSync;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundSyncHelloPacket(int version) implements SyncHelloPacket {

    public static final Type<ClientboundSyncHelloPacket> TYPE = WildfireGender.clientBoundPacket("hello");
    public static final StreamCodec<ByteBuf, ClientboundSyncHelloPacket> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(ClientboundSyncHelloPacket::new, SyncHelloPacket::version);

    public ClientboundSyncHelloPacket() {
        this(VERSION);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(Consumer<ServerboundSyncHelloPacket> replySender, IntConsumer versionSetter) {
        replySender.accept(new ServerboundSyncHelloPacket());
        versionSetter.accept(version);
        if (version == VERSION) {
            WildfireGender.LOGGER.info(WildfireSync.MARKER, "Received hello packet from server with protocol version {}", version);
        } else {
            WildfireGender.LOGGER.warn(WildfireSync.MARKER, "Server is using an unsupported sync protocol version! Server supports version {} but we expect {}",
                version, VERSION
            );
        }
    }
}

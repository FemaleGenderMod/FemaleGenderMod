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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundSyncHelloPacket(IntArrayList version) implements SyncHelloPacket {

    public static final Type<ClientboundSyncHelloPacket> TYPE = WildfireGender.clientBoundPacket("hello");
    public static final StreamCodec<ByteBuf, ClientboundSyncHelloPacket> STREAM_CODEC = ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.collection(IntArrayList::new))
        .map(ClientboundSyncHelloPacket::new, ClientboundSyncHelloPacket::version);

    public ClientboundSyncHelloPacket(int version) {
        this(IntArrayList.of(version));
    }

    public ClientboundSyncHelloPacket() {
        this(VERSION);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /// @apiNote Only call on the client side
    public void handle(Consumer<ServerboundSyncHelloPacket> replySender, IntConsumer versionSetter) {
        // could be changed in the future to also accept a close enough protocol if we have support
        // for one of the versions the server accepts
        replySender.accept(new ServerboundSyncHelloPacket());
        if(version.isEmpty()) {
            WildfireGender.LOGGER.error(WildfireSync.MARKER, "Server sent an empty supported version list");
            versionSetter.accept(-1);
            return;
        }

        int closest = -1;
        for(int version : this.version) {
            if(version == VERSION) {
                WildfireGender.LOGGER.info(WildfireSync.MARKER, "Received exact protocol version match from server (version {})", version);
                versionSetter.accept(version);
                return;
            }

            if(version > VERSION) {
                WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Server declares support for protocol version {} which is newer than what we're using ({}), ignoring it", version, VERSION);
                continue;
            }

            if(version > closest) {
                WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Server declares support for protocol version {}", version);
                closest = version;
            }
        }

        if(closest == -1) {
            WildfireGender.LOGGER.warn(WildfireSync.MARKER, "Server only declares support for newer sync protocol versions than we support");
        } else {
            WildfireGender.LOGGER.warn(WildfireSync.MARKER, "Server declares support for sync protocol version {} but we expect {}", closest, VERSION);
        }
        versionSetter.accept(closest);
    }
}

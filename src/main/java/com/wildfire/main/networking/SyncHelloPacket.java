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
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Function;

/**
 * <p>Packet sent upon joining a server that supports it, identifying the sync packet version used by the mod.</p>
 *
 * <p>While we currently only use this to print some messages to the game logs, this is primarily intended
 * for use by third-party sync implementations to aid in supporting multiple versions.</p>
 *
 * @since 5.0.0-Beta.2
 */
public sealed interface SyncHelloPacket extends CustomPacketPayload {
    /*static*/ int VERSION = 1;

    int version();

    static <T extends SyncHelloPacket> StreamCodec<ByteBuf, T> codec(Function<Integer, T> constructor) {
        return StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SyncHelloPacket::version,
                constructor
        );
    }

    // TODO either split these apart into multiple classes to match the sync packets,
    //      or merge the sync packet classes to work similarly to this?
    record Clientbound(int version) implements SyncHelloPacket {
        public Clientbound() {
            this(VERSION);
        }

        public static final Type<Clientbound> ID = new CustomPacketPayload.Type<>(WildfireGender.id("clientbound/hello"));
        public static final StreamCodec<ByteBuf, Clientbound> CODEC = codec(Clientbound::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        @SuppressWarnings("unused")
        @Environment(EnvType.CLIENT)
        public void handle(ClientPlayNetworking.Context context) {
            WildfireSync.LOGGER.info("Received hello response from server with protocol version {}", version);
            if(version != VERSION) {
                WildfireSync.LOGGER.warn("Sync version mismatch; network errors will likely occur! (our sync version is {})", VERSION);
            }
        }
    }

    record Serverbound(int version) implements SyncHelloPacket {
        public Serverbound() {
            this(VERSION);
        }

        public static final Type<Serverbound> ID = new CustomPacketPayload.Type<>(WildfireGender.id("serverbound/hello"));
        public static final StreamCodec<ByteBuf, Serverbound> CODEC = codec(Serverbound::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        public void handle(ServerPlayNetworking.Context context) {
            WildfireSync.LOGGER.info("Received hello from player {} using sync protocol version {}", context.player().getUUID(), version);
            // note that while the only action the client performs upon receiving this response is printing some messages
            // to the game logs, this should still be treated as a required response to any client that sends it
            // if the server supports it.
            context.responseSender().sendPacket(new Clientbound());
        }
    }
}

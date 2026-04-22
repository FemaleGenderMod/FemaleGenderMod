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
import com.wildfire.main.networking.WildfireSync;
import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.TriState;

/// Packet sent upon joining a server that supports it, identifying the sync packet version used by the mod.
///
/// While we currently only use this to print some messages to the game logs, this is primarily intended
/// for use by third-party sync implementations to aid in supporting multiple versions.
///
/// @since 5.0.0-Beta.2
public sealed interface SyncHelloPacket extends CustomPacketPayload {
    /// Denotes the current sync protocol version
    ///
    /// This version handshake is initiated by the connecting client, with the server only then responding
    /// with its own protocol version.
    ///
    /// If the server doesn't respond or responds with a different value, then the server is assumed to not support
    /// syncing over the current protocol, and will not send or receive any sync packets, and vice versa.
    ///
    /// | Protocol Version   | Changes                                                                                                                           |
    /// | ------------------ | --------------------------------------------------------------------------------------------------------------------------------- |
    /// | `1` (5.0.0-Beta.2) | Initial versioned protocol                                                                                                        |
    /// | `2` (TBD)          | Hello packet is now sent during configuration phase, sync packets are now identified as `clientbound/sync` and `serverbound/sync` |
    /*public static final*/ int VERSION = 2;//TODO: Do we want to try to make it so that if it detects version 1 it syncs using that format?

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
        public void handle(ClientConfigurationNetworking.Context context) {
            if(version == VERSION) {
                WildfireSync.LOGGER.info("Received hello response from server with protocol version {}", version);
                context.packetContext().set(WildfireSync.MATCHING_VERSION, TriState.TRUE);
            } else {
                WildfireSync.LOGGER.warn(
                    "Server reported an unsupported sync protocol version! Server supports version {} but we expect {}",
                    version, VERSION
                );
                context.packetContext().set(WildfireSync.MATCHING_VERSION, TriState.FALSE);
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

        public void handle(ServerConfigurationNetworking.Context context) {
            context.responseSender().sendPacket(new Clientbound());
            if(version == VERSION) {
                WildfireSync.LOGGER.info("Received hello packet from client with protocol version {}", version);
                context.packetContext().set(WildfireSync.MATCHING_VERSION, TriState.TRUE);
            } else {
                WildfireSync.LOGGER.warn(
                    "Client reported an unsupported sync protocol version! Client supports version {} but we expect {}",
                    version, VERSION
                );
                context.packetContext().set(WildfireSync.MATCHING_VERSION, TriState.FALSE);
            }
        }
    }
}

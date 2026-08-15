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

package com.wildfire.common.networking;

import com.wildfire.common.WildfireGender;
import com.wildfire.common.networking.packets.hello.SyncHelloPacket;
import com.wildfire.common.networking.packets.sync.ClientboundSyncPacket;
import com.wildfire.common.networking.packets.sync.ServerboundSyncPacket;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoNetworking implements WildfireNetworking {

    public static final AttributeKey<Integer> VERSION = AttributeKey.newInstance(WildfireGender.id("version").toString());

    @Override
    public boolean canSyncToPlayer(ServerPlayer player) {
        return player.connection.hasChannel(ClientboundSyncPacket.TYPE) && versionMatches(player.connection.connection);
    }

    @Override
    public boolean canSyncToServer(Connection connection) {
        if (connection.getPacketListener() instanceof ICommonPacketListener listener) {
            return listener.hasChannel(ServerboundSyncPacket.TYPE) && versionMatches(connection);
        }
        return false;
    }

    @Override
    public boolean versionMatches(final Connection connection) {
        Attribute<Integer> version = connection.channel().attr(VERSION);
        return version != null && version.get() == SyncHelloPacket.VERSION;
    }

    @Override
    public void syncToPlayer(final ServerPlayer sendTo, final ClientboundSyncPacket packet) {
        PacketDistributor.sendToPlayer(sendTo, packet);
    }

    @Override
    public void syncToServer(final ServerboundSyncPacket packet) {
        ClientPacketDistributor.sendToServer(packet);
    }

    @Override
    public Collection<ServerPlayer> playersTracking(final Entity entity) {
        if (!entity.level().isClientSide() && entity.level().getChunkSource() instanceof ServerChunkCache chunkCache) {
            return chunkCache.chunkMap.getPlayersWatching(entity);
        }
        return Collections.emptyList();
    }
}

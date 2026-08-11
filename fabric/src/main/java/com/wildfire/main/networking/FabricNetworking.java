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
import com.wildfire.main.networking.packets.hello.SyncHelloPacket;
import com.wildfire.main.networking.packets.sync.ClientboundSyncPacket;
import com.wildfire.main.networking.packets.sync.ServerboundSyncPacket;
import java.util.Collection;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class FabricNetworking implements WildfireNetworking {

    public static final PacketContext.Key<Integer> VERSION = PacketContext.key(WildfireGender.id("version"));

    @Override
    public boolean canSyncToPlayer(ServerPlayer player) {
        return ServerPlayNetworking.canSend(player, ClientboundSyncPacket.TYPE) && versionMatches(player.connection.connection);
    }

    @Override
    public boolean canSyncToServer(Connection connection) {
        return ClientPlayNetworking.canSend(ServerboundSyncPacket.TYPE) && versionMatches(connection);
    }

    @Override
    public boolean versionMatches(final Connection connection) {
        Integer version = connection.getPacketContext().get(VERSION);
        return version != null && version == SyncHelloPacket.VERSION;
    }

    @Override
    public void syncToPlayer(final ServerPlayer sendTo, final ClientboundSyncPacket packet) {
        ServerPlayNetworking.send(sendTo, packet);
    }

    @Override
    public void syncToServer(final ServerboundSyncPacket packet) {
        ClientPlayNetworking.send(packet);
    }

    @Override
    public Collection<ServerPlayer> playersTracking(Entity toSync) {
        return PlayerLookup.tracking(toSync);
    }
}

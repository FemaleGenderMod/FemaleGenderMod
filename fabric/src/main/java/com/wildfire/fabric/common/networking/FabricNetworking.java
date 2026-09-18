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

package com.wildfire.fabric.common.networking;

import com.wildfire.common.WildfireGender;
import com.wildfire.common.networking.WildfireNetworking;
import com.wildfire.common.networking.packets.hello.SyncHelloPacket;
import java.util.Collection;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class FabricNetworking implements WildfireNetworking {

    public static final PacketContext.Key<Integer> VERSION = PacketContext.key(WildfireGender.id("version"));

    @Override
    public boolean canSendToPlayer(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        return ServerPlayNetworking.canSend(player, type) && versionMatches(player.connection.connection);
    }

    @Override
    public boolean canSendToServer(Connection connection, CustomPacketPayload.Type<?> type) {
        return ClientPlayNetworking.canSend(type) && versionMatches(connection);
    }

    @Override
    public boolean versionMatches(final Connection connection) {
        Integer version = connection.getPacketContext().get(VERSION);
        return version != null && version == SyncHelloPacket.VERSION;
    }

    @Override
    public void sendToClient(final ServerPlayer sendTo, final CustomPacketPayload packet) {
        ServerPlayNetworking.send(sendTo, packet);
    }

    @Override
    public void sendToServer(final CustomPacketPayload packet) {
        ClientPlayNetworking.send(packet);
    }

    @Override
    public Collection<ServerPlayer> playersTracking(Entity toSync) {
        return PlayerLookup.tracking(toSync);
    }
}

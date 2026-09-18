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

import com.wildfire.common.WildfireHelper;
import java.util.Collection;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface WildfireNetworking {

    WildfireNetworking INSTANCE = WildfireHelper.getService(WildfireNetworking.class);

    boolean canSendToPlayer(ServerPlayer player, CustomPacketPayload.Type<?> type);

    /// @apiNote Only call on the client
    boolean canSendToServer(Connection connection, CustomPacketPayload.Type<?> type);

    boolean versionMatches(Connection connection);

    void sendToClient(ServerPlayer sendTo, CustomPacketPayload packet);

    void sendToServer(CustomPacketPayload packet);

    Collection<ServerPlayer> playersTracking(Entity entity);
}

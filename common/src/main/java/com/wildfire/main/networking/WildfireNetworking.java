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

import com.wildfire.main.WildfireHelper;
import com.wildfire.main.networking.packets.sync.ClientboundSyncPacket;
import com.wildfire.main.networking.packets.sync.ServerboundSyncPacket;
import java.util.Collection;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface WildfireNetworking {

    WildfireNetworking INSTANCE = WildfireHelper.getService(WildfireNetworking.class);

    boolean canSyncToPlayer(ServerPlayer player);

    /// @apiNote Only call on the client
    boolean canSyncToServer(Connection connection);

    boolean versionMatches(Connection connection);

    void syncToPlayer(ServerPlayer sendTo, ClientboundSyncPacket packet);

    void syncToServer(ServerboundSyncPacket packet);

    Collection<ServerPlayer> playersTracking(Entity entity);
}

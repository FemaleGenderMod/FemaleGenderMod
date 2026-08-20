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

package com.wildfire.common;

import com.wildfire.common.entitydata.BreastDataComponent;
import com.wildfire.common.entitydata.PlayerConfigHolder;
import com.wildfire.common.networking.WildfireSync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class WildfireEventHandler {
    private WildfireEventHandler() {
        throw new UnsupportedOperationException();
    }

    /// Removes a disconnecting player from the cache on a server
    public static void playerDisconnected(Player player) {
        WildfireGender.CACHE.invalidate(player.getUUID());
    }

    /// Send a sync packet when a player enters the render distance of another player
    public static void onBeginTracking(Entity tracked, ServerPlayer syncTo) {
        if(tracked instanceof Player toSync) {
            PlayerConfigHolder genderToSync = WildfireGender.getPlayerById(toSync.getUUID());
            if(genderToSync == null) return;
            // Note that we intentionally don't check if we've previously synced a player with this code path;
            // because we use entity tracking to sync, it's entirely possible that one player would leave the
            // tracking distance of another, change their settings, and then re-enter their tracking distance;
            // we wouldn't sync while they're out of tracking distance, and as such, their settings would be out
            // of sync until they relog.
            WildfireSync.sendToClient(syncTo, genderToSync);
        }
    }

    /// Apply player settings to chestplates equipped onto armor stands
    public static void onEquipArmorStand(Player player, ItemStack item) {
        PlayerConfigHolder playerConfig = WildfireGender.getPlayerById(player.getUUID());
        if(playerConfig == null) {
            // while we shouldn't have our tag on the stack still, we're still checking to catch any armor
            // that may still have the tag from older versions, or from potential cross-mod interactions
            // which allow for removing items from armor stands without calling the vanilla
            // #equip and/or #onBreak methods
            BreastDataComponent.removeFromStack(item);
            return;
        }

        // Note that we always attach player data to the item stack as a server has no concept of resource packs,
        // making it impossible to compare against any armor data that isn't registered through the mod API.
        BreastDataComponent component = BreastDataComponent.fromPlayer(player, playerConfig);
        if(component != null) {
            component.write(item);
        }
    }
}

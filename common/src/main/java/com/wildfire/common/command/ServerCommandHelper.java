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

package com.wildfire.common.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;

public interface ServerCommandHelper<SOURCE extends SharedSuggestionProvider> extends CommandHelper<SOURCE> {
    Permission SERVER_COMMAND = Permissions.COMMANDS_GAMEMASTER;
    Permission DEBUG_COMMANDS = Permissions.COMMANDS_GAMEMASTER;

    MinecraftServer getServer(SOURCE source);
    ServerPlayer getPlayer(SOURCE source) throws CommandSyntaxException;

    default boolean hasCommandPermission(SOURCE source) {
        return source.permissions().hasPermission(SERVER_COMMAND);
    }

    default boolean hasDebugCommandPermission(SOURCE source) {
        return source.permissions().hasPermission(DEBUG_COMMANDS);
    }

    Entity resolveSingleEntityArgument(CommandContext<SOURCE> ctx, String name) throws CommandSyntaxException;
}

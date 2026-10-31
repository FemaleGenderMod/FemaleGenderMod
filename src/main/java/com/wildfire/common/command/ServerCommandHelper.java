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

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Predicate;

public interface ServerCommandHelper extends CommandHelper<CommandSourceStack> {
    /// The vanilla permission level required for the mod's server commands
    int COMMAND_PERMISSION_LEVEL = 2;

    CommandPermission COMMAND_PERMISSION = new CommandPermission("command", COMMAND_PERMISSION_LEVEL);

    ServerPlayer getPlayer(CommandSourceStack source) throws CommandSyntaxException;

    /// Returns a [Predicate] to determine if the provided [command source][CommandSourceStack] has
    /// the given [permissions][CommandPermission] to use a command
    ///
    /// Use of this is recommended over checking a vanilla permission level to allow for future
    /// support of a permissions API.
    default Predicate<CommandSourceStack> requirePermissions(CommandPermission permission) {
        return source -> source.hasPermission(permission.level());
    }

    // TODO extract this out to its own class if/when we support a proper permissions API (e.g. Neo's / LuckPerms / etc.)
    record CommandPermission(String key, int level) {
    }
}

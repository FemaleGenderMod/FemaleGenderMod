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

package com.wildfire.client;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.wildfire.client.command.ClientCommandHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class NeoCommandHelper implements ClientCommandHelper<CommandSourceStack> {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder(final String key) {
        return Commands.literal(key);
    }

    @Override
    public <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(final String key, final ArgumentType<T> type) {
        return Commands.argument(key, type);
    }

    @Override
    public void sendSystemMessage(final CommandSourceStack source, final Component message) {
        source.sendSystemMessage(message);
    }

    @Override
    public void sendFailure(final CommandSourceStack source, final Component message) {
        source.sendFailure(message);
    }

    @Override
    public Level getLevel(final CommandSourceStack source) {
        return source.getUnsidedLevel();
    }

    @Override
    public LocalPlayer getPlayer(final CommandSourceStack source) {
        //TODO - Neo: Validate this
        if (source.getEntity() instanceof LocalPlayer player) {
            return player;
        }
        return getMinecraft(source).player;
    }

    @Override
    public Minecraft getMinecraft(final CommandSourceStack source) {
        return Minecraft.getInstance();
    }
}

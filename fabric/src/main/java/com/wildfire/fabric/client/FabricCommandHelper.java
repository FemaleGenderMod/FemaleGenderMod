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

package com.wildfire.fabric.client;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.wildfire.client.command.ClientCommandHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class FabricCommandHelper implements ClientCommandHelper<FabricClientCommandSource> {

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> literalArgumentBuilder(final String key) {
        return ClientCommands.literal(key);
    }

    @Override
    public <T> RequiredArgumentBuilder<FabricClientCommandSource, T> argument(final String key, final ArgumentType<T> type) {
        return ClientCommands.argument(key, type);
    }

    @Override
    public void sendSystemMessage(final FabricClientCommandSource source, final Component message) {
        source.sendFeedback(message);
    }

    @Override
    public void sendFailure(final FabricClientCommandSource source, final Component message) {
        source.sendError(message);
    }

    @Override
    public Level getLevel(final FabricClientCommandSource source) {
        return source.getLevel();
    }

    @Override
    public LocalPlayer getPlayer(final FabricClientCommandSource source) {
        return source.getPlayer();
    }

    @Override
    public Minecraft getMinecraft(final FabricClientCommandSource source) {
        return source.getClient();
    }
}

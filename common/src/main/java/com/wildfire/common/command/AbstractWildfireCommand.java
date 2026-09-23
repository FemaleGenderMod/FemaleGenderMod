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
import com.wildfire.client.command.ClientCommandHelper;
import com.wildfire.common.WildfireLang;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.UnknownNullability;

import static com.wildfire.common.WildfireGender.COMMAND_PREFIX;

public abstract class AbstractWildfireCommand<T extends CommandHelper<S>, S extends SharedSuggestionProvider> {
    protected final T helper;

    protected AbstractWildfireCommand(T helper) {
        this.helper = helper;
    }

    protected void send(CommandContext<S> ctx, Component text) {
        helper.sendSystemMessage(ctx.getSource(), WildfireLang.GENERIC_SPACE.translate(COMMAND_PREFIX, text));
    }

    @SuppressWarnings("SameParameterValue")
    @UnknownNullability("nullability depends on the relevant ArgumentType & defaultValue")
    protected <V> V getOrDefault(
        CommandContext<S> ctx,
        String name,
        @UnknownNullability V defaultValue,
        Class<V> clazz
    ) {
        V value = defaultValue;
        try {
            value = ctx.getArgument(name, clazz);
        } catch (IllegalArgumentException _) {
        }
        return value;
    }

    @SuppressWarnings("SameParameterValue")
    protected void sendHelp(CommandContext<S> ctx, WildfireLang header, WildfireLang... commands) {
        List<Component> lines = new ArrayList<>();
        lines.add(WildfireLang.GENERIC_SPACE.translate(COMMAND_PREFIX, header.translate().withStyle(style -> style.withUnderlined(true))));

        for (WildfireLang langEntry : commands) {
            lines.add(WildfireLang.GENERIC_SPACE.translate(COMMAND_PREFIX, WildfireLang.GENERIC_DASH_EXPLANATION.translateColored(TextColor.GRAY,
                langEntry.translateColored(TextColor.AQUA),
                langEntry.translateDescription().withColor(TextColor.WHITE)
            )));

        }

        helper.sendSystemMessage(ctx.getSource(), ComponentUtils.formatList(lines, CommonComponents.NEW_LINE));
    }
}

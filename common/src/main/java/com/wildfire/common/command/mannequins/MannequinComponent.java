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

package com.wildfire.common.command.mannequins;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wildfire.common.WildfireLang;
import com.wildfire.common.command.ServerCommandHelper;
import com.wildfire.common.config.value.ConfigValue;
import com.wildfire.common.entities.avatars.MannequinConfigHolder;
import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/*package-private*/ final class MannequinComponent<T> {
    private final Component name;
    private final ArgumentType<?> argument;
    private final Parser<T> parser;
    private final ConfigValueGetter<T> value;
    private final @Nullable SuggestionProvider<CommandSourceStack> suggestionProvider;

    private MannequinComponent(
        Component name,
        ArgumentType<?> argument,
        ConfigValueGetter<T> value,
        Parser<T> parser,
        @Nullable SuggestionProvider<CommandSourceStack> suggestionProvider
    ) {
        this.name = name;
        this.argument = argument;
        this.value = value;
        this.parser = parser;
        this.suggestionProvider = suggestionProvider;
    }

    static <T> Builder<T> builder() {
        return new Builder<>();
    }

    static <T> Builder<T> builder(Class<T> type) {
        return new Builder<T>().parser(type);
    }

    public Component name() {
        return name;
    }

    public Object value(MannequinConfigHolder config) {
        Object value = this.value.get(config).get();
        // quick and dirty fix for booleans displaying as 1/0
        if(value instanceof Boolean bool) {
            return Boolean.toString(bool);
        }
        return value;
    }

    public RequiredArgumentBuilder<CommandSourceStack, ?> argument(ServerCommandHelper helper, String name) {
        return helper.argument(name, argument)
            // this is safe to pass a possibly null value to as it simply sets a field which is null by default
            .suggests(suggestionProvider);
    }

    public boolean update(MannequinConfigHolder config, CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        T input = parser.parse(ctx, name);
        ConfigValue<T> value = this.value.get(config);
        if(Objects.equals(value.get(), input)) {
            return false;
        }
        return value.update(input);
    }

    @FunctionalInterface
    /*package-private*/ interface Parser<T> {
        static <T> Parser<T> of(final Class<T> type) {
            return (ctx, name) -> ctx.getArgument(name, type);
        }

        T parse(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException;
    }

    @FunctionalInterface
    /*package-private*/ interface ConfigValueGetter<T> {
        ConfigValue<T> get(MannequinConfigHolder config);
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    /*package-private*/ static class Builder<T> {
        private ArgumentType<?> argument;
        private Component name;
        private Parser<T> parser;
        private ConfigValueGetter<T> getter;
        private @Nullable SuggestionProvider<CommandSourceStack> suggestionProvider = null;

        public Builder<T> argument(ArgumentType<?> argument) {
            this.argument = argument;
            return this;
        }

        public Builder<T> name(Component name) {
            this.name = name;
            return this;
        }

        public Builder<T> name(WildfireLang name) {
            return name(name.translate());
        }

        public Builder<T> getter(ConfigValueGetter<T> getter) {
            this.getter = getter;
            return this;
        }

        public Builder<T> parser(Parser<T> parser) {
            this.parser = parser;
            return this;
        }

        public Builder<T> parser(Class<T> type) {
            this.parser = Parser.of(type);
            return this;
        }

        public Builder<T> suggests(SuggestionProvider<CommandSourceStack> suggestionProvider) {
            this.suggestionProvider = suggestionProvider;
            return this;
        }

        public MannequinComponent<T> build() {
            Preconditions.checkNotNull(argument, "Argument type must be set");
            Preconditions.checkNotNull(name, "Name must be set");
            Preconditions.checkNotNull(parser, "Parser must be set");
            Preconditions.checkNotNull(getter, "Getter must be set");
            return new MannequinComponent<>(name, argument, getter, parser, suggestionProvider);
        }
    }
}

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

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wildfire.api.Gender;
import com.wildfire.common.WildfireLang;
import com.wildfire.common.command.ServerCommandHelper;
import com.wildfire.common.config.validator.ConfigRange;
import com.wildfire.common.config.value.ConfigKey;
import com.wildfire.common.config.value.ConfigValue;
import com.wildfire.common.entities.Breasts;
import com.wildfire.common.entities.avatars.AbstractAvatarConfigHolder;
import com.wildfire.common.entities.avatars.MannequinConfigHolder;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jspecify.annotations.Nullable;

public interface MannequinComponent<A, T> {
    GenderComponent GENDER = new GenderComponent();

    FloatComponent BREAST_SIZE = new FloatComponent(Breasts.BUST_SIZE, config -> config.breasts().bustSize());
    FloatComponent BREAST_OFFSET_X = new FloatComponent(Breasts.BREASTS_OFFSET_X, config -> config.breasts().xOffset());
    FloatComponent BREAST_OFFSET_Y = new FloatComponent(Breasts.BREASTS_OFFSET_Y, config -> config.breasts().yOffset());
    FloatComponent BREAST_OFFSET_Z = new FloatComponent(Breasts.BREASTS_OFFSET_Z, config -> config.breasts().zOffset());
    FloatComponent BREAST_CLEAVAGE = new FloatComponent(Breasts.BREASTS_CLEAVAGE, config -> config.breasts().cleavage());
    BoolComponent SHOW_IN_ARMOR = new BoolComponent(AbstractAvatarConfigHolder::showBreastsInArmor);

    BoolComponent PHYSICS = new BoolComponent(config -> config.breasts().physics().enabled());
    FloatComponent PHYSICS_BOUNCE = new FloatComponent(Breasts.Physics.BOUNCE_MULTIPLIER, config -> config.breasts().physics().bounceMultiplier());
    FloatComponent PHYSICS_FLOPPY = new FloatComponent(Breasts.Physics.FLOPPINESS, config -> config.breasts().physics().floppiness());
    BoolComponent PHYSICS_UNIBOOB = new BoolComponent(config -> config.breasts().physics().uniboob());

    ArgumentType<A> argument();
    <S extends SharedSuggestionProvider> T parse(CommandContext<S> ctx, String name, ServerCommandHelper<S> helper) throws CommandSyntaxException;

    void update(MannequinConfigHolder config, T value);

    default <S extends SharedSuggestionProvider> @Nullable SuggestionProvider<S> suggestionProvider() {
        return null;
    }

    class GenderComponent implements MannequinComponent<String, Gender> {
        private static final DynamicCommandExceptionType INVALID_INPUT = new DynamicCommandExceptionType(
            WildfireLang.COMMAND_SERVER_INVALID_GENDER::translate);

        @Override
        public ArgumentType<String> argument() {
            return StringArgumentType.word();
        }

        @Override
        public <S extends SharedSuggestionProvider> Gender parse(final CommandContext<S> ctx, final String name, final ServerCommandHelper<S> helper) throws CommandSyntaxException {
            String input = StringArgumentType.getString(ctx, name);
            try {
                return Gender.valueOf(input.toUpperCase(Locale.ROOT));
            } catch(IllegalArgumentException _) {
                throw INVALID_INPUT.create(input);
            }
        }

        @Override
        public void update(final MannequinConfigHolder config, final Gender value) {
            config.gender().accept(value);
        }

        @Override
        public <S extends SharedSuggestionProvider> SuggestionProvider<S> suggestionProvider() {
            return new GenderSuggestionProvider<>();
        }
    }

    class FloatComponent implements MannequinComponent<Float, Float> {
        private final ArgumentType<Float> argument;
        private final Function<MannequinConfigHolder, ConfigValue<Float>> valueGetter;

        public FloatComponent(final ConfigKey<Float> key, final Function<MannequinConfigHolder, ConfigValue<Float>> value) {
            if(key.validator() instanceof ConfigRange<Float>(Float minInclusive, Float maxInclusive)) {
                argument = FloatArgumentType.floatArg(minInclusive, maxInclusive);
            } else {
                argument = FloatArgumentType.floatArg();
            }
            this.valueGetter = value;
        }

        @Override
        public ArgumentType<Float> argument() {
            return argument;
        }

        @Override
        public <S extends SharedSuggestionProvider> Float parse(final CommandContext<S> ctx, final String name, final ServerCommandHelper<S> helper) {
            return FloatArgumentType.getFloat(ctx, name);
        }

        @Override
        public void update(final MannequinConfigHolder config, final Float value) {
            valueGetter.apply(config).accept(value);
        }
    }

    class BoolComponent implements MannequinComponent<Boolean, Boolean> {
        private final Function<MannequinConfigHolder, ConfigValue<Boolean>> valueGetter;

        public BoolComponent(final Function<MannequinConfigHolder, ConfigValue<Boolean>> value) {
            this.valueGetter = value;
        }

        @Override
        public ArgumentType<Boolean> argument() {
            return BoolArgumentType.bool();
        }

        @Override
        public <S extends SharedSuggestionProvider> Boolean parse(final CommandContext<S> ctx, final String name, final ServerCommandHelper<S> helper) {
            return BoolArgumentType.getBool(ctx, name);
        }

        @Override
        public void update(final MannequinConfigHolder config, final Boolean value) {
            valueGetter.apply(config).accept(value);
        }
    }
}

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

package com.wildfire.main.config.value;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.wildfire.main.config.validator.ConfigRange;
import com.wildfire.main.config.validator.ConfigValidator;
import com.wildfire.main.uvs.UVLayout;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public record ConfigKey<TYPE>(String key, Either<TYPE, Supplier<TYPE>> defaultValueSupplier, MapCodec<TYPE> codec, MapCodec<TYPE> codecOrDefault, ConfigValidator<TYPE> validator) {

    public TYPE defaultValue() {
        return defaultValueSupplier.map(Function.identity(), Supplier::get);
    }

    @Contract("null -> false")
    public boolean validate(@Nullable TYPE value) {
        return value != null && validator.validate(value);
    }

    public ConfigValue<TYPE> createValueHandler(TYPE value) {
        return new ConfigValue<>(this, value);
    }

    private static final ConfigValidator<?> ALWAYS_TRUE = _ -> true;

    @SuppressWarnings("unchecked")
    public static <TYPE> ConfigValidator<TYPE> alwaysTrueValidator() {
        return (ConfigValidator<TYPE>) ALWAYS_TRUE;
    }

    public static <TYPE> ConfigKey<TYPE> create(String key, TYPE defaultValue, Codec<TYPE> baseCodec) {
        return create(key, defaultValue, baseCodec, alwaysTrueValidator());
    }

    public static <TYPE> ConfigKey<TYPE> create(String key, TYPE defaultValue, Codec<TYPE> baseCodec, ConfigValidator<TYPE> range) {
        MapCodec<TYPE> codec = baseCodec.fieldOf(key);
        return new ConfigKey<>(key, Either.left(defaultValue), codec, codec.orElse(error -> {
            //TODO: Test this logging message (if we want it), also do we want the promote partial to be here instead of just for floats??
            //WildfireGender.LOGGER.warn("{}. Falling back to default value: {}", error, defaultValue);
        }, defaultValue), range);
    }

    public static <TYPE> ConfigKey<TYPE> create(String key, Supplier<TYPE> defaultValueSupplier, Codec<TYPE> baseCodec, ConfigValidator<TYPE> range) {
        MapCodec<TYPE> codec = baseCodec.fieldOf(key);
        return new ConfigKey<>(key, Either.right(defaultValueSupplier), codec, codec.orElseGet(error -> {
            //TODO: Test this logging message (if we want it), also do we want the promote partial to be here instead of just for floats??
            //WildfireGender.LOGGER.warn("{}. Falling back to default value: {}", error, defaultValueSupplier.get());
        }, defaultValueSupplier), range);
    }

    public static ConfigKey<UVLayout> create(String key, Supplier<UVLayout> defaultValueSupplier) {
        return create(key, defaultValueSupplier, UVLayout.MUTABLE_CONFIG_CODEC, alwaysTrueValidator());
    }

    public static ConfigKey<Boolean> create(String key, boolean defaultValue) {
        return create(key, defaultValue, Codec.BOOL);
    }

    public static ConfigKey<Float> create(String key, float defaultValue, float minInclusive, float maxInclusive) {
        ConfigRange<Float> configRange = new ConfigRange<>(minInclusive, maxInclusive);
        //Codec is based on ExtraCodecs#floatRange, but sets the clamped value as a partial value, and then promotes it
        return create(key, defaultValue, Codec.FLOAT.validate(value -> {
            if (configRange.validate(value)) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> "Value must be within range [" + minInclusive + ";" + maxInclusive + "]: " + value, Math.clamp(value, minInclusive, maxInclusive));
        }).promotePartial(_ -> {//TODO: Do we want to log that it was invalid and we promoted the clamped value instead?
        }), configRange);
    }
}

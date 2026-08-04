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

package com.wildfire.main.config.types;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.wildfire.main.uvs.UVLayout;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public record ConfigKey<TYPE>(String key, Either<TYPE, Supplier<TYPE>> defaultValueSupplier, MapCodec<TYPE> codec, MapCodec<TYPE> codecOrDefault, @Nullable ConfigRange<TYPE> range) {

    public TYPE defaultValue() {
        return defaultValueSupplier.map(Function.identity(), Supplier::get);
    }

    public boolean validate(TYPE value) {
        return range == null || range.validate(value);
    }

    public static <TYPE> ConfigKey<TYPE> create(String key, TYPE defaultValue, Codec<TYPE> baseCodec) {
        return create(key, defaultValue, baseCodec, null);
    }

    public static <TYPE> ConfigKey<TYPE> create(String key, TYPE defaultValue, Codec<TYPE> baseCodec, @Nullable ConfigRange<TYPE> range) {
        MapCodec<TYPE> codec = baseCodec.fieldOf(key);
        return new ConfigKey<>(key, Either.left(defaultValue), codec, codec.orElse(error -> {
            //TODO: Test this logging message (if we want it), also do we want the promote partial to be here instead of just for floats??
            //WildfireGender.LOGGER.warn("{}. Falling back to default value: {}", error, defaultValue);
        }, defaultValue), range);
    }

    public static <TYPE> ConfigKey<TYPE> create(String key, Supplier<TYPE> defaultValueSupplier, Codec<TYPE> baseCodec, @Nullable ConfigRange<TYPE> range) {
        MapCodec<TYPE> codec = baseCodec.fieldOf(key);
        return new ConfigKey<>(key, Either.right(defaultValueSupplier), codec, codec.orElseGet(error -> {
            //TODO: Test this logging message (if we want it), also do we want the promote partial to be here instead of just for floats??
            //WildfireGender.LOGGER.warn("{}. Falling back to default value: {}", error, defaultValueSupplier.get());
        }, defaultValueSupplier), range);
    }

    public static ConfigKey<UVLayout> create(String key, Supplier<UVLayout> defaultValueSupplier) {
        return create(key, defaultValueSupplier, UVLayout.MUTABLE_CONFIG_CODEC, null);
    }

    public static ConfigKey<Boolean> create(String key, boolean defaultValue) {
        return create(key, defaultValue, Codec.BOOL);
    }

    public static ConfigKey<Float> create(String key, float defaultValue) {
        //Note: Float.MIN_VALUE is smallest possible positive float
        return create(key, defaultValue, -Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public static ConfigKey<Float> create(String key, float defaultValue, float minInclusive, float maxInclusive) {
        //Codec is based on ExtraCodecs#floatRange, but sets the clamped value as a partial value, and then promotes it
        return create(key, defaultValue, Codec.FLOAT.validate(value -> {
            if (value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> "Value must be within range [" + minInclusive + ";" + maxInclusive + "]: " + value, Math.clamp(value, minInclusive, maxInclusive));
        }).promotePartial(_ -> {//TODO: Do we want to log that it was invalid and we promoted the clamped value instead?
        }), new ConfigRange<>(minInclusive, maxInclusive, Float::compareTo));
    }
}

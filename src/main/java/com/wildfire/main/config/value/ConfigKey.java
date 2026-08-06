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
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public record ConfigKey<TYPE>(Either<TYPE, Supplier<TYPE>> defaultValueSupplier, Codec<TYPE> codec, ConfigValidator<TYPE> validator) {

    private static final ConfigValidator<?> ALWAYS_TRUE = new ConfigValidator<>() {
        @Override
        public boolean validate(final Object value) {
            return false;
        }

        @Override
        public DataResult<Object> codecValidation(Object value) {
            return DataResult.success(value);
        }

        @Override
        public boolean hasCodecValidation() {
            return false;
        }
    };

    @SuppressWarnings("unchecked")
    public static <TYPE> ConfigValidator<TYPE> alwaysTrueValidator() {
        return (ConfigValidator<TYPE>) ALWAYS_TRUE;
    }

    public static final ConfigKey<Boolean> DEFAULT_TRUE = new ConfigKey<>(true, Codec.BOOL);
    public static final ConfigKey<Boolean> DEFAULT_FALSE = new ConfigKey<>(false, Codec.BOOL);

    public ConfigKey {
        if (validator.hasCodecValidation()) {
            codec = codec.validate(validator::codecValidation).promotePartial(_ -> {
                //TODO: Do we want to log that it was invalid and we promoted the clamped value instead?
            });
        }
    }

    public ConfigKey(TYPE defaultValue, Codec<TYPE> baseCodec) {
        this(defaultValue, baseCodec, alwaysTrueValidator());
    }

    public ConfigKey(TYPE defaultValue, Codec<TYPE> baseCodec, ConfigValidator<TYPE> range) {
        this(Either.left(defaultValue), baseCodec, range);
    }

    public ConfigKey(Supplier<TYPE> defaultValueSupplier, Codec<TYPE> baseCodec) {
        this(defaultValueSupplier, baseCodec, alwaysTrueValidator());
    }

    public ConfigKey(Supplier<TYPE> defaultValueSupplier, Codec<TYPE> baseCodec, ConfigValidator<TYPE> range) {
        this(Either.right(defaultValueSupplier), baseCodec, range);
    }

    public TYPE defaultValue() {
        return defaultValueSupplier.map(Function.identity(), Supplier::get);
    }

    @Contract("null -> false")
    public boolean validate(@Nullable TYPE value) {
        return value != null && validator.validate(value);
    }

    public MapCodec<TYPE> codecOrDefault(String key) {
        //Note: We don't do any logging for when we fall back to the default values, as this is also the path used for constructing the default config objects
        return codec.fieldOf(key).orElseGet(this::defaultValue);
    }

    public ConfigValue<TYPE> createValueHandler(TYPE value) {
        return new ConfigValue<>(this, value);
    }

    public static ConfigKey<Float> create(float defaultValue, float minInclusive, float maxInclusive) {
        return new ConfigKey<>(defaultValue, Codec.FLOAT, new ConfigRange<>(minInclusive, maxInclusive));
    }
}

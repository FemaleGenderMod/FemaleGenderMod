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

package com.wildfire.main.config.validator;

import com.mojang.serialization.DataResult;
import java.util.function.Supplier;

public record ConfigRange<TYPE extends Comparable<TYPE>>(TYPE minInclusive, TYPE maxInclusive) implements ConfigValidator<TYPE> {

    @Override
    public boolean validate(TYPE value) {
        return value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataResult<TYPE> codecValidation(final TYPE value) {
        if (validate(value)) {
            return DataResult.success(value);
        }
        //Codec is based on ExtraCodecs#floatRange, but sets the clamped value as a partial value, and then promotes it
        Supplier<String> errorMessage = () -> "Value must be within range [" + minInclusive + ";" + maxInclusive + "]: " + value;
        if (value instanceof Float val) {
            return DataResult.error(errorMessage, (TYPE) (Float) Math.clamp(val, (Float) minInclusive, (Float) maxInclusive));
        }
        return DataResult.error(errorMessage);
    }
}

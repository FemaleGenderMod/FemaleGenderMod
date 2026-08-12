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

package com.wildfire.common.config.value;

import com.wildfire.common.config.validator.ConfigValidator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class KeyBackedConfigValue<TYPE> implements ConfigValue<TYPE> {

    private final ConfigKey<TYPE> key;
    private TYPE value;

    KeyBackedConfigValue(ConfigKey<TYPE> key, TYPE value) {
        this.key = key;
        assert key.validate(value);
        this.value = value;
    }

    @Override
    public ConfigValidator<TYPE> validator() {
        return key.validator();
    }

    @Override
    public TYPE get() {
        return value;
    }

    @Override
    public boolean reset() {
        TYPE defaultValue = this.key.defaultValue();
        if (!value.equals(defaultValue)) {
            this.value = defaultValue;
            return true;
        }
        return false;
    }

    @Override
    public boolean update(TYPE newValue) {
        if (validator().validate(newValue)) {
            this.value = newValue;
            return true;
        }
        //TODO: Do we care about having a logging message? If so where should we get the key name from
        //WildfireGender.LOGGER.warn("Failed to update config '{}' to value: {}", key.key(), value);
        return false;
    }

    @Override
    public String toString() {
        return get().toString();
    }
}

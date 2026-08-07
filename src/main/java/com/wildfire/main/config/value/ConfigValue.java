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

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ConfigValue<TYPE> implements Supplier<TYPE>, Consumer<TYPE> {

    public static final UnaryOperator<Boolean> TOGGLE = value -> !value;

    private final ConfigKey<TYPE> key;
    private TYPE value;

    ConfigValue(ConfigKey<TYPE> key, TYPE value) {
        this.key = key;
        assert key.validate(value);
        this.value = value;
    }

    public ConfigKey<TYPE> key() {
        return key;
    }

    @Override
    public TYPE get() {
        return value;
    }

    /// {@return `true` if the value was changed to the default value, `false` if it was already at the default value}
    public boolean reset() {
        TYPE defaultValue = this.key.defaultValue();
        if (!value.equals(defaultValue)) {
            this.value = defaultValue;
            return true;
        }
        return false;
    }

    public boolean update(UnaryOperator<TYPE> transformer) {
        return update(transformer.apply(value));
    }

    public boolean update(TYPE newValue) {
        if (key.validate(newValue)) {
            this.value = newValue;
            return true;
        }
        //TODO: Do we care about having a logging message? If so where should we get the key name from
        //WildfireGender.LOGGER.warn("Failed to update config '{}' to value: {}", key.key(), value);
        return false;
    }

    @Override
    public final void accept(TYPE newValue) {
        update(newValue);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

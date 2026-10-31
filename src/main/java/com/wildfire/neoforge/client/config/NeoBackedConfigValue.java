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

package com.wildfire.neoforge.client.config;

import com.wildfire.common.WildfireGender;
import com.wildfire.common.config.validator.ConfigValidator;
import com.wildfire.common.config.value.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoBackedConfigValue<TYPE> implements ConfigValue<TYPE> {

    private final ConfigValidator<TYPE> validator;
    private final ModConfigSpec.ConfigValue<TYPE> neoValue;

    NeoBackedConfigValue(ConfigValidator<TYPE> validator, ModConfigSpec.ConfigValue<TYPE> neoValue) {
        this.validator = validator;
        this.neoValue = neoValue;
    }

    @Override
    public ConfigValidator<TYPE> validator() {
        return validator;
    }

    @Override
    public TYPE get() {
        return neoValue.get();
    }

    @Override
    public boolean reset() {
        TYPE defaultValue = this.neoValue.getDefault();
        if (!defaultValue.equals(get())) {
            this.neoValue.set(defaultValue);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(TYPE newValue) {
        if (validator().validate(newValue)) {
            this.neoValue.set(newValue);
            return true;
        }
        WildfireGender.LOGGER.warn("Failed to update config '{}' to value: {}", String.join(".", neoValue.getPath()), newValue);
        return false;
    }

    @Override
    public String toString() {
        return get().toString();
    }
}

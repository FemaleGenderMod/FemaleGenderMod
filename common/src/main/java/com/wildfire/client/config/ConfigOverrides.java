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

package com.wildfire.client.config;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.common.config.value.ConfigKey;
import com.wildfire.common.config.value.ConfigValue;

//TODO: Do we want to rename disable rendering and disable sound replacement to not have disable in the field name?
public record ConfigOverrides(ConfigValue<Boolean> armorPhysics, ConfigValue<Boolean> disableRendering, ConfigValue<Boolean> disableSoundReplacement) {

    static final ConfigKey<Boolean> ARMOR_PHYSICS_OVERRIDE = ConfigKey.DEFAULT_FALSE;
    static final ConfigKey<Boolean> DISABLE_RENDERING = ConfigKey.DEFAULT_FALSE;
    static final ConfigKey<Boolean> DISABLE_SOUND_REPLACEMENT = ConfigKey.DEFAULT_FALSE;

    public static final MapCodec<ConfigOverrides> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ARMOR_PHYSICS_OVERRIDE.codecOrDefault("armor_physics_override").forGetter(config -> config.armorPhysics.get()),
        DISABLE_RENDERING.codecOrDefault("disable_rendering").forGetter(config -> config.disableRendering.get()),
        DISABLE_SOUND_REPLACEMENT.codecOrDefault("disable_sound_replacement").forGetter(config -> config.disableSoundReplacement.get())
    ).apply(instance, ConfigOverrides::new));

    public ConfigOverrides(boolean armorPhysics, boolean disableRendering, boolean disableSoundReplacement) {
        this(ARMOR_PHYSICS_OVERRIDE.createValueHandler(armorPhysics),
            DISABLE_RENDERING.createValueHandler(disableRendering),
            DISABLE_SOUND_REPLACEMENT.createValueHandler(disableSoundReplacement));
    }
}

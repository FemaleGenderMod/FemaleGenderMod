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

package com.wildfire.common.entities.armorstands;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.api.Gender;
import com.wildfire.common.config.UVs;
import com.wildfire.common.entities.Breasts;
import com.wildfire.common.entities.EntityConfig;

public class ArmorStandConfig extends EntityConfig {
    public static final Codec<ArmorStandConfig> CODEC = RecordCodecBuilder.create(instance -> codecGroup(instance)
        .apply(instance, ArmorStandConfig::new)
    );

    protected ArmorStandConfig(final Gender gender, final Breasts breasts, final UVs uvs) {
        super(gender, breasts, uvs);
    }

    public static ArmorStandConfig createDefault() {
        //Note: Theoretically this can never fail so it is safe to use getOrThrow as everything in the codec has orElse(default)
        return CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()).getOrThrow();
    }
}

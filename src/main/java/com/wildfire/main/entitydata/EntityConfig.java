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

package com.wildfire.main.entitydata;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.config.value.ConfigValue;
import com.wildfire.main.uvs.UVs;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

/// A stripped down version of a [`player's config`][PlayerConfig], intended for use with non-player entities.
///
/// Unlike players, this has very minimal configuration support.
///
/// Currently only used for [`armor stands`][ArmorStand], and as a superclass for [`player configs`][PlayerConfig].
public class EntityConfig  {

    /// @return `true` if the mod has support for the provided entity
    public static boolean isSupportedEntity(LivingEntity entity) {
        // TODO mannequins are not properly supported right now; this method only returns true to indicate that
        //        our rendering does technically support it, despite the fact that there is no way to properly utilize
        //        them without using janky workarounds.
        return entity instanceof Avatar || entity instanceof ArmorStand;
    }

    public static final Codec<EntityConfig> CODEC = RecordCodecBuilder.create(instance -> codecGroup(instance)
        .apply(instance, EntityConfig::new)
    );

    protected static <CONFIG extends EntityConfig> P3<Mu<CONFIG>, Gender, Breasts, UVs> codecGroup(Instance<CONFIG> instance) {
        return instance.group(
            Configuration.GENDER.codecOrDefault().forGetter(config -> config.gender.get()),
            Breasts.CODEC.forGetter(config -> config.breasts),
            //TODO: Should UVs be in player, or maybe avatar once that intermediary exists?
            UVs.CODEC.forGetter(config -> config.uvs)
        );
    }

    public final ConfigValue<Gender> gender;
    public final Breasts breasts;
    public final UVs uvs;

    // note: hurt sounds, armor physics override, and show in armor are not defined here, as they have no relevance
    // to entities, and are instead entirely in PlayerConfig

    protected EntityConfig(Gender gender, Breasts breasts, UVs uvs) {
        this.gender = Configuration.GENDER.createValueHandler(gender);
        this.breasts = breasts;
        this.uvs = uvs;
    }
}

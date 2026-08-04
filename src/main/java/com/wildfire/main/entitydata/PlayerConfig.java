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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.config.value.ConfigValue;
import com.wildfire.main.uvs.UVLayout;

/// A version of [EntityConfig] backed by a [Configuration] for use with players
public class PlayerConfig extends EntityConfig {

    //~ if >=26.2 'oldMcCodec()' -> 'codec()'
    public static final Codec<PlayerConfig> CODEC = codec();

    //? if >=26.2 {
    private static Codec<PlayerConfig> codec() {
        return RecordCodecBuilder.create(instance -> codecGroup(instance)
            .and(Configuration.HURT_SOUNDS.codecOrDefault().forGetter(config -> config.hurtSounds.get()))
            .and(Configuration.HOLIDAY_THEMES.codecOrDefault().forGetter(config -> config.holidayThemes.get()))
            .and(Configuration.SHOW_IN_ARMOR.codecOrDefault().forGetter(config -> config.showBreastsInArmor.get()))
            .apply(instance, PlayerConfig::new));
    }
    //?}
    //? if <26.2 {
    private static Codec<PlayerConfig> oldMcCodec() {
        return RecordCodecBuilder.create(instance -> instance.group(
            EntityConfig.MAP_CODEC.forGetter(config -> config),
            Configuration.HURT_SOUNDS.codecOrDefault().forGetter(config -> config.hurtSounds.get()),
            Configuration.HOLIDAY_THEMES.codecOrDefault().forGetter(config -> config.holidayThemes.get()),
            Configuration.SHOW_IN_ARMOR.codecOrDefault().forGetter(config -> config.showBreastsInArmor.get())
        ).apply(instance, PlayerConfig::new));
    }
    private PlayerConfig(EntityConfig cfg, boolean hurtSounds, boolean holidayThemes, boolean showBreastsInArmor) {
        super(cfg);
        this.hurtSounds = Configuration.HURT_SOUNDS.createValueHandler(hurtSounds);
        this.holidayThemes = Configuration.HOLIDAY_THEMES.createValueHandler(holidayThemes);
        this.showBreastsInArmor = Configuration.SHOW_IN_ARMOR.createValueHandler(showBreastsInArmor);
    }
    //?}

    public final ConfigValue<Boolean> hurtSounds;
    public final ConfigValue<Boolean> holidayThemes;
    public final ConfigValue<Boolean> showBreastsInArmor;

    public PlayerConfig(Gender gender, float bustSize, float voicePitch, Breasts breasts, boolean breastPhysics, float bounceMultiplier , float floppyMultiplier,
        UVLayout leftBreastUVLayout, UVLayout rightBreastUVLayout, UVLayout leftBreastOverlayUVLayout, UVLayout rightBreastOverlayUVLayout,
        boolean hurtSounds, boolean holidayThemes, boolean showBreastsInArmor) {
        super(gender, bustSize, voicePitch, breasts, breastPhysics, bounceMultiplier, floppyMultiplier, leftBreastUVLayout, rightBreastUVLayout,
            leftBreastOverlayUVLayout, rightBreastOverlayUVLayout);
        this.hurtSounds = Configuration.HURT_SOUNDS.createValueHandler(hurtSounds);
        this.holidayThemes = Configuration.HOLIDAY_THEMES.createValueHandler(holidayThemes);
        this.showBreastsInArmor = Configuration.SHOW_IN_ARMOR.createValueHandler(showBreastsInArmor);
    }
}

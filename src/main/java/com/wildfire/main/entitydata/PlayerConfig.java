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
import com.wildfire.main.uvs.UVLayout;

/// A version of [EntityConfig] backed by a [Configuration] for use with players
public class PlayerConfig extends EntityConfig {

    //~ if >=26.2 'oldMcCodec()' -> 'codec()'
    public static final Codec<PlayerConfig> CODEC = codec();

    //? if >=26.2 {
    private static Codec<PlayerConfig> codec() {
        return RecordCodecBuilder.create(instance -> codecGroup(instance)
            .and(Configuration.HURT_SOUNDS.codecOrDefault().forGetter(PlayerConfig::hasHurtSounds))
            .and(Configuration.SHOW_IN_ARMOR.codecOrDefault().forGetter(EntityConfig::showBreastsInArmor))
            .and(Configuration.HOLIDAY_THEMES.codecOrDefault().forGetter(PlayerConfig::hasHolidayThemes))
            .apply(instance, PlayerConfig::new));
    }
    //?}
    //? if <26.2 {
    private static Codec<PlayerConfig> oldMcCodec() {
        return RecordCodecBuilder.create(instance -> {
            var p11 = (com.wildfire.mixins.accessors.ProductsAccessor<RecordCodecBuilder.Mu<PlayerConfig>, Gender, Float, Float, Breasts, Boolean, Float, Float, UVLayout, UVLayout, UVLayout, UVLayout>) (Object) codecGroup(instance);
            return new com.mojang.datafixers.Products.P14<>(p11.t1(), p11.t2(), p11.t3(), p11.t4(), p11.t5(), p11.t6(), p11.t7(), p11.t8(), p11.t9(), p11.t10(), p11.t11(),
                    Configuration.SHOW_IN_ARMOR.codecOrDefault().forGetter(EntityConfig::showBreastsInArmor),
                    Configuration.HOLIDAY_THEMES.codecOrDefault().forGetter(PlayerConfig::hasHolidayThemes),
                    Configuration.HURT_SOUNDS.codecOrDefault().forGetter(PlayerConfig::hasHurtSounds)
            ).apply(instance, PlayerConfig::new);
        });
    }
    //?}

    protected boolean hurtSounds;
    protected boolean holidayThemes;
    protected boolean showBreastsInArmor;

    public PlayerConfig(Gender gender, float bustSize, float voicePitch, Breasts breasts, boolean breastPhysics, float bounceMultiplier , float floppyMultiplier,
        UVLayout leftBreastUVLayout, UVLayout rightBreastUVLayout, UVLayout leftBreastOverlayUVLayout, UVLayout rightBreastOverlayUVLayout,
        boolean hurtSounds, boolean showBreastsInArmor, boolean holidayThemes) {
        super(gender, bustSize, voicePitch, breasts, breastPhysics, bounceMultiplier, floppyMultiplier, leftBreastUVLayout, rightBreastUVLayout,
            leftBreastOverlayUVLayout, rightBreastOverlayUVLayout);
        this.hurtSounds = hurtSounds;
        this.holidayThemes = holidayThemes;
        this.showBreastsInArmor = showBreastsInArmor;
    }

    public boolean updateGender(Gender value) {
        return updateValue(Configuration.GENDER, value, v -> this.gender = v);
    }

    public boolean updateBustSize(float value) {
        return updateValue(Configuration.BUST_SIZE, value, v -> this.bustSize = v);
    }


    public boolean hasHolidayThemes() {
        return holidayThemes;
    }

    public boolean updateHolidayThemes(boolean value) {
        return updateValue(Configuration.HOLIDAY_THEMES, value, v -> this.holidayThemes = v);
    }

    public boolean hasHurtSounds() {
        return hurtSounds;
    }

    public boolean updateVoicePitch(float value) {
        return updateValue(Configuration.VOICE_PITCH, value, v -> this.voicePitch = v);
    }

    public boolean updateHurtSounds(boolean value) {
        return updateValue(Configuration.HURT_SOUNDS, value, v -> this.hurtSounds = v);
    }

    public boolean updateBreastPhysics(boolean value) {
        return updateValue(Configuration.BREAST_PHYSICS, value, v -> this.breastPhysics = v);
    }

    @Override
    public boolean showBreastsInArmor() {
        return showBreastsInArmor;
    }

    public boolean updateShowBreastsInArmor(boolean value) {
        return updateValue(Configuration.SHOW_IN_ARMOR, value, v -> this.showBreastsInArmor = v);
    }

    public boolean updateBounceMultiplier(float value) {
        return updateValue(Configuration.BOUNCE_MULTIPLIER, value, v -> this.bounceMultiplier = v);
    }

    public boolean updateFloppiness(float value) {
        return updateValue(Configuration.FLOPPY_MULTIPLIER, value, v -> this.floppyMultiplier = v);
    }
}

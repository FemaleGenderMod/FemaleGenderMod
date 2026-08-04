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

import com.mojang.datafixers.Products.P11;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.config.types.ConfigKey;
import com.wildfire.main.uvs.UVLayout;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import java.util.function.Consumer;

/// A stripped down version of a [`player's config`][PlayerConfig], intended for use with non-player entities.
///
/// Unlike players, this has very minimal configuration support.
///
/// Currently only used for [`armor stands`][ArmorStand], and as a superclass for [`player configs`][PlayerConfig].
public class EntityConfig {

    /// @return `true` if the mod has support for the provided entity
    public static boolean isSupportedEntity(LivingEntity entity) {
        // TODO mannequins are not properly supported right now; this method only returns true to indicate that
        //        our rendering does technically support it, despite the fact that there is no way to properly utilize
        //        them without using janky workarounds.
        return entity instanceof Avatar || entity instanceof ArmorStand;
    }

    protected static final MapCodec<EntityConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> codecGroup(instance)
        .apply(instance, EntityConfig::new)
    );
    public static final Codec<EntityConfig> CODEC = MAP_CODEC.codec();

    //TODO: What of these can be moved to the player config codec
    protected static <CONFIG extends EntityConfig> P11<Mu<CONFIG>, Gender, Float, Float, Breasts, Boolean, Float, Float, UVLayout, UVLayout, UVLayout, UVLayout> codecGroup(Instance<CONFIG> instance) {
        return instance.group(
            Configuration.GENDER.codecOrDefault().forGetter(EntityConfig::getGender),
            Configuration.BUST_SIZE.codecOrDefault().forGetter(EntityConfig::getBustSize),
            Configuration.VOICE_PITCH.codecOrDefault().forGetter(EntityConfig::getVoicePitch),

            Breasts.CODEC.forGetter(EntityConfig::getBreasts),

            Configuration.BREAST_PHYSICS.codecOrDefault().forGetter(EntityConfig::hasBreastPhysics),
            Configuration.BOUNCE_MULTIPLIER.codecOrDefault().forGetter(EntityConfig::getBounceMultiplier),
            Configuration.FLOPPY_MULTIPLIER.codecOrDefault().forGetter(EntityConfig::getFloppiness),

            Configuration.LEFT_BREAST_UV_LAYOUT.codecOrDefault().forGetter(EntityConfig::getLeftBreastUVLayout),
            Configuration.RIGHT_BREAST_UV_LAYOUT.codecOrDefault().forGetter(EntityConfig::getRightBreastUVLayout),

            Configuration.LEFT_BREAST_OVERLAY_UV_LAYOUT.codecOrDefault().forGetter(EntityConfig::getLeftBreastOverlayUVLayout),
            Configuration.RIGHT_BREAST_OVERLAY_UV_LAYOUT.codecOrDefault().forGetter(EntityConfig::getRightBreastOverlayUVLayout)
        );
    }

    protected final Breasts breasts;

    protected Gender gender;
    protected float bustSize;
    protected boolean breastPhysics;
    protected float bounceMultiplier;
    protected float floppyMultiplier;

    protected UVLayout leftBreastUVLayout;
    protected UVLayout rightBreastUVLayout;

    protected UVLayout leftBreastOverlayUVLayout;
    protected UVLayout rightBreastOverlayUVLayout;

    protected float voicePitch;

    // note: hurt sounds, armor physics override, and show in armor are not defined here, as they have no relevance
    // to entities, and are instead entirely in PlayerConfig

    protected EntityConfig(Gender gender, float bustSize, float voicePitch, Breasts breasts, boolean breastPhysics, float bounceMultiplier , float floppyMultiplier,
        UVLayout leftBreastUVLayout, UVLayout rightBreastUVLayout, UVLayout leftBreastOverlayUVLayout, UVLayout rightBreastOverlayUVLayout) {
        this.gender = gender;
        this.bustSize = bustSize;
        this.voicePitch = voicePitch;
        this.breasts = breasts;
        this.breastPhysics = breastPhysics;
        this.bounceMultiplier = bounceMultiplier;
        this.floppyMultiplier = floppyMultiplier;
        this.leftBreastUVLayout = leftBreastUVLayout;
        this.rightBreastUVLayout = rightBreastUVLayout;
        this.leftBreastOverlayUVLayout = leftBreastOverlayUVLayout;
        this.rightBreastOverlayUVLayout = rightBreastOverlayUVLayout;
    }

    public Gender getGender() {
        return gender;
    }

    public Breasts getBreasts() {
        return breasts;
    }

    public float getBustSize() {
        return bustSize;
    }

    public boolean hasBreastPhysics() {
        return breastPhysics;
    }

    public boolean showBreastsInArmor() {
        return true;
    }

    public float getBounceMultiplier() {
        return bounceMultiplier;
    }

    public float getFloppiness() {
        return this.floppyMultiplier;
    }

    public float getVoicePitch() {
        return this.voicePitch;
    }

    // FIXME these update methods should match the rest and be in PlayerConfig instead of here
    // FIXME this should really be redesigned to not have multiple methods with very similar names;
    //		ideally something like `getUVs().skin().left()` etc.
    public UVLayout getLeftBreastUVLayout() {
        return this.leftBreastUVLayout;
    }

    public boolean updateLeftBreastUVLayout(UVLayout layout) {
        return updateValue(Configuration.LEFT_BREAST_UV_LAYOUT, layout, v -> this.leftBreastUVLayout = v);
    }

    public UVLayout getRightBreastUVLayout() {
        return this.rightBreastUVLayout;
    }

    public boolean updateRightBreastUVLayout(UVLayout layout) {
        return updateValue(Configuration.RIGHT_BREAST_UV_LAYOUT, layout, v -> this.rightBreastUVLayout = v);
    }

    public UVLayout getLeftBreastOverlayUVLayout() {
        return this.leftBreastOverlayUVLayout;
    }

    public boolean updateLeftBreastOverlayUVLayout(UVLayout layout) {
        return updateValue(Configuration.LEFT_BREAST_OVERLAY_UV_LAYOUT, layout, v -> this.leftBreastOverlayUVLayout = v);
    }

    public UVLayout getRightBreastOverlayUVLayout() {
        return this.rightBreastOverlayUVLayout;
    }

    public boolean updateRightBreastOverlayUVLayout(UVLayout layout) {
        return updateValue(Configuration.RIGHT_BREAST_OVERLAY_UV_LAYOUT, layout, v -> this.rightBreastOverlayUVLayout = v);
    }

    protected <VALUE> boolean updateValue(ConfigKey<VALUE> key, VALUE value, Consumer<VALUE> setter) {
        if (key.validate(value)) {
            setter.accept(value);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "%s(gender=%s)".formatted(getClass().getCanonicalName(), gender);
    }
}

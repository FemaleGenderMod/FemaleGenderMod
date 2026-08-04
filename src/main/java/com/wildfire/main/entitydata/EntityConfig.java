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
import com.wildfire.main.config.value.ConfigValue;
import com.wildfire.main.uvs.UVLayout;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

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
            Configuration.GENDER.codecOrDefault().forGetter(config -> config.gender.get()),
            Configuration.BUST_SIZE.codecOrDefault().forGetter(config -> config.bustSize.get()),
            Configuration.VOICE_PITCH.codecOrDefault().forGetter(config -> config.voicePitch.get()),

            Breasts.CODEC.forGetter(config -> config.breasts),

            Configuration.BREAST_PHYSICS.codecOrDefault().forGetter(config -> config.breastPhysics.get()),
            Configuration.BOUNCE_MULTIPLIER.codecOrDefault().forGetter(config -> config.bounceMultiplier.get()),
            Configuration.FLOPPY_MULTIPLIER.codecOrDefault().forGetter(config -> config.floppiness.get()),

            Configuration.LEFT_BREAST_UV_LAYOUT.codecOrDefault().forGetter(config -> config.leftBreastUVLayout.get()),
            Configuration.RIGHT_BREAST_UV_LAYOUT.codecOrDefault().forGetter(config -> config.rightBreastUVLayout.get()),

            Configuration.LEFT_BREAST_OVERLAY_UV_LAYOUT.codecOrDefault().forGetter(config -> config.leftBreastOverlayUVLayout.get()),
            Configuration.RIGHT_BREAST_OVERLAY_UV_LAYOUT.codecOrDefault().forGetter(config -> config.rightBreastOverlayUVLayout.get())
        );
    }

    public final Breasts breasts;

    public final ConfigValue<Gender> gender;
    //TODO: Primitive value types?
    public final ConfigValue<Float> bustSize;
    public final ConfigValue<Boolean> breastPhysics;
    public final ConfigValue<Float> bounceMultiplier;
    public final ConfigValue<Float> floppiness;

    // FIXME this should really be redesigned to not have multiple methods with very similar names;
    //		ideally something like `getUVs().skin().left()` etc.
    public final ConfigValue<UVLayout> leftBreastUVLayout;
    public final ConfigValue<UVLayout> rightBreastUVLayout;

    public final ConfigValue<UVLayout> leftBreastOverlayUVLayout;
    public final ConfigValue<UVLayout> rightBreastOverlayUVLayout;

    public final ConfigValue<Float> voicePitch;

    // note: hurt sounds, armor physics override, and show in armor are not defined here, as they have no relevance
    // to entities, and are instead entirely in PlayerConfig

    //? if <26.2 {
    EntityConfig(EntityConfig cfg) {//Handling for old MC, just copy the intermediary created values as we can just take over the objects
        this.gender = cfg.gender;
        this.bustSize = cfg.bustSize;
        this.voicePitch = cfg.voicePitch;
        this.breasts = cfg.breasts;
        this.breastPhysics = cfg.breastPhysics;
        this.bounceMultiplier = cfg.bounceMultiplier;
        this.floppiness = cfg.floppiness;
        this.leftBreastUVLayout = cfg.leftBreastUVLayout;
        this.rightBreastUVLayout = cfg.rightBreastUVLayout;
        this.leftBreastOverlayUVLayout = cfg.leftBreastOverlayUVLayout;
        this.rightBreastOverlayUVLayout = cfg.rightBreastOverlayUVLayout;
    }
    //~}

    protected EntityConfig(Gender gender, float bustSize, float voicePitch, Breasts breasts, boolean breastPhysics, float bounceMultiplier, float floppiness,
        UVLayout leftBreastUVLayout, UVLayout rightBreastUVLayout, UVLayout leftBreastOverlayUVLayout, UVLayout rightBreastOverlayUVLayout) {
        this.gender = Configuration.GENDER.createValueHandler(gender);
        this.bustSize = Configuration.BUST_SIZE.createValueHandler(bustSize);
        this.voicePitch = Configuration.VOICE_PITCH.createValueHandler(voicePitch);
        this.breasts = breasts;
        this.breastPhysics = Configuration.BREAST_PHYSICS.createValueHandler(breastPhysics);
        this.bounceMultiplier = Configuration.BOUNCE_MULTIPLIER.createValueHandler(bounceMultiplier);
        this.floppiness = Configuration.FLOPPY_MULTIPLIER.createValueHandler(floppiness);
        this.leftBreastUVLayout = Configuration.LEFT_BREAST_UV_LAYOUT.createValueHandler(leftBreastUVLayout);
        this.rightBreastUVLayout = Configuration.RIGHT_BREAST_UV_LAYOUT.createValueHandler(rightBreastUVLayout);
        this.leftBreastOverlayUVLayout = Configuration.LEFT_BREAST_OVERLAY_UV_LAYOUT.createValueHandler(leftBreastOverlayUVLayout);
        this.rightBreastOverlayUVLayout = Configuration.RIGHT_BREAST_OVERLAY_UV_LAYOUT.createValueHandler(rightBreastOverlayUVLayout);
    }

    @Override
    public String toString() {
        return "%s(gender=%s)".formatted(getClass().getCanonicalName(), gender);
    }
}

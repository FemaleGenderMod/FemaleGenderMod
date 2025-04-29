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

import com.wildfire.main.Gender;
import com.wildfire.physics.BreastPhysics;
import net.minecraft.util.math.MathHelper;

/**
 * Represents an immutable view of a {@link EntityConfig} at a certain point in time.
 */
public record EntityConfigState(
    Gender gender,
    float bustSize,
    boolean hasBreastPhysics,
    float bounceMultiplier,
    float floppyMultiplier,
    BreastState breasts,
    BreastPhysicsState leftBreastPhysics,
    BreastPhysicsState rightBreastPhysics,
    boolean getArmorPhysicsOverride,
    boolean showBreastsInArmor,
    boolean hasJacketLayer,
    boolean hasHolidayThemes
) {
    public EntityConfigState(EntityConfig entityConfig) {
        this(
            entityConfig.getGender(),
            entityConfig.getBustSize(),
            entityConfig.hasBreastPhysics(),
            entityConfig.getBounceMultiplier(),
            entityConfig.getFloppiness(),
            new BreastState(entityConfig.getBreasts()),
            new BreastPhysicsState(entityConfig.getLeftBreastPhysics()),
            new BreastPhysicsState(entityConfig.getRightBreastPhysics()),
            entityConfig.getArmorPhysicsOverride(),
            entityConfig.showBreastsInArmor(),
            entityConfig.hasJacketLayer(),
            (entityConfig instanceof PlayerConfig playerConfig && playerConfig.hasHolidayThemes())
        );
    }

    public record BreastState(
        float xOffset,
        float yOffset,
        float zOffset,
        float cleavage,
        boolean uniboob
    ) {
        public BreastState(Breasts breasts) {
            this(
                breasts.getXOffset(),
                breasts.getYOffset(),
                breasts.getZOffset(),
                breasts.getCleavage(),
                breasts.isUniboob()
            );
        }
    }

    public record BreastPhysicsState(
        float prePositionY, float positionY,
        float prePositionX, float positionX,
        float preBounceRotation, float bounceRotation,
        float preBreastSize, float breastSize
    ) {
        public BreastPhysicsState(BreastPhysics breastPhysics) {
            this(
                breastPhysics.getPrePositionY(), breastPhysics.getPositionY(),
                breastPhysics.getPrePositionX(), breastPhysics.getPositionX(),
                breastPhysics.getPreBounceRotation(), breastPhysics.getBounceRotation(),
                breastPhysics.getPreBreastSize(), breastPhysics.getBreastSize()
            );
        }

        public float getPositionY(float partialTicks) {
            return MathHelper.lerp(partialTicks, this.prePositionY(), this.positionY());
        }

        public float getPositionX(float partialTicks) {
            return MathHelper.lerp(partialTicks, this.prePositionX(), this.positionX());
        }

        public float getBounceRotation(float partialTicks) {
            return MathHelper.lerp(partialTicks, this.preBounceRotation(), this.bounceRotation());
        }

        public float getBreastSize(float partialTicks) {
            return MathHelper.lerp(partialTicks, this.preBreastSize(), this.breastSize());
        }
    }
}

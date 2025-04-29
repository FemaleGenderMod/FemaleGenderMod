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

public class EntityConfigState {
    public final BreastState breasts = new BreastState();
    public final BreastPhysicsState leftBreastPhysics = new BreastPhysicsState();
    public final BreastPhysicsState rightBreastPhysics = new BreastPhysicsState();

    public Gender gender;
    public float bustSize;
    public boolean hasBreastPhysics;
    public float bounceMultiplier;
    public float floppyMultiplier;
    public boolean getArmorPhysicsOverride;
    public boolean showBreastsInArmor;
    public boolean hasJacketLayer;
    public boolean hasHolidayThemes;

    public void update(EntityConfig entityConfig) {
        this.breasts.update(entityConfig.getBreasts());
        this.leftBreastPhysics.update(entityConfig.getLeftBreastPhysics());
        this.rightBreastPhysics.update(entityConfig.getRightBreastPhysics());

        this.gender = entityConfig.getGender();
        this.bustSize = entityConfig.getBustSize();
        this.hasBreastPhysics = entityConfig.hasBreastPhysics();
        this.bounceMultiplier = entityConfig.getBounceMultiplier();
        this.floppyMultiplier = entityConfig.getFloppiness();
        this.getArmorPhysicsOverride = entityConfig.getArmorPhysicsOverride();
        this.showBreastsInArmor = entityConfig.showBreastsInArmor();
        this.hasJacketLayer = entityConfig.hasJacketLayer();
        this.hasHolidayThemes = entityConfig instanceof PlayerConfig playerConfig && playerConfig.hasHolidayThemes();
    }

    public static class BreastState {
        public float xOffset;
        public float yOffset;
        public float zOffset;
        public float cleavage;
        public boolean uniboob;

        public void update(Breasts breasts) {
            this.xOffset = breasts.getXOffset();
            this.yOffset = breasts.getYOffset();
            this.zOffset = breasts.getZOffset();
            this.cleavage = breasts.getCleavage();
            this.uniboob = breasts.isUniboob();
        }
    }

    public static class BreastPhysicsState {
        private float prePositionY, positionY;
        private float prePositionX, positionX;
        private float preBounceRotation, bounceRotation;
        private float preBreastSize, breastSize;

        public void update(BreastPhysics breastPhysics) {
            this.prePositionY = breastPhysics.getPrePositionY();
            this.positionY = breastPhysics.getPositionY();
            this.prePositionX = breastPhysics.getPrePositionX();
            this.positionX = breastPhysics.getPositionX();
            this.preBounceRotation = breastPhysics.getPreBounceRotation();
            this.bounceRotation = breastPhysics.getBounceRotation();
            this.preBreastSize = breastPhysics.getPreBreastSize();
            this.breastSize = breastPhysics.getBreastSize();
        }

        public float getPositionY(float partialTicks) {
            return MathHelper.lerp(partialTicks, this.prePositionY, this.positionY);
        }

        public float getPositionX(float partialTicks) {
            return MathHelper.lerp(partialTicks, this.prePositionX, this.positionX);
        }

        public float getBounceRotation(float partialTicks) {
            return MathHelper.lerp(partialTicks, this.preBounceRotation, this.bounceRotation);
        }

        public float getBreastSize(float partialTicks) {
            return MathHelper.lerp(partialTicks, this.preBreastSize, this.breastSize);
        }
    }
}

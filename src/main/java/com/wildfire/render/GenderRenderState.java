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

package com.wildfire.render;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.WildfireGenderClient;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.physics.BreastPhysics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

/**
 * A decoupled render state object that represents a snapshot of a {@link EntityConfig} during a certain frame.
 */
@Environment(EnvType.CLIENT)
public class GenderRenderState {
    private static final RenderStateDataKey<GenderRenderState> STATE = RenderStateDataKey.create(() -> "GenderRenderState");

    public static void update(LivingEntity entity, EntityRenderState state, float partialTicks) {
        if(EntityConfig.isSupportedEntity(entity)) {
            var config = EntityConfig.getEntity(entity);
            state.setData(STATE, new GenderRenderState(config, entity, partialTicks));
        }
    }

    public static @Nullable GenderRenderState get(EntityRenderState state) {
        return state.getData(STATE);
    }

    public final BreastState breasts;
    public final BreastPhysicsState leftBreastPhysics;
    public final BreastPhysicsState rightBreastPhysics;

    public final float partialTicks;

    public final Gender gender;
    public final float bustSize;
    public final boolean hasBreastPhysics;
    public final float bounceMultiplier;
    public final float floppyMultiplier;
    public final boolean armorPhysicsOverride;
    public final boolean showBreastsInArmor;
    public final boolean hasJacketLayer;
    public final boolean hasHolidayThemes;

    public final UVLayout leftBreastUVLayout;
    public final UVLayout rightBreastUVLayout;
    public final UVLayout leftBreastOverlayUVLayout;
    public final UVLayout rightBreastOverlayUVLayout;
    public final IGenderArmor armor;

    public final boolean isBreathing;
    public final @Nullable Component nametag;

    private GenderRenderState(EntityConfig entityConfig, LivingEntity entity, float partialTicks) {
        this.breasts = new BreastState(entityConfig.getBreasts());
        this.leftBreastPhysics = new BreastPhysicsState(entityConfig.getLeftBreastPhysics());
        this.rightBreastPhysics = new BreastPhysicsState(entityConfig.getRightBreastPhysics());

        this.partialTicks = partialTicks;

        this.gender = entityConfig.getGender();
        this.bustSize = entityConfig.getBustSize();
        this.hasBreastPhysics = entityConfig.hasBreastPhysics();
        this.bounceMultiplier = entityConfig.getBounceMultiplier();
        this.floppyMultiplier = entityConfig.getFloppiness();
        this.armorPhysicsOverride = entityConfig.getArmorPhysicsOverride();
        this.showBreastsInArmor = entityConfig.showBreastsInArmor();

        if(entity instanceof Avatar playerLikeEntity) {
            this.hasJacketLayer = playerLikeEntity.isModelPartShown(PlayerModelPart.JACKET);
        } else {
            this.hasJacketLayer = entityConfig instanceof PlayerConfig || entityConfig.hasJacketLayer();
        }

        if(entityConfig instanceof PlayerConfig playerConfig) {
            this.hasHolidayThemes = playerConfig.hasHolidayThemes();
        } else {
            this.hasHolidayThemes = false;
        }

        this.leftBreastUVLayout = entityConfig.getLeftBreastUVLayout().copy();
        this.rightBreastUVLayout = entityConfig.getRightBreastUVLayout().copy();
        this.leftBreastOverlayUVLayout = entityConfig.getLeftBreastOverlayUVLayout().copy();
        this.rightBreastOverlayUVLayout = entityConfig.getRightBreastOverlayUVLayout().copy();
        this.armor = WildfireHelper.getArmorConfig(entity.getItemBySlot(EquipmentSlot.CHEST));

        //noinspection resource
        this.isBreathing = !entity.isUnderWater() || MobEffectUtil.hasWaterBreathing(entity) ||
            entity.level().getBlockState(entity.blockPosition()).is(Blocks.BUBBLE_COLUMN);
        this.nametag = entity instanceof Player ? WildfireGenderClient.getNametag(entity.getUUID()) : null;
    }

    public static class BreastState {
        public final float xOffset;
        public final float yOffset;
        public final float zOffset;
        public final float cleavage;
        public final boolean uniboob;

        private BreastState(Breasts breasts) {
            this.xOffset = breasts.getXOffset();
            this.yOffset = breasts.getYOffset();
            this.zOffset = breasts.getZOffset();
            this.cleavage = breasts.getCleavage();
            this.uniboob = breasts.isUniboob();
        }
    }

    public class BreastPhysicsState {
        private final float prePositionY, positionY;
        private final float prePositionX, positionX;
        private final float preBounceRotation, bounceRotation;
        private final float preBreastSize, breastSize;

        private BreastPhysicsState(BreastPhysics breastPhysics) {
            this.prePositionY = breastPhysics.getPrePositionY();
            this.positionY = breastPhysics.getPositionY();
            this.prePositionX = breastPhysics.getPrePositionX();
            this.positionX = breastPhysics.getPositionX();
            this.preBounceRotation = breastPhysics.getPreBounceRotation();
            this.bounceRotation = breastPhysics.getBounceRotation();
            this.preBreastSize = breastPhysics.getPreBreastSize();
            this.breastSize = breastPhysics.getBreastSize();
        }

        public float getPositionY() {
            return Mth.lerp(partialTicks, this.prePositionY, this.positionY);
        }

        public float getPositionX() {
            return Mth.lerp(partialTicks, this.prePositionX, this.positionX);
        }

        public float getBounceRotation() {
            return Mth.lerp(partialTicks, this.preBounceRotation, this.bounceRotation);
        }

        public float getBreastSize() {
            return Mth.lerp(partialTicks, this.preBreastSize, this.breastSize);
        }
    }
}

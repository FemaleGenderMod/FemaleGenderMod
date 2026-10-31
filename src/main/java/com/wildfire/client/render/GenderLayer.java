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

package com.wildfire.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.wildfire.api.IGenderArmor;
import com.wildfire.api.client.WildfireClientAPI;
import com.wildfire.api.uvs.UVLayout;
import com.wildfire.client.WildfireClientHelper;
import com.wildfire.client.config.ClientConfig;
import com.wildfire.client.physics.BreastPhysics;
import com.wildfire.client.physics.BothBreastsPhysics;
import com.wildfire.client.render.WildfireModelRenderer.BreastModelBox;
import com.wildfire.client.render.WildfireModelRenderer.OverlayModelBox;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.WildfireHelper;
import com.wildfire.common.config.UVs;
import com.wildfire.common.entities.BreastState;
import com.wildfire.common.entities.EntityConfigHolder;
import com.wildfire.common.entities.avatars.AvatarConfig;
import com.wildfire.common.entities.armorstands.ArmorStandConfigHolder;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import com.wildfire.client.gui.FakeGUIPlayer;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;
import org.joml.Quaternionf;

// TODO split this into an AbstractGenderLayer?
/// @apiNote Only use this on the client side
public class GenderLayer<ENTITY extends LivingEntity, MODEL extends HumanoidModel<ENTITY>> extends RenderLayer<ENTITY, MODEL> {

    /// @apiNote  **Do not modify this array**
    private static final BreastSide[] BREAST_SIDES = BreastSide.values();

    @UnknownNullability("null until #resizeBox() is first called")
    private BreastModelBox lBreast, rBreast;
    @UnknownNullability("null until #resizeBox() is first called")
    private OverlayModelBox lBreastWear, rBreastWear;

    private @Nullable UVLayout prevLeftBreastUVLayout, prevRightBreastUVLayout,
        prevLeftBreastOverlayUVLayout, prevRightBreastOverlayUVLayout;

    private boolean isUniboob;
    protected boolean isChestplateOccupied, bounceEnabled, breathingAnimation, hasJacketLayer;
    protected float breastOffsetX, breastOffsetY, breastOffsetZ, lPhysPositionY, lPhysPositionX, rPhysPositionY, rPhysPositionX,
            lPhysBounceRotation, rPhysBounceRotation, breastSize, zOffset, outwardAngle;

    public GenderLayer(RenderLayerParent<ENTITY, MODEL> render) {
        super(render);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource bufferSource, int lightCoords, ENTITY entity, float limbAngle,
        float limbDistance, float partialTicks, float animationProgress, float headYaw, float headPitch) {
        if (ClientConfig.config().overrides().disableRendering().get() || entity.isSpectator()) {
            //Rendering is disabled client side, or the entity is in spectator so only the head will be rendered
            return;
        }
        //Surround with a try/catch to fix for essential mod.
        try {
            EntityConfigHolder<?> entityConfig = entity instanceof Player player && FakeGUIPlayer.isFakePlayer(player)
                ? FakeGUIPlayer.getConfigOf(player)
                : WildfireClientAPI.getConfig(entity);
            if (entityConfig == null) return;

            if (!setupRender(entity, entityConfig, partialTicks)) return;

            //RenderType selection copied from LivingEntityRenderer#getRenderType
            RenderType breastRenderType = null;
            int baseColor = 0xFFFFFFFF;
            ResourceLocation entityTexture = getTextureLocation(entity);
            if (entityTexture != null) {
                boolean bodyVisible = !entity.isInvisible();
                Minecraft minecraft = Minecraft.getInstance();
                boolean translucent = !bodyVisible && minecraft.player != null && !entity.isInvisibleTo(minecraft.player);
                if (translucent) {
                    breastRenderType = RenderType.itemEntityTranslucentCull(entityTexture);
                    baseColor = 0x26FFFFFF;
                } else if (bodyVisible) {
                    breastRenderType = RenderType.entityTranslucent(entityTexture);
                } else if (minecraft.shouldEntityAppearGlowing(entity)) {
                    breastRenderType = RenderType.outline(entityTexture);
                } else if (!isChestplateOccupied) {
                    //Exit early if we don't need to render the breasts, and we don't need to render the armor
                    return;
                }
            } else if (!isChestplateOccupied) {
                //Exit early if we don't need to render the breasts, and we don't need to render the armor
                return;
            }

            //Note: We ignore LivingEntityRenderer#getWhiteOverlayProgress as it is always 0 for entities we attach the layer to
            int overlayCoords = LivingEntityRenderer.getOverlayCoords(entity, 0);
            final RenderType finalBreastRenderType = breastRenderType;
            final int finalBaseColor = baseColor;
            renderSides(entity, getParentModel(), matrixStack, bufferSource, lightCoords, overlayCoords,
                (breastEntity, poseStack, buffers, light, overlay, side) ->
                    renderBreast(breastEntity, finalBreastRenderType, poseStack, buffers, light, overlay, finalBaseColor, side));
        } catch(Exception e) {
            WildfireGender.LOGGER.error("Failed to render breast layer", e);
        }
    }

    /// Common logic for setting up breast rendering
    ///
    /// @return `true` if rendering should continue
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean setupRender(ENTITY entity, EntityConfigHolder<?> entityConfig, float partialTicks) {
        return setupRender(entity, entityConfig, partialTicks,
            WildfireClientHelper.getArmorConfig(entity.getItemBySlot(EquipmentSlot.CHEST)));
    }

    /// Common logic for setting up breast rendering
    ///
    /// @return `true` if rendering should continue
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean setupRender(ENTITY entity, EntityConfigHolder<?> entityConfig, float partialTicks, IGenderArmor genderArmor) {
        boolean armorPhysicsOverride = ClientConfig.config().overrides().armorPhysics().get();
        isChestplateOccupied = genderArmor.coversBreasts() && !armorPhysicsOverride;
        if (genderArmor.alwaysHidesBreasts() || !showBreastsInArmor(entityConfig) && isChestplateOccupied) {
            //If the armor always hides breasts or there is armor and the player configured breasts
            // to be hidden when wearing armor, we can just exit early rather than doing any calculations
            return false;
        }

        BreastState breasts = new BreastState(entityConfig.breasts());
        breastOffsetX = WildfireHelper.round(breasts.offsets().x(), 1);
        breastOffsetY = -WildfireHelper.round(breasts.offsets().y(), 1);
        breastOffsetZ = -WildfireHelper.round(breasts.offsets().z(), 1);

        isUniboob = entityConfig.breasts().physics().uniboob().get();
        hasJacketLayer = entity instanceof Player player && player.isModelPartShown(PlayerModelPart.JACKET)
            || entityConfig instanceof ArmorStandConfigHolder armorStandConfig && armorStandConfig.hasJacketLayer();

        BothBreastsPhysics breastPhysics = entityConfig.breastPhysics();
        BreastPhysics leftPhysics = breastPhysics.left();
        // Physics values are interpolated in-place during rendering, replacing the cached render state layer
        final float bSize = Mth.lerp(partialTicks, leftPhysics.getPreBreastSize(), leftPhysics.getBreastSize());
        outwardAngle = Math.round(breasts.cleavage() * 100f);
        outwardAngle = Math.min(outwardAngle, 10);

        resizeBox(entityConfig);

        lPhysPositionY = Mth.lerp(partialTicks, leftPhysics.getPrePositionY(), leftPhysics.getPositionY());
        lPhysPositionX = Mth.lerp(partialTicks, leftPhysics.getPrePositionX(), leftPhysics.getPositionX());
        lPhysBounceRotation = Mth.lerp(partialTicks, leftPhysics.getPreBounceRotation(), leftPhysics.getBounceRotation());
        if(isUniboob) {
            rPhysPositionY = lPhysPositionY;
            rPhysPositionX = lPhysPositionX;
            rPhysBounceRotation = lPhysBounceRotation;
        } else {
            BreastPhysics rightPhysics = breastPhysics.right();
            rPhysPositionY = Mth.lerp(partialTicks, rightPhysics.getPrePositionY(), rightPhysics.getPositionY());
            rPhysPositionX = Mth.lerp(partialTicks, rightPhysics.getPrePositionX(), rightPhysics.getPositionX());
            rPhysBounceRotation = Mth.lerp(partialTicks, rightPhysics.getPreBounceRotation(), rightPhysics.getBounceRotation());
        }

        breastSize = Math.min(bSize * 1.5f, 0.7f); // Limit the max size to 0.7f

        if (bSize > 0.7f) {
            breastSize = bSize; // If bSize exceeds 0.7f, use bSize
        }

        if (breastSize < 0.02f) {
            return false; // Return false if breastSize is too small
        }

        zOffset = 0.0625f - (bSize * 0.0625f); // Calculate zOffset
        breastSize += 0.5f * Math.abs(bSize - 0.7f) * 2f; // Adjust breastSize based on bSize

        //If the armor physics is overridden ignore resistance
        float resistance = Math.clamp(genderArmor.physicsResistance(), 0, 1);
        boolean isBreathing = !entity.isUnderWater() || MobEffectUtil.hasWaterBreathing(entity) ||
            entity.level().getBlockState(entity.blockPosition()).is(Blocks.BUBBLE_COLUMN);
        //Note: We only check if the breathing animation should be enabled if the chestplate's physics resistance
        // is less than or equal to 0.5 so that if we won't be rendering it we can avoid doing extra calculations
        breathingAnimation = (armorPhysicsOverride || resistance <= 0.5F) && isBreathing;
        bounceEnabled = entityConfig.breasts().physics().enabled().get() && (!isChestplateOccupied || resistance < 1); //oh, you found this?
        return true;
    }

    private static boolean showBreastsInArmor(EntityConfigHolder<?> entityConfig) {
        return entityConfig.config() instanceof AvatarConfig playerConfig ? playerConfig.showBreastsInArmor.get() : true;
    }

    protected void resizeBox(EntityConfigHolder<?> entityConfig) {
        //TODO: Better way for this?
        UVs uvs = entityConfig.uvs();
        UVLayout leftBreastUVLayout = uvs.skin().left().get().copy();
        UVLayout rightBreastUVLayout = uvs.skin().right().get().copy();
        UVLayout leftBreastOverlayUVLayout = uvs.overlay().left().get().copy();
        UVLayout rightBreastOverlayUVLayout = uvs.overlay().right().get().copy();

        if(!Objects.equals(this.prevLeftBreastUVLayout, leftBreastUVLayout)
                || !Objects.equals(this.prevRightBreastUVLayout, rightBreastUVLayout)
                || !Objects.equals(this.prevLeftBreastOverlayUVLayout, leftBreastOverlayUVLayout)
                || !Objects.equals(this.prevRightBreastOverlayUVLayout, rightBreastOverlayUVLayout)) {

            this.prevLeftBreastUVLayout = leftBreastUVLayout;
            this.prevRightBreastUVLayout = rightBreastUVLayout;
            this.prevLeftBreastOverlayUVLayout = leftBreastOverlayUVLayout;
            this.prevRightBreastOverlayUVLayout = rightBreastOverlayUVLayout;

            this.lBreast = new BreastModelBox(64, 64, -4F, 0.0F, 0F, 4, 5, 3, 0.0F, leftBreastUVLayout);
            this.rBreast = new BreastModelBox(64, 64, 0F, 0.0F, 0F, 4, 5, 3, 0.0F, rightBreastUVLayout);
            this.lBreastWear = new OverlayModelBox(64, 64, -4F, 0.0F, 0F, 4, 5, 3, 0.0F, leftBreastOverlayUVLayout);
            this.rBreastWear = new OverlayModelBox(64, 64, 0, 0.0F, 0F, 4, 5, 3, 0.0F, rightBreastOverlayUVLayout);
        }
    }

    protected void setupTransformations(ENTITY entity, MODEL model, PoseStack matrixStack, BreastSide side) {
        if(entity.isBaby()) {
            float ageScale = 1.0F / model.babyBodyScale;
            matrixStack.scale(ageScale, ageScale, ageScale);
            matrixStack.translate(0.0D, model.bodyYOffset / 16.0F, 0.0D);
        }

        ModelPart body = model.body;
        body.translateAndRotate(matrixStack);

        if(bounceEnabled) {
            matrixStack.translate(side.forSide(lPhysPositionX, rPhysPositionX) / 32f, side.forSide(lPhysPositionY, rPhysPositionY) / 32f, 0);
        }

        matrixStack.translate(side.leftOrNegate(breastOffsetX) * 0.0625f, 0.05625f + (breastOffsetY * 0.0625f), zOffset - 0.0625f * 2f + (breastOffsetZ * 0.0425f)); //shift down to correct position

        if(!isUniboob) {
            matrixStack.translate(side.leftOrNegate(-0.0625f * 2), 0, 0);
        }
        if(bounceEnabled) {
            matrixStack.mulPose(Axis.YP.rotationDegrees(side.forSide(lPhysBounceRotation, rPhysBounceRotation)));
        }
        if(!isUniboob) {
            matrixStack.translate(side.leftOrNegate(0.0625f * 2), 0, 0);
        }

        float rotation = breastSize;
        if(bounceEnabled) {
            matrixStack.translate(0, -0.035f * breastSize, 0); //shift down to correct position
            rotation -= side.forSide(lPhysPositionY, rPhysPositionY) / 12f;
        }

        rotation = Math.min(rotation, breastSize + 0.2f);
        rotation = Math.min(rotation, 1); //hard limit for MAX

        if(isChestplateOccupied) {
            matrixStack.translate(0, 0, 0.01f);
        }

        Quaternionf rotationTransform = side.forSide(Axis.YP, Axis.YN)
            .rotationDegrees(outwardAngle)
            .rotateX(-35f * rotation * Mth.DEG_TO_RAD);

        if(breathingAnimation) {
            float f5 = -Mth.cos(entity.tickCount * 0.09F) * 0.45F + 0.45F;
            rotationTransform.rotateX(f5 * Mth.DEG_TO_RAD);
        }

        matrixStack.mulPose(rotationTransform);
        matrixStack.scale(0.9995f, 1f, 1f); //z-fighting FIXXX
    }

    private void renderBreast(ENTITY entity, @Nullable RenderType breastRenderType, PoseStack poseStack, MultiBufferSource bufferSource,
        int lightCoords, int overlayCoords, int color, BreastSide side) {
        if (breastRenderType != null) {
            //Only render the breasts if we have a render type for them
            VertexConsumer vertexConsumer = bufferSource.getBuffer(breastRenderType);
            renderBox(side.forSide(lBreast, rBreast), poseStack, vertexConsumer, lightCoords, overlayCoords, color);
            if (hasJacketLayer) {
                shiftForJacket(poseStack);
                renderBox(side.forSide(lBreastWear, rBreastWear), poseStack, vertexConsumer, lightCoords, overlayCoords, color);
            }
        } else if (hasJacketLayer) { //Copy exact size
            shiftForJacket(poseStack);
        }
    }

    protected static void shiftForJacket(PoseStack matrixStack) {
        matrixStack.translate(0, 0, -0.015f);
        matrixStack.scale(1.05f, 1.05f, 1.05f);
    }

    protected static void renderBox(WildfireModelRenderer.ModelBox model, PoseStack matrixStack, VertexConsumer bufferIn,
        int light, int overlay, int color) {
        WildfireModelRenderer.renderBox(model, matrixStack.last(), bufferIn, light, overlay, color);
    }

    protected void renderSides(ENTITY entity, MODEL model, PoseStack matrixStack, MultiBufferSource bufferSource, int lightCoords,
        int overlayCoords, BreastSideRenderer<ENTITY> renderer) {
        for (final BreastSide breastSide : BREAST_SIDES) {
            matrixStack.pushPose();
            try {
                setupTransformations(entity, model, matrixStack, breastSide);
                renderer.render(entity, matrixStack, bufferSource, lightCoords, overlayCoords, breastSide);
            } finally {
                matrixStack.popPose();
            }
        }
    }

    @FunctionalInterface
    protected interface BreastSideRenderer<ENTITY extends LivingEntity> {
        void render(ENTITY entity, PoseStack poseStack, MultiBufferSource bufferSource, int lightCoords, int overlayCoords, BreastSide side);
    }
}

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

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildfire.api.IGenderArmor;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.mixins.accessors.LivingEntityRendererAccessor;
import com.wildfire.render.WildfireModelRenderer.BreastModelBox;
import com.wildfire.render.WildfireModelRenderer.OverlayModelBox;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;

import java.util.Objects;
import java.util.function.Consumer;

// TODO split this into an AbstractGenderLayer?
@Environment(EnvType.CLIENT)
public class GenderLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {

    private static final float DEG_TO_RAD = (float) (Math.PI / 180);

    @UnknownNullability("null until #resizeBox() is first called")
    private BreastModelBox lBreast, rBreast;
    @UnknownNullability("null until #resizeBox() is first called")
    private OverlayModelBox lBreastWear, rBreastWear;

    private @Nullable UVLayout prevLeftBreastUVLayout, prevRightBreastUVLayout,
        prevLeftBreastOverlayUVLayout, prevRightBreastOverlayUVLayout;

    private final RenderLayerParent<S, M> context;

    private boolean isUniboob;
    // although ItemStack instances are mutable, this is safe to keep a reference to as this is a copy of the real stack
    protected ItemStack armorStack = ItemStack.EMPTY;
    protected IGenderArmor genderArmor = IGenderArmor.EMPTY;
    protected boolean isChestplateOccupied, bounceEnabled, breathingAnimation;
    protected float breastOffsetX, breastOffsetY, breastOffsetZ, lPhysPositionY, lPhysPositionX, rPhysPositionY, rPhysPositionX,
            lPhysBounceRotation, rPhysBounceRotation, breastSize, zOffset, outwardAngle;

    public GenderLayer(RenderLayerParent<S, M> render) {
        super(render);
        this.context = render;
    }

    /**
     * Convenience method around {@link LivingEntityRendererAccessor#invokeGetRenderType}
     */
    private @Nullable RenderType getRenderLayer(S state) {
        var renderer = (LivingEntityRenderer<?, ?, ?>) context;
        var accessor = (LivingEntityRendererAccessor) renderer;

        boolean bodyVisible = accessor.invokeIsBodyVisible(state);
        boolean translucent = !bodyVisible && !state.isInvisibleToPlayer;
        boolean glowing = state.appearsGlowing();

        return accessor.invokeGetRenderType(state, bodyVisible, translucent, glowing);
    }

    @Override
    public void submit(PoseStack matrixStack, SubmitNodeCollector queue, int light, S state, float limbAngle, float limbDistance) {
        var entityConfigState = GenderRenderState.get(state);
        if(entityConfigState == null) return;

        try {
            if(!setupRender(state, entityConfigState)) return;
            int overlay = LivingEntityRenderer.getOverlayCoords(state, 0);

            //noinspection CodeBlock2Expr
            renderSides(state, getParentModel(), matrixStack, side -> {
                renderBreast(state, matrixStack, queue, overlay, side);
            });
        } catch(Exception e) {
            WildfireGender.LOGGER.error("Failed to render breast layer", e);
        }
    }

    /**
     * Common logic for setting up breast rendering
     *
     * @return {@code true} if rendering should continue
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean setupRender(S entityState, GenderRenderState genderState) {
        if(!ClientConfig.RENDER_BREASTS) return false;

        armorStack = entityState.chestEquipment;
        //Note: When the stack is empty the helper will fall back to an implementation that returns the proper data
        // TODO should this be moved into the render state?
        genderArmor = WildfireHelper.getArmorConfig(armorStack);
        isChestplateOccupied = genderArmor.coversBreasts() && !genderState.armorPhysicsOverride;
        if(genderArmor.alwaysHidesBreasts() || !genderState.showBreastsInArmor && isChestplateOccupied) {
            //If the armor always hides breasts or there is armor and the player configured breasts
            // to be hidden when wearing armor, we can just exit early rather than doing any calculations
            return false;
        }

        if(!isLayerVisible(entityState)) {
            return false;
        }

        GenderRenderState.BreastState breasts = genderState.breasts;
        breastOffsetX = WildfireHelper.round(breasts.xOffset, 1);
        breastOffsetY = -WildfireHelper.round(breasts.yOffset, 1);
        breastOffsetZ = -WildfireHelper.round(breasts.zOffset, 1);

        isUniboob = breasts.uniboob;

        GenderRenderState.BreastPhysicsState leftPhysicsState = genderState.leftBreastPhysics;
        final float bSize = leftPhysicsState.getBreastSize();
        outwardAngle = Math.round(breasts.cleavage * 100f);
        outwardAngle = Math.min(outwardAngle, 10);

        resizeBox(genderState, bSize);

        lPhysPositionY = leftPhysicsState.getPositionY();
        lPhysPositionX = leftPhysicsState.getPositionX();
        lPhysBounceRotation = leftPhysicsState.getBounceRotation();
        if(isUniboob) {
            rPhysPositionY = lPhysPositionY;
            rPhysPositionX = lPhysPositionX;
            rPhysBounceRotation = lPhysBounceRotation;
        } else {
            GenderRenderState.BreastPhysicsState rightPhysicsState = genderState.rightBreastPhysics;
            rPhysPositionY = rightPhysicsState.getPositionY();
            rPhysPositionX = rightPhysicsState.getPositionX();
            rPhysBounceRotation = rightPhysicsState.getBounceRotation();
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

        float resistance = Mth.clamp(genderArmor.physicsResistance(), 0, 1);
        breathingAnimation = ((genderState.armorPhysicsOverride || resistance <= 0.5F) && genderState.isBreathing);
        bounceEnabled = genderState.hasBreastPhysics && (!isChestplateOccupied || resistance < 1); //oh, you found this?
        return true;
    }

    protected boolean isLayerVisible(S state) {
        return !state.isInvisibleToPlayer || state.appearsGlowing();
    }

    protected void resizeBox(GenderRenderState state, float breastSize) {
        //TODO: Better way for this?
        if(!Objects.equals(this.prevLeftBreastUVLayout, state.leftBreastUVLayout)
                || !Objects.equals(this.prevRightBreastUVLayout, state.rightBreastUVLayout)
                || !Objects.equals(this.prevLeftBreastOverlayUVLayout, state.leftBreastOverlayUVLayout)
                || !Objects.equals(this.prevRightBreastOverlayUVLayout, state.rightBreastOverlayUVLayout)) {

            this.prevLeftBreastUVLayout = state.leftBreastUVLayout;
            this.prevRightBreastUVLayout = state.rightBreastUVLayout;
            this.prevLeftBreastOverlayUVLayout = state.leftBreastOverlayUVLayout;
            this.prevRightBreastOverlayUVLayout = state.rightBreastOverlayUVLayout;

            this.lBreast = new BreastModelBox(64, 64, -4F, 0.0F, 0F, 4, 5, 3, 0.0F, state.leftBreastUVLayout);
            this.rBreast = new BreastModelBox(64, 64, 0F, 0.0F, 0F, 4, 5, 3, 0.0F, state.rightBreastUVLayout);
            this.lBreastWear = new OverlayModelBox(64, 64, -4F, 0.0F, 0F, 4, 5, 3, 0.0F, state.leftBreastOverlayUVLayout);
            this.rBreastWear = new OverlayModelBox(64, 64, 0, 0.0F, 0F, 4, 5, 3, 0.0F, state.rightBreastOverlayUVLayout);
        }
    }

    protected void setupTransformations(S state, M model, PoseStack matrixStack, BreastSide side) {
        if(state.isBaby) {
            matrixStack.scale(state.ageScale, state.ageScale, state.ageScale);
            matrixStack.translate(0f, 0.75f, 0f);
        }

        model.root().translateAndRotate(matrixStack);
        ModelPart body = model.body;
        body.translateAndRotate(matrixStack);

        if(bounceEnabled) {
            matrixStack.translate((side.isLeft ? lPhysPositionX : rPhysPositionX) / 32f, 0, 0);
            matrixStack.translate(0, (side.isLeft ? lPhysPositionY : rPhysPositionY) / 32f, 0);
        }

        matrixStack.translate((side.isLeft ? breastOffsetX : -breastOffsetX) * 0.0625f, 0.05625f + (breastOffsetY * 0.0625f), zOffset - 0.0625f * 2f + (breastOffsetZ * 0.0425f)); //shift down to correct position

        if(!isUniboob) {
            matrixStack.translate(-0.0625f * 2 * (side.isLeft ? 1 : -1), 0, 0);
        }
        if(bounceEnabled) {
            matrixStack.mulPose(new Quaternionf().rotationXYZ(0, (float)((side.isLeft ? lPhysBounceRotation : rPhysBounceRotation) * (Math.PI / 180f)), 0));
        }
        if(!isUniboob) {
            matrixStack.translate(0.0625f * 2 * (side.isLeft ? 1 : -1), 0, 0);
        }

        float rotation = breastSize;
        if(bounceEnabled) {
            matrixStack.translate(0, -0.035f * breastSize, 0); //shift down to correct position
            rotation -= (side.isLeft ? lPhysPositionY : rPhysPositionY) / 12f;
        }

        rotation = Math.min(rotation, breastSize + 0.2f);
        rotation = Math.min(rotation, 1); //hard limit for MAX

        if(isChestplateOccupied) {
            matrixStack.translate(0, 0, 0.01f);
        }

        Quaternionf rotationTransform = new Quaternionf()
                .rotationY((side.isLeft ? outwardAngle : -outwardAngle) * DEG_TO_RAD)
                .rotateX(-35f * rotation * DEG_TO_RAD);

        if(breathingAnimation) {
            float f5 = -Mth.cos(state.ageInTicks * 0.09F) * 0.45F + 0.45F;
            rotationTransform.rotateX(f5 * DEG_TO_RAD);
        }

        matrixStack.mulPose(rotationTransform);
        matrixStack.scale(0.9995f, 1f, 1f); //z-fighting FIXXX
    }

    private void renderBreast(S state, PoseStack poseStack, SubmitNodeCollector collector, int overlay, BreastSide side) {
        RenderType type = getRenderLayer(state);
        if(type == null) return; // only render if the player is visible in some capacity

        int alpha = state.isInvisible ? ARGB.as8BitChannel(0.15f) : 255;
        int color = ARGB.color(alpha, 255, 255, 255);

        var model = side.isLeft ? lBreast : rBreast;
        collector.order(1).submitModel(new BreastModel(model), state, poseStack, type, state.lightCoords, overlay, color, null, state.outlineColor, null);

        if(state instanceof AvatarRenderState playerState && playerState.showJacket) {
            poseStack.translate(0, 0, -0.015f);
            poseStack.scale(1.05f, 1.05f, 1.05f);
            var jacketModel = side.isLeft ? lBreastWear : rBreastWear;
            collector.order(2).submitModel(new BreastModel(jacketModel), state, poseStack, type, state.lightCoords, overlay, color, null, state.outlineColor, null);
        }
    }

    protected void renderSides(S state, M model, PoseStack matrixStack, Consumer<BreastSide> renderer) {
        matrixStack.pushPose();
        try {
            setupTransformations(state, model, matrixStack, BreastSide.LEFT);
            renderer.accept(BreastSide.LEFT);
        } finally {
            matrixStack.popPose();
        }

        matrixStack.pushPose();
        try {
            setupTransformations(state, model, matrixStack, BreastSide.RIGHT);
            renderer.accept(BreastSide.RIGHT);
        } finally {
            matrixStack.popPose();
        }
    }
}

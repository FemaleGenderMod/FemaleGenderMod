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
import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.PlayerConfig;
import java.util.Calendar;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class HolidayFeaturesRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private final ModelPart santaHat;

    private static final ResourceLocation SANTA_HAT = WildfireGender.rl("textures/santa_hat.png");

    private final boolean christmas = isAroundChristmas();
    private final RenderType hatRenderType;

    public HolidayFeaturesRenderer(PlayerRenderer renderer) {
        super(renderer);
        santaHat = createSantaHat().bakeRoot();
        this.hatRenderType = net.minecraft.client.renderer.RenderType.entityTranslucent(SANTA_HAT);
    }

    @Override
    public void render(@NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferSource, int light, @NotNull AbstractClientPlayer entity, float limbAngle,
          float limbDistance, float partialTicks, float animationProgress, float headYaw, float headPitch) {
        PlayerConfig config = WildfireGender.getPlayerById(entity.getUUID());
        if (config == null || !config.hasHolidayThemes()) {
            return;
        }

        renderSantaHat(entity, matrixStack, bufferSource, light);
    }

    private void renderSantaHat(AbstractClientPlayer state, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
        if (!christmas) {
            return;
        }

        matrixStack.pushPose();
        try {
            int overlay = LivingEntityRenderer.getOverlayCoords(state, 0);
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(hatRenderType);

            if (state.isBaby()) {
                float ageScale = state.getAgeScale();
                matrixStack.scale(ageScale, ageScale, ageScale);
                matrixStack.translate(0, 0.75F, 0);
            }

            ModelPart mPart = getParentModel().head;
            mPart.translateAndRotate(matrixStack);
            santaHat.render(matrixStack, vertexConsumer, light, overlay);
        } catch (Exception e) {
            WildfireGender.LOGGER.error("Failed to render santa hat", e);
        }
        matrixStack.popPose();
    }

    private static LayerDefinition createSantaHat() {
        CubeDeformation dilation = new CubeDeformation(0.75f);
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition modelPartData = meshDefinition.getRoot();
        modelPartData.addOrReplaceChild("santa_hat", CubeListBuilder.create()
              .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    public static boolean isAroundChristmas() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
    }
}

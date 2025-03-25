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

import com.wildfire.main.WildfireGender;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

import java.util.Calendar;

public class HolidayFeaturesRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
	private final ModelPart santaHat;

	private static final Identifier SANTA_HAT = Identifier.of(WildfireGender.MODID, "textures/santa_hat.png");
	private static final boolean christmas = isAroundChristmas();

	public HolidayFeaturesRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
		super(context);
		santaHat = createSantaHat().createModel();
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
		var entity = ((RenderStateEntityCapture)state).getEntity();
		if(entity == null) return;
		var config = WildfireGender.getPlayerById(entity.getUuid());
		if(config == null || !config.hasHolidayThemes()) return;

		renderSantaHat(state, matrices, vertexConsumers, light);
	}

	private void renderSantaHat(PlayerEntityRenderState state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
		if(!christmas) return;

		matrixStack.push();
		try {
			int overlay = LivingEntityRenderer.getOverlay(state, 0);
			RenderLayer hatRenderType = RenderLayer.getEntityTranslucent(SANTA_HAT);
			if(hatRenderType == null) return;
			VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(hatRenderType);

			if(state.baby) {
				matrixStack.scale(state.ageScale, state.ageScale, state.ageScale);
				matrixStack.translate(0f, 0.75f, 0f);
			}

			ModelPart mPart = getContextModel().head;
			matrixStack.translate(mPart.originX * 0.0625f, mPart.originY * 0.0625f, mPart.originZ * 0.0625f);
			if(mPart.roll != 0.0F || mPart.yaw != 0.0F || mPart.pitch != 0.0F) {
				matrixStack.multiply(new Quaternionf().rotationZYX(mPart.roll, mPart.yaw, mPart.pitch));
			}

			santaHat.render(matrixStack, vertexConsumer, light, overlay);
		} catch(Exception e) {
			WildfireGender.LOGGER.error("Failed to render breast layer", e);
		}
		matrixStack.pop();
	}

	private static TexturedModelData createSantaHat() {
		Dilation dilation = new Dilation(0.75f);
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("santa_hat", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation), ModelTransform.NONE);
		return TexturedModelData.of(modelData, 32, 32);
	}

	public static boolean isAroundChristmas() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
	}
}

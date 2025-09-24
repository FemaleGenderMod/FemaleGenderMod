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
import com.wildfire.main.config.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Calendar;

@Environment(EnvType.CLIENT)
public class HolidayFeaturesRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
	private static final Identifier SANTA_HAT_TEXTURE = Identifier.of(WildfireGender.MODID, "textures/santa_hat.png");
	private static final BipedEntityModel<PlayerEntityRenderState> SANTA_HAT_MODEL = new SantaHatModel();
	private static final boolean christmas = isAroundChristmas();

	public HolidayFeaturesRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
		super(context);
	}

	@Override
	public void render(MatrixStack matrices, OrderedRenderCommandQueue renderQueue, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
		var genderRenderState = GenderRenderState.get(state);
		if (genderRenderState == null || !genderRenderState.hasHolidayThemes) return;

		renderSantaHat(state, matrices, renderQueue, light);
	}

	private void renderSantaHat(PlayerEntityRenderState state, MatrixStack matrixStack, OrderedRenderCommandQueue renderQueue, int light) {
		if(!state.hatVisible) return;
		if(!ClientConfig.INSTANCE.get(ClientConfig.HOLIDAY_COSMETICS).asBoolean(christmas)) return;

		matrixStack.push();
		int overlay = LivingEntityRenderer.getOverlay(state, 0);
		RenderLayer renderLayer = RenderLayer.getEntityTranslucent(SANTA_HAT_TEXTURE);

		if(state.baby) {
			matrixStack.scale(state.ageScale, state.ageScale, state.ageScale);
			matrixStack.translate(0f, 0.75f, 0f);
		}

		matrixStack.scale(1.145f, 1.145f, 1.145f);
		renderQueue.submitModel(SANTA_HAT_MODEL, state, matrixStack, renderLayer, light, overlay, state.outlineColor, null);
		matrixStack.pop();
	}

	public static boolean isAroundChristmas() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
	}

	private static class SantaHatModel extends PlayerEntityModel {
		public SantaHatModel() {
			super(createSantaHat().createModel(), false);
		}

		private static TexturedModelData createSantaHat() {
			var root = PlayerEntityModel.getTexturedModelData(Dilation.NONE, false);
			var clearedRoot = root.getRoot().resetChildrenParts();
			var headPart = clearedRoot.getChild(EntityModelPartNames.HEAD);
			headPart.addChild("santa_hat", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, Dilation.NONE), ModelTransform.NONE);
			return TexturedModelData.of(root, 32, 32);
		}
	}
}
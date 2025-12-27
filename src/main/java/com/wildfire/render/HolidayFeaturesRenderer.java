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
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.util.Calendar;

@Environment(EnvType.CLIENT)
public class HolidayFeaturesRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private static final Identifier SANTA_HAT_TEXTURE = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/santa_hat.png");
	private static final HumanoidModel<AvatarRenderState> SANTA_HAT_MODEL = new SantaHatModel();
	private static final boolean christmas = isAroundChristmas();

	public HolidayFeaturesRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> context) {
		super(context);
	}

	@Override
	public void submit(PoseStack matrices, SubmitNodeCollector renderQueue, int light, AvatarRenderState state, float limbAngle, float limbDistance) {
		var genderRenderState = GenderRenderState.get(state);
		if (genderRenderState == null || !genderRenderState.hasHolidayThemes) return;

		renderSantaHat(state, matrices, renderQueue, light);
	}

	private void renderSantaHat(AvatarRenderState state, PoseStack matrixStack, SubmitNodeCollector renderQueue, int light) {
		if(state.isInvisible) return;
		if(!state.showHat) return;
		if(!ClientConfig.INSTANCE.get(ClientConfig.HOLIDAY_COSMETICS).toBoolean(christmas)) return;

		matrixStack.pushPose();
		int overlay = LivingEntityRenderer.getOverlayCoords(state, 0);
		RenderType renderLayer = RenderTypes.entityTranslucent(SANTA_HAT_TEXTURE);

		if(state.isBaby) {
			matrixStack.scale(state.ageScale, state.ageScale, state.ageScale);
			matrixStack.translate(0f, 0.75f, 0f);
		}

		matrixStack.scale(1.145f, 1.145f, 1.145f);
		renderQueue.submitModel(SANTA_HAT_MODEL, state, matrixStack, renderLayer, light, overlay, state.outlineColor, null);
		matrixStack.popPose();
	}

	public static boolean isAroundChristmas() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
	}

	private static class SantaHatModel extends PlayerModel {
		public SantaHatModel() {
			super(createSantaHat().bakeRoot(), false);
		}

		private static LayerDefinition createSantaHat() {
			var root = PlayerModel.createMesh(CubeDeformation.NONE, false);
			var clearedRoot = root.getRoot().clearRecursively();
			var headPart = clearedRoot.getChild(PartNames.HEAD);
			headPart.addOrReplaceChild("santa_hat", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE), PartPose.ZERO);
			return LayerDefinition.create(root, 32, 32);
		}
	}
}
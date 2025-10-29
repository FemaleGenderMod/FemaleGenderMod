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
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

@Environment(EnvType.CLIENT)
public record BreastRenderCommand(
		WildfireModelRenderer.ModelBox model,
		int light,
		int overlay,
		int color,
		int outline,
		@Nullable UnaryOperator<VertexConsumer> consumerOperator
) implements SubmitNodeCollector.CustomGeometryRenderer {
	public BreastRenderCommand(WildfireModelRenderer.ModelBox model, LivingEntityRenderState state, int overlay, int color) {
		this(model, state.lightCoords, overlay, color, state.outlineColor, null);
	}

	public static BreastRenderCommand trim(WildfireModelRenderer.ModelBox model, LivingEntityRenderState state, TextureAtlasSprite trimSprite) {
		return new BreastRenderCommand(model, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, 0, trimSprite::wrap);
	}

	@Override
	public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
		if(consumerOperator != null) {
			vertexConsumer = consumerOperator.apply(vertexConsumer);
		}
		GenderLayer.renderBox(model, matricesEntry, vertexConsumer, light, overlay, color);
	}
}

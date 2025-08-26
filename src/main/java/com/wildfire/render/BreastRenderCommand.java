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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
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
) implements OrderedRenderCommandQueue.Custom {
	public BreastRenderCommand(WildfireModelRenderer.ModelBox model, LivingEntityRenderState state, int overlay, int color) {
		this(model, state.light, overlay, color, state.outlineColor, null);
	}

	public static BreastRenderCommand trim(WildfireModelRenderer.ModelBox model, LivingEntityRenderState state, Sprite trimSprite) {
		return new BreastRenderCommand(model, state.light, OverlayTexture.DEFAULT_UV, -1, 0, trimSprite::getTextureSpecificVertexConsumer);
	}

	@Override
	public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
		if(consumerOperator != null) {
			vertexConsumer = consumerOperator.apply(vertexConsumer);
		}
		GenderLayer.renderBox(model, matricesEntry, vertexConsumer, light, overlay, color);
	}
}

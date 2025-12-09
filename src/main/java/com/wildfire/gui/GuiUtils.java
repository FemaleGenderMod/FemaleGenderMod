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

package com.wildfire.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public final class GuiUtils {
	public enum Justify {
		LEFT, CENTER
	}

	private static final float ENTITY_SCALE = 0.0625F;
	private static final double HALF_PI = Math.PI / 2;
	private static final double DOUBLE_PI = Math.PI * 2;

	private GuiUtils() {
		throw new UnsupportedOperationException();
	}

	public static MutableComponent doneNarrationText() {
		return Component.translatable("gui.narrate.button", Component.translatable("gui.done"));
	}

	// Reimplementation of DrawContext#drawCenteredTextWithShadow but with the text shadow removed
	public static void drawCenteredText(GuiGraphics ctx, Font font, Component text, int x, int y, int color) {
		int centeredX = x - font.width(text) / 2;
		ctx.drawString(font, text, centeredX, y, color, false);
	}

	public static void drawCenteredText(GuiGraphics ctx, Font font, FormattedCharSequence text, int x, int y, int color) {
		int centeredX = x - font.width(text) / 2;
		ctx.drawString(font, text, centeredX, y, color, false);
	}

	public static void drawCenteredTextWrapped(GuiGraphics ctx, Font font, FormattedText text, int x, int y, int width, int color) {
		for(var var7 = font.split(text, width).iterator(); var7.hasNext(); y += 9) {
			FormattedCharSequence orderedText = var7.next();
			GuiUtils.drawCenteredText(ctx, font, orderedText, x, y, color);
		}
	}

	// Reimplementation of ClickableWidget#drawScrollableText but with the text shadow removed
	public static void drawScrollableTextWithoutShadow(Justify justify, GuiGraphics context, Font font, Component text, int left, int top, int right, int bottom, int color) {
		color = ARGB.opaque(color);
		int i = font.width(text);
		int j = (top + bottom - 9) / 2 + 1;
		int k = right - left;
		if (i > k) {
			int l = i - k;
			double d = Util.getMillis() / 1000.0;
			double e = Math.max(l * 0.5, 3.0);
			double f = Math.sin(HALF_PI * Math.cos(DOUBLE_PI * d / e)) / 2.0 + 0.5;
			double g = Mth.lerp(f, 0.0, l);
			context.enableScissor(left, top, right, bottom);
			context.drawString(font, text, left - (int)g, j, color, false);
			context.disableScissor();
		} else {
			if(justify == Justify.CENTER) {
				drawCenteredText(context, font, text, (left + right) / 2, j, color);
			} else if(justify == Justify.LEFT) {
				context.drawString(font, text, left, j, color, false);
			}
		}
	}

	// copy of InventoryScreen#renderEntityInInventoryFollowsMouse that allows for applying an X/Y offset to the drawn entity
	public static void drawEntityOnScreen(GuiGraphics graphics, int x1, int y1, int x2, int y2, int size, float mouseX, float mouseY, float xOffset, float yOffset, LivingEntity entity) {
		float centerX = (x1 + x2) / 2.0F;
		float centerY = (y1 + y2) / 2.0F;
		float xAngle = (float)Math.atan((centerX - mouseX) / 40.0F);
		float yAngle = (float)Math.atan((centerY - mouseY) / 40.0F);
		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 20.0F * (float) (Math.PI / 180.0));
		rotation.mul(xRotation);
		EntityRenderState entityRenderState = InventoryScreen.extractRenderState(entity);
		if (entityRenderState instanceof LivingEntityRenderState livingEntityRenderState) {
			livingEntityRenderState.bodyRot = 180.0F + xAngle * 20.0F;
			livingEntityRenderState.yRot = xAngle * 20.0F;
			if (livingEntityRenderState.pose != Pose.FALL_FLYING) {
				livingEntityRenderState.xRot = -yAngle * 20.0F;
			} else {
				livingEntityRenderState.xRot = 0.0F;
			}

			livingEntityRenderState.boundingBoxWidth = livingEntityRenderState.boundingBoxWidth / livingEntityRenderState.scale;
			livingEntityRenderState.boundingBoxHeight = livingEntityRenderState.boundingBoxHeight / livingEntityRenderState.scale;
			livingEntityRenderState.scale = 1.0F;
		}

		Vector3f translation = new Vector3f(xOffset, entityRenderState.boundingBoxHeight / 2.0F + ENTITY_SCALE + yOffset, 0.0F);
		graphics.submitEntityRenderState(entityRenderState, size, translation, rotation, xRotation, x1, y1, x2, y2);
	}

	// TODO this could probably be removed and replaced with references to the real method we're copying here
	public static void drawEntityOnScreen(GuiGraphics graphics, int x1, int y1, int x2, int y2, int size, float mouseX, float mouseY, LivingEntity entity) {
		drawEntityOnScreen(graphics, x1, y1, x2, y2, size, mouseX, mouseY, 0f, 0f, entity);
	}
}

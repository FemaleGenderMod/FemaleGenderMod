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
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class GuiUtils {
	public enum Justify {
		LEFT, CENTER
	}

	private static final double HALF_PI = Math.PI / 2;
	private static final double DOUBLE_PI = Math.PI * 2;

	private GuiUtils() {
		throw new UnsupportedOperationException();
	}

	public static MutableComponent doneNarrationText() {
		return Component.translatable("gui.narrate.button", Component.translatable("gui.done"));
	}

	// Reimplementation of DrawContext#drawCenteredTextWithShadow but with the text shadow removed
	public static void drawCenteredText(GuiGraphics ctx, Font textRenderer, Component text, int x, int y, int color) {
		int centeredX = x - textRenderer.width(text) / 2;
		ctx.drawString(textRenderer, text, centeredX, y, color, false);
	}

	public static void drawCenteredText(GuiGraphics ctx, Font textRenderer, FormattedCharSequence text, int x, int y, int color) {
		int centeredX = x - textRenderer.width(text) / 2;
		ctx.drawString(textRenderer, text, centeredX, y, color, false);
	}

	public static void drawCenteredTextWrapped(GuiGraphics ctx, Font textRenderer, FormattedText text, int x, int y, int width, int color) {
		for(var var7 = textRenderer.split(text, width).iterator(); var7.hasNext(); y += 9) {
			FormattedCharSequence orderedText = var7.next();
			GuiUtils.drawCenteredText(ctx, textRenderer, orderedText, x, y, color);
			Objects.requireNonNull(textRenderer);
		}

	}

	// Reimplementation of ClickableWidget#drawScrollableText but with the text shadow removed
	public static void drawScrollableTextWithoutShadow(Justify justify, GuiGraphics context, Font textRenderer, Component text, int left, int top, int right, int bottom, int color) {
		color = ARGB.opaque(color);
		int i = textRenderer.width(text);
		int j = (top + bottom - 9) / 2 + 1;
		int k = right - left;
		if (i > k) {
			int l = i - k;
			double d = Util.getMillis() / 1000.0;
			double e = Math.max(l * 0.5, 3.0);
			double f = Math.sin(HALF_PI * Math.cos(DOUBLE_PI * d / e)) / 2.0 + 0.5;
			double g = Mth.lerp(f, 0.0, l);
			context.enableScissor(left, top, right, bottom);
			context.drawString(textRenderer, text, left - (int)g, j, color, false);
			context.disableScissor();
		} else {
			if(justify == Justify.CENTER) {
				drawCenteredText(context, textRenderer, text, (left + right) / 2, j, color);
			} else if(justify == Justify.LEFT) {
				context.drawString(textRenderer, text, left, j, color, false);
			}
		}
	}

	// Copy of InventoryScreen#drawEntity that doesn't call DrawContext#enableScissor or DrawContext#disableScissor
	public static void drawEntityOnScreenNoScissor(GuiGraphics context, int x1, int y1, int x2, int y2, int size, float mouseX, float mouseY, LivingEntity entity) {
		float f = 0.0625F;
		float g = (x1 + x2) / 2.0F;
		float h = (y1 + y2) / 2.0F;
		float i = (float)Math.atan((g - mouseX) / 40.0F);
		float j = (float)Math.atan((h - mouseY) / 40.0F);
		Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf quaternionf2 = new Quaternionf().rotateX(j * 20.0F * (float) (Math.PI / 180.0));
		quaternionf.mul(quaternionf2);
		float k = entity.yBodyRot;
		float l = entity.getYRot();
		float m = entity.getXRot();
		float n = entity.yHeadRotO;
		float o = entity.yHeadRot;
		entity.yBodyRot = 180.0F + i * 20.0F;
		entity.setYRot(180.0F + i * 40.0F);
		entity.setXRot(-j * 20.0F);
		entity.yHeadRot = entity.getYRot();
		entity.yHeadRotO = entity.getYRot();
		float p = entity.getScale();
		Vector3f vector3f = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + f * p, 0.0F);
		float q = size / p;
		InventoryScreen.renderEntityInInventory(context, x1, y1, x2, y2, q, vector3f, quaternionf, quaternionf2, entity);
		entity.yBodyRot = k;
		entity.setYRot(l);
		entity.setXRot(m);
		entity.yHeadRotO = n;
		entity.yHeadRot = o;
	}

	// Copy of InventoryScreen#drawEntity that doesn't call DrawContext#enableScissor or DrawContext#disableScissor
	// Allows adjusting entity x and y offsets.
	public static void drawEntityOnScreenNoScissor(GuiGraphics context, float entXOff, float entYOff, int x1, int y1, int x2, int y2, int size, float mouseX, float mouseY, LivingEntity entity) {
		float f = 0.0625F;
		float g = (x1 + x2) / 2.0F;
		float h = (y1 + y2) / 2.0F;
		float i = (float)Math.atan((g - mouseX) / 40.0F);
		float j = (float)Math.atan((h - mouseY) / 40.0F);
		Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf quaternionf2 = new Quaternionf().rotateX(j * 20.0F * (float) (Math.PI / 180.0));
		quaternionf.mul(quaternionf2);
		float k = entity.yBodyRot;
		float l = entity.getYRot();
		float m = entity.getXRot();
		float n = entity.yHeadRotO;
		float o = entity.yHeadRot;
		entity.yBodyRot = 180.0F + i * 20.0F;
		entity.setYRot(180.0F + i * 40.0F);
		entity.setXRot(-j * 20.0F);
		entity.yHeadRot = entity.getYRot();
		entity.yHeadRotO = entity.getYRot();
		float p = entity.getScale();
		Vector3f vector3f = new Vector3f(entXOff, entity.getBbHeight() / 2.0F + f * p + entYOff, 0.0F);
		float q = size / p;
		InventoryScreen.renderEntityInInventory(context, x1, y1, x2, y2, q, vector3f, quaternionf, quaternionf2, entity);
		entity.yBodyRot = k;
		entity.setYRot(l);
		entity.setXRot(m);
		entity.yHeadRotO = n;
		entity.yHeadRot = o;
	}

	//Unknown if I want to use this yet, but it's here for now.
	public static final Component FEMALE_GENDER_MOD_LOGO_TEXT = Component.empty()
			.append(Component.literal("F").withStyle(ChatFormatting.LIGHT_PURPLE))
			.append(Component.literal("emale").withStyle(ChatFormatting.WHITE))
			.append(" ")
			.append(Component.literal("G").withStyle(ChatFormatting.LIGHT_PURPLE))
			.append(Component.literal("ender").withStyle(ChatFormatting.WHITE))
			.append(" ")
			.append(Component.literal("M").withStyle(ChatFormatting.LIGHT_PURPLE))
			.append(Component.literal("od").withStyle(ChatFormatting.WHITE));
}

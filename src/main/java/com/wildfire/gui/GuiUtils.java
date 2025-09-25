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
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
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

	public static MutableText doneNarrationText() {
		return Text.translatable("gui.narrate.button", Text.translatable("gui.done"));
	}

	// Reimplementation of DrawContext#drawCenteredTextWithShadow but with the text shadow removed
	public static void drawCenteredText(DrawContext ctx, TextRenderer textRenderer, Text text, int x, int y, int color) {
		int centeredX = x - textRenderer.getWidth(text) / 2;
		ctx.drawText(textRenderer, text, centeredX, y, color, false);
	}

	public static void drawCenteredText(DrawContext ctx, TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
		int centeredX = x - textRenderer.getWidth(text) / 2;
		ctx.drawText(textRenderer, text, centeredX, y, color, false);
	}

	public static void drawCenteredTextWrapped(DrawContext ctx, TextRenderer textRenderer, StringVisitable text, int x, int y, int width, int color) {
		for(var var7 = textRenderer.wrapLines(text, width).iterator(); var7.hasNext(); y += 9) {
			OrderedText orderedText = var7.next();
			GuiUtils.drawCenteredText(ctx, textRenderer, orderedText, x, y, color);
			Objects.requireNonNull(textRenderer);
		}

	}

	// Reimplementation of ClickableWidget#drawScrollableText but with the text shadow removed
	public static void drawScrollableTextWithoutShadow(Justify justify, DrawContext context, TextRenderer textRenderer, Text text, int left, int top, int right, int bottom, int color) {
		color = ColorHelper.fullAlpha(color);
		int i = textRenderer.getWidth(text);
		int j = (top + bottom - 9) / 2 + 1;
		int k = right - left;
		if (i > k) {
			int l = i - k;
			double d = Util.getMeasuringTimeMs() / 1000.0;
			double e = Math.max(l * 0.5, 3.0);
			double f = Math.sin(HALF_PI * Math.cos(DOUBLE_PI * d / e)) / 2.0 + 0.5;
			double g = MathHelper.lerp(f, 0.0, l);
			context.enableScissor(left, top, right, bottom);
			context.drawText(textRenderer, text, left - (int)g, j, color, false);
			context.disableScissor();
		} else {
			if(justify == Justify.CENTER) {
				drawCenteredText(context, textRenderer, text, (left + right) / 2, j, color);
			} else if(justify == Justify.LEFT) {
				context.drawText(textRenderer, text, left, j, color, false);
			}
		}
	}

	// Copy of InventoryScreen#drawEntity that doesn't call DrawContext#enableScissor or DrawContext#disableScissor
	public static void drawEntityOnScreenNoScissor(DrawContext context, int x1, int y1, int x2, int y2, int size, float mouseX, float mouseY, LivingEntity entity) {
		float f = 0.0625F;
		float g = (x1 + x2) / 2.0F;
		float h = (y1 + y2) / 2.0F;
		float i = (float)Math.atan((g - mouseX) / 40.0F);
		float j = (float)Math.atan((h - mouseY) / 40.0F);
		Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf quaternionf2 = new Quaternionf().rotateX(j * 20.0F * (float) (Math.PI / 180.0));
		quaternionf.mul(quaternionf2);
		float k = entity.bodyYaw;
		float l = entity.getYaw();
		float m = entity.getPitch();
		float n = entity.lastHeadYaw;
		float o = entity.headYaw;
		entity.bodyYaw = 180.0F + i * 20.0F;
		entity.setYaw(180.0F + i * 40.0F);
		entity.setPitch(-j * 20.0F);
		entity.headYaw = entity.getYaw();
		entity.lastHeadYaw = entity.getYaw();
		float p = entity.getScale();
		Vector3f vector3f = new Vector3f(0.0F, entity.getHeight() / 2.0F + f * p, 0.0F);
		float q = size / p;
		InventoryScreen.drawEntity(context, x1, y1, x2, y2, q, vector3f, quaternionf, quaternionf2, entity);
		entity.bodyYaw = k;
		entity.setYaw(l);
		entity.setPitch(m);
		entity.lastHeadYaw = n;
		entity.headYaw = o;
	}

	// Copy of InventoryScreen#drawEntity that doesn't call DrawContext#enableScissor or DrawContext#disableScissor
	// Allows adjusting entity x and y offsets.
	public static void drawEntityOnScreenNoScissor(DrawContext context, float entXOff, float entYOff, int x1, int y1, int x2, int y2, int size, float mouseX, float mouseY, LivingEntity entity) {
		float f = 0.0625F;
		float g = (x1 + x2) / 2.0F;
		float h = (y1 + y2) / 2.0F;
		float i = (float)Math.atan((g - mouseX) / 40.0F);
		float j = (float)Math.atan((h - mouseY) / 40.0F);
		Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf quaternionf2 = new Quaternionf().rotateX(j * 20.0F * (float) (Math.PI / 180.0));
		quaternionf.mul(quaternionf2);
		float k = entity.bodyYaw;
		float l = entity.getYaw();
		float m = entity.getPitch();
		float n = entity.lastHeadYaw;
		float o = entity.headYaw;
		entity.bodyYaw = 180.0F + i * 20.0F;
		entity.setYaw(180.0F + i * 40.0F);
		entity.setPitch(-j * 20.0F);
		entity.headYaw = entity.getYaw();
		entity.lastHeadYaw = entity.getYaw();
		float p = entity.getScale();
		Vector3f vector3f = new Vector3f(entXOff, entity.getHeight() / 2.0F + f * p + entYOff, 0.0F);
		float q = size / p;
		InventoryScreen.drawEntity(context, x1, y1, x2, y2, q, vector3f, quaternionf, quaternionf2, entity);
		entity.bodyYaw = k;
		entity.setYaw(l);
		entity.setPitch(m);
		entity.lastHeadYaw = n;
		entity.headYaw = o;
	}

	//Unknown if I want to use this yet, but it's here for now.
	public static final Text FEMALE_GENDER_MOD_LOGO_TEXT = Text.empty()
			.append(Text.literal("F").formatted(Formatting.LIGHT_PURPLE))
			.append(Text.literal("emale").formatted(Formatting.WHITE))
			.append(" ")
			.append(Text.literal("G").formatted(Formatting.LIGHT_PURPLE))
			.append(Text.literal("ender").formatted(Formatting.WHITE))
			.append(" ")
			.append(Text.literal("M").formatted(Formatting.LIGHT_PURPLE))
			.append(Text.literal("od").formatted(Formatting.WHITE));
}

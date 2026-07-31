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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public final class GuiUtils {
    public enum Justify {
        LEFT, CENTER
    }

    public static final float ENTITY_SCALE = 0.0625F;
    private static final double HALF_PI = Math.PI / 2;
    private static final double DOUBLE_PI = Math.PI * 2;

    private GuiUtils() {
        throw new UnsupportedOperationException();
    }

    public static MutableComponent doneNarrationText() {
        return Component.translatable("gui.narrate.button", Component.translatable("gui.done"));
    }

    // Reimplementation of DrawContext#drawCenteredTextWithShadow but with the text shadow removed
    public static void drawCenteredText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color) {
        int centeredX = x - font.width(text) / 2;
        graphics.text(font, text, centeredX, y, color, false);
    }

    public static void drawCenteredText(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        int centeredX = x - font.width(text) / 2;
        graphics.text(font, text, centeredX, y, color, false);
    }

    public static void drawCenteredTextWrapped(GuiGraphicsExtractor graphics, Font font, FormattedText text, int x, int y, int width, int color) {
        for(var var7 = font.split(text, width).iterator(); var7.hasNext(); y += 9) {
            FormattedCharSequence orderedText = var7.next();
            drawCenteredText(graphics, font, orderedText, x, y, color);
        }
    }

    // Reimplementation of ClickableWidget#drawScrollableText but with the text shadow removed
    public static void drawScrollableTextWithoutShadow(Justify justify, GuiGraphicsExtractor graphics, Font font, Component text, int left, int top, int right, int bottom, int color) {
        int i = font.width(text);
        int j = (top + bottom - 9) / 2 + 1;
        int k = right - left;
        if (i > k) {
            int l = i - k;
            double d = Util.getMillis() / 1000.0;
            double e = Math.max(l * 0.5, 3.0);
            double f = Math.sin(HALF_PI * Math.cos(DOUBLE_PI * d / e)) / 2.0 + 0.5;
            double g = Mth.lerp(f, 0.0, l);
            graphics.enableScissor(left, top, right, bottom);
            graphics.text(font, text, left - (int)g, j, color, false);
            graphics.disableScissor();
        } else {
            if(justify == Justify.CENTER) {
                drawCenteredText(graphics, font, text, (left + right) / 2, j, color);
            } else if(justify == Justify.LEFT) {
                graphics.text(font, text, left, j, color, false);
            }
        }
    }
}

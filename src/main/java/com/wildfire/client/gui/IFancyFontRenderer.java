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

package com.wildfire.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/// A reduced version of a [class from Mekanism](https://github.com/mekanism/Mekanism/blob/1.21.x/src/main/java/mekanism/client/render/IFancyFontRenderer.java)
public interface IFancyFontRenderer {

    default Font font() {
        return Minecraft.getInstance().font;
    }

    /// Time the gui was opened in ms, or zero if the time is unknown (scrolling text will just use the current time then)
    long getTimeOpened();

    default void drawScrollingString(GuiGraphics graphics, Component text, int x, int y, TextAlignment alignment, int color, int width, int maxLengthPad, boolean shadow) {
        drawScrollingString(graphics, text.getVisualOrderText(), x, y, alignment, color, width, maxLengthPad, shadow, -1);
    }

    default void drawScrollingString(GuiGraphics graphics, FormattedCharSequence text, int x, int y, TextAlignment alignment, int color, int width, int maxLengthPad, boolean shadow, long visibleFor) {
        drawScrollingString(graphics, text, x, y, alignment, color, width, font().lineHeight, maxLengthPad, shadow, visibleFor);
    }

    default void drawScrollingString(GuiGraphics graphics, Component text, int x, int y, TextAlignment alignment, int color, int width, int height, int maxLengthPad,
        boolean shadow) {
        drawScrollingString(graphics, text.getVisualOrderText(), x, y, alignment, color, width, height, maxLengthPad, shadow, -1);
    }

    default void drawScrollingString(GuiGraphics graphics, FormattedCharSequence text, int x, int y, TextAlignment alignment, int color, int width, int height,
        int maxLengthPad, boolean shadow, long visibleFor) {
        drawScrollingString(graphics, text, x + maxLengthPad, y, x + width - maxLengthPad, y + height, alignment, color, shadow, visibleFor);
    }

    default void drawScrollingString(GuiGraphics graphics, Component text, int minX, int minY, int maxX, int maxY, TextAlignment alignment, int color,
        boolean shadow) {
        drawScrollingString(graphics, text.getVisualOrderText(), minX, minY, maxX, maxY, alignment, color, shadow, -1);
    }

    default void drawScrollingString(GuiGraphics graphics, FormattedCharSequence text, int minX, int minY, int maxX, int maxY, TextAlignment alignment, int color,
        boolean shadow, long visibleFor) {
        Font font = font();
        int textWidth = font.width(text);
        int areaWidth = maxX - minX;
        boolean isScrolling = textWidth > areaWidth;
        //Note: Instead of doing what vanilla does, we divide to float, and don't add one
        // That way if min and max are not just lineHeight away they will be more accurate, and otherwise it won't render one line below where it should be
        float targetY = (minY + maxY - font.lineHeight) / 2F;
        float targetX;
        if (isScrolling) {
            targetX = prepScrollingString(graphics, font, textWidth, areaWidth, minX, minY, maxX, maxY, visibleFor == -1 ? Util.getMillis() - getTimeOpened() : visibleFor);
        } else {
            targetX = alignment.getTarget(font, minX, maxX, textWidth);
        }
        graphics.drawString(font, text, targetX, targetY, color, shadow);
        if (isScrolling) {
            graphics.disableScissor();
        }
    }

    default void drawScaledScrollingString(GuiGraphics graphics, Component text, int x, int y, TextAlignment alignment, int color, int width,
        int maxLengthPad, boolean shadow, float scale) {
        drawScaledScrollingString(graphics, text, x, y, alignment, color, width, font().lineHeight, maxLengthPad, shadow, scale);
    }

    default void drawScaledScrollingString(GuiGraphics graphics, Component text, int x, int y, TextAlignment alignment, int color, int width, int height,
        int maxLengthPad, boolean shadow, float scale) {
        drawScaledScrollingString(graphics, text, x + maxLengthPad, y, x + width - maxLengthPad, y + height, alignment, color, shadow, scale);
    }

    default void drawScaledScrollingString(GuiGraphics graphics, Component text, int minX, int minY, int maxX, int maxY, TextAlignment alignment, int color, boolean shadow,
        float scale) {
        drawScaledScrollingString(graphics, text, minX, minY, maxX, maxY, alignment, color, shadow, scale, -1);
    }

    default void drawScaledScrollingString(GuiGraphics graphics, Component text, int minX, int minY, int maxX, int maxY, TextAlignment alignment, int color, boolean shadow,
        float scale, long visibleFor) {
        if (Mth.equal(scale, 1)) {
            drawScrollingString(graphics, text.getVisualOrderText(), minX, minY, maxX, maxY, alignment, color, shadow, visibleFor);
            return;
        }
        Font font = font();
        float textWidth = font.width(text) * scale;
        int areaWidth = maxX - minX;
        boolean isScrolling = textWidth > areaWidth;
        //Note: Instead of doing what vanilla does, we divide to float, and don't add one
        // That way if min and max are not just lineHeight away they will be more accurate, and otherwise it won't render one line below where it should be
        float targetY = (minY + maxY - font.lineHeight) / 2F;
        float targetX;
        if (isScrolling) {
            targetX = prepScrollingString(graphics, font, textWidth, areaWidth, minX, minY, maxX, maxY, visibleFor == -1 ? Util.getMillis() - getTimeOpened() : visibleFor);
        } else {
            targetX = alignment.getTarget(font, minX, maxX, textWidth);
        }
        PoseStack matrix = prepTextScale(graphics, font, targetX, targetY, scale);
        graphics.drawString(font, text.getVisualOrderText(), 0, 0, color, shadow);
        matrix.popPose();
        if (isScrolling) {
            graphics.disableScissor();
        }
    }

    /// Based off the logic for calculating the scissor area and draw target that vanilla does in:
    ///
    /// [net.minecraft.client.gui.components.AbstractWidget#renderScrollingString(GuiGraphics, Font, Component, int, int, int, int, int, int)]
    ///
    /// @param visibleDuration Time in ms that this string has been visible for.
    ///
    /// @apiNote Call [GuiGraphics#disableScissor()] after using this method
    private static float prepScrollingString(GuiGraphics graphics, Font font, double textWidth, int areaWidth, int minX, int minY, int maxX, int maxY, long visibleDuration) {
        //Note: We are drawing in relative coordinates, but GuiGraphics#enableScissor, is expecting absolute coordinates,
        // so we need to get the translations from our pose stack
        //Note: This is equivalent to what Matrix4f#getTranslation(Vector3f) would do, without all the extra allocations.
        Matrix4f matrix4f = graphics.pose().last().pose();
        int left = (int) matrix4f.m30();
        int top = (int) matrix4f.m31();
        graphics.enableScissor(left + minX, top + minY, left + maxX, top + maxY);
        //TODO: Re-evaluate this, as for text (especially scaled text) when moving very slowly near the edges, it makes the text a bit blurry
        // Though maybe it is better to just make it not move so insanely slowly near the edges
        //Note: Vanilla casts overflowedBy to an int, as it only bothers drawing based on int pixels.
        // As we already handle and calculates with floats, casting to a float here provides a much smoother looking scroll
        return minX - (float) getOverflowedBy(font, textWidth - areaWidth, visibleDuration);
    }

    private static double getOverflowedBy(Font font, double overflowWidth, long visibleDuration) {
        //Seconds since the gui was opened
        double seconds = visibleDuration / 1_000D;
        //Inlined vanilla scroll constants (AbstractWidget#PERIOD_PER_SCROLLED_PIXEL and AbstractWidget#MIN_SCROLL_PERIOD)
        double scrollPeriod = Math.max(overflowWidth * 150.0D, 2000.0D);
        //Controls the speed at which we go between the start of the scroll and the end
        double scrollSpeedModifier = Math.cos((2 * Math.PI) * seconds / scrollPeriod);
        if (!font.isBidirectional()) {
            //If the text is left to right (such as english). We need to start the modifier at the opposite peak so that it starts
            // at the beginning of the string
            //Note: Mojang doesn't include this negative for rendering text in english, but that is because they just use the current ms
            // for the seconds calculation, which means that they don't care where in the wave they start
            scrollSpeedModifier = -scrollSpeedModifier;
        }
        //Shift it so that the range is from [0, 1]
        double scrolledSoFar = Math.sin((Math.PI / 2) * scrollSpeedModifier) / 2.0 + 0.5;
        //Vanilla uses: Mth.lerp(scrolledSoFar, 0.0, overflowWidth); to calculate overflowedBy. But that is equivalent to just performing the following multiplication
        return scrolledSoFar * overflowWidth;
    }

    //Note: As translate will implicitly cast x and y to being floats, we might as well pass these in as floats to reduce duplicate code
    private static PoseStack prepTextScale(GuiGraphics graphics, Font font, float x, float y, float scale) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        float halfLineHeight = font.lineHeight / 2F;
        float yAdd = halfLineHeight - halfLineHeight * scale;
        pose.translate(x, y + yAdd, 0);
        pose.scale(scale, scale, scale);
        return pose;
    }

    default void drawCenteredTextWrapped(GuiGraphics graphics, FormattedText text, int x, int y, int width, int color) {
        Font font = font();
        for (FormattedCharSequence line : font.split(text, width)) {
            int centeredX = x - font.width(line) / 2;
            graphics.drawString(font, line, centeredX, y, color, false);
            y += font.lineHeight;
        }
    }

    enum TextAlignment {
        LEFT,
        CENTER,
        RIGHT,
        /**
         * Represents that for left to right languages this will be left aligned, and for right to left it will be right aligned.
         */
        RELATIVE;//TODO: Evaluate usages of LEFT and use this instead in places that make sense

        public float getTarget(Font font, int minX, int maxX, float textWidth) {
            return switch (this) {
                case LEFT -> minX;
                case CENTER -> minX + ((maxX - minX) - textWidth) / 2;
                case RIGHT -> maxX - textWidth;
                case RELATIVE -> font.isBidirectional() ? maxX - textWidth : minX;
            };
        }
    }
}

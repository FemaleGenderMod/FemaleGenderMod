package com.wildfire.client.gui;

import java.util.Iterator;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

/**
 * A reduced version of a <a href="https://github.com/mekanism/Mekanism/blob/1.21.x/src/main/java/mekanism/client/render/IFancyFontRenderer.java">class from Mekanism</a>
 */
public interface IFancyFontRenderer {

    default Font font() {
        return Minecraft.getInstance().font;
    }

    /**
     * Time the gui was opened in ms, or zero if the time is unknown (scrolling text will just use the current time then)
     */
    default long getTimeOpened() {
        //TODO - 1.21: Implement this
        return 0;
    }

    default void drawScrollingString(GuiGraphics graphics, Component text, int x, int y, TextAlignment alignment, int color, int width, int maxLengthPad, boolean shadow) {
        drawScrollingString(graphics, text, x, y, alignment, color, width, maxLengthPad, shadow, getTimeOpened());
    }

    default void drawScrollingString(GuiGraphics graphics, Component text, int x, int y, TextAlignment alignment, int color, int width, int maxLengthPad, boolean shadow,
          long msVisible) {
        drawScrollingString(graphics, text, x, y, alignment, color, width, font().lineHeight, maxLengthPad, shadow, msVisible);
    }

    default void drawScrollingString(GuiGraphics graphics, Component text, int x, int y, TextAlignment alignment, int color, int width, int height, int maxLengthPad,
          boolean shadow, long msVisible) {
        drawScrollingString(graphics, text, x + maxLengthPad, y, x + width - maxLengthPad, y + height, alignment, color, shadow, msVisible);
    }

    default void drawScrollingString(GuiGraphics graphics, Component text, int minX, int minY, int maxX, int maxY, TextAlignment alignment, int color, boolean shadow,
          long msVisible) {
        Font font = font();
        int textWidth = font.width(text);
        int areaWidth = maxX - minX;
        boolean isScrolling = textWidth > areaWidth;
        //Note: Instead of doing what vanilla does, we divide to float, and don't add one
        // That way if min and max are not just lineHeight away they will be more accurate, and otherwise it won't render one line below where it should be
        float targetY = (minY + maxY - font.lineHeight) / 2F;
        float targetX;
        if (isScrolling) {
            targetX = prepScrollingString(graphics, font, textWidth, areaWidth, minX, minY, maxX, maxY, Util.getMillis() - msVisible);
        } else {
            targetX = alignment.getTarget(font, minX, maxX, textWidth);
        }
        graphics.drawString(font, text.getVisualOrderText(), targetX, targetY, color, shadow);
        if (isScrolling) {
            graphics.disableScissor();
        }
    }

    /**
     * Based off the logic for calculating the scissor area and draw target that vanilla does in
     * {@link AbstractWidget#renderScrollingString(GuiGraphics, Font, Component, int, int, int, int, int, int)}
     *
     * @param visibleDuration Time in ms that this string has been visible for.
     *
     * @apiNote Call {@link GuiGraphics#disableScissor()} after using this method
     */
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
        double scrollPeriod = Math.max(overflowWidth * AbstractWidget.PERIOD_PER_SCROLLED_PIXEL, AbstractWidget.MIN_SCROLL_PERIOD);
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

    default void drawCenteredText(GuiGraphics graphics, Component text, int x, int y, int color) {
        drawCenteredText(graphics, text.getVisualOrderText(), x, y, color);
    }

    default void drawCenteredText(GuiGraphics graphics, FormattedCharSequence text, int x, int y, int color) {
        //TODO - 1.21: Replace this and other direct calls to GuiGraphics#drawString/drawCenteredString with using the scrolling text
        Font font = font();
        int centeredX = x - font.width(text) / 2;
        graphics.drawString(font, text, centeredX, y, color, false);
    }

    default void drawCenteredTextWrapped(GuiGraphics graphics, FormattedText text, int x, int y, int width, int color) {
        Font font = font();
        for(Iterator<FormattedCharSequence> iterator = font.split(text, width).iterator(); iterator.hasNext(); y += font.lineHeight) {
            drawCenteredText(graphics, iterator.next(), x, y, color);
        }
    }

    enum TextAlignment {
        LEFT,
        CENTER,
        RIGHT,
        /**
         * Represents that for left to right languages this will be left aligned, and for right to left it will be right aligned.
         */
        RELATIVE;//TODO: Make use of this in various spots that make sense

        public float getTarget(Font font, int minX, int maxX, float textWidth) {
            return switch (this) {
                case LEFT -> minX;
                case CENTER -> minX + ((maxX - minX) - textWidth) / 2F;
                case RIGHT -> maxX - textWidth;
                case RELATIVE -> font.isBidirectional() ? maxX - textWidth : minX;
            };
        }
    }
}
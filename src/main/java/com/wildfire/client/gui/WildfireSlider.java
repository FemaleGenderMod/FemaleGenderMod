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

import com.wildfire.client.gui.IFancyFontRenderer.TextAlignment;
import com.wildfire.main.config.FloatConfigKey;
import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class WildfireSlider extends AbstractSliderButton implements IFancyFontRenderer {

    private final double minValue;
    private final double maxValue;
    private final FloatConsumer valueUpdate;
    private final Float2ObjectFunction<Component> messageUpdate;
    private final FloatConsumer onSave;

    private float lastValue;
    private boolean changed;
    private double arrowKeyStep = 0.05;

    public WildfireSlider(int xPos, int yPos, int width, int height, FloatConfigKey config, double currentVal, FloatConsumer valueUpdate,
          Float2ObjectFunction<Component> messageUpdate, FloatConsumer onSave) {
        this(xPos, yPos, width, height, config.getMinInclusive(), config.getMaxInclusive(), currentVal, valueUpdate, messageUpdate, onSave);
    }

    public WildfireSlider(int xPos, int yPos, int width, int height, double minVal, double maxVal, double currentVal, FloatConsumer valueUpdate,
          Float2ObjectFunction<Component> messageUpdate, FloatConsumer onSave) {
        super(xPos, yPos, width, height, Component.empty(), 0);
        this.minValue = minVal;
        this.maxValue = maxVal;
        this.valueUpdate = valueUpdate;
        this.messageUpdate = messageUpdate;
        this.onSave = onSave;
        setValueInternal(currentVal);
    }

    public void setArrowKeyStep(double arrowKeyStep) {
        this.arrowKeyStep = arrowKeyStep;
    }

    @Override
    protected void updateMessage() {
        setMessage(messageUpdate.get(lastValue));
    }

    @Override
    protected void applyValue() {
        float newValue = getFloatValue();
        if (lastValue != newValue) {
            valueUpdate.accept(newValue);
            lastValue = newValue;
            changed = true;
        }
    }

    public void save() {
        if (changed) {
            onSave.accept(lastValue);
            changed = false;
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        save();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
            value += (keyCode == GLFW.GLFW_KEY_LEFT ? -arrowKeyStep : arrowKeyStep);
            value = Mth.clamp(value, 0, 1);
            applyValue();
            updateMessage();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
            save();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 84 << 24);

        graphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, FastColor.ARGB32.color(0x80, 0x222222));

        //Inner Blue Filler
        int xPos = getX() + 2 + (int) (value * (getWidth() - 3));

        graphics.fill(getX() + 1, getY() + 1, xPos - 1, getY() + getHeight() - 1, FastColor.ARGB32.color(0xB4, active ? 0x222266 : 0x111133));

        if (active) {
            int xPos2 = getX() + 3 + (int) (value * (getWidth() - 4));
            graphics.fill(xPos2 - 2, getY() + 1, xPos2, getY() + getHeight() - 1, FastColor.ARGB32.color(0x78, 0xFFFFFF));
        }

        int textColor = (isHoveredOrFocused() && active) || changed ? 0xFFFF55 : 0xFFFFFF;
        if (!active) {
            textColor = 0x666666;
        }
        //Font font = Minecraft.getInstance().font;
        //GuiHelper.renderScrollingString(graphics, this, font, 2, textColor);
        //TODO - 1.21: Re-evaluate the +1 to the height
        drawScrollingString(graphics, getMessage(), getX(), getY(), TextAlignment.CENTER, textColor, getWidth(), getHeight() + 1, 2, false,
              getTimeOpened());
    }

    public float getFloatValue() {
        return (float) getValue();
    }

    public double getValue() {
        return this.value * (maxValue - minValue) + minValue;
    }

    private void setValueInternal(double value) {
        this.value = Mth.clamp((value - this.minValue) / (this.maxValue - this.minValue), 0, 1);
        this.lastValue = (float) value;
        updateMessage();
        //Note: Does not call applyValue
    }
}

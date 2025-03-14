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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

public class WildfireButton extends Button implements IFancyFontRenderer {

    public WildfireButton(int x, int y, int width, int height, Component text, Button.OnPress onPress, CreateNarration narrationSupplier) {
        super(x, y, width, height, text, onPress, narrationSupplier);
    }

    public WildfireButton(int x, int y, int w, int h, Component text, Button.OnPress onPress) {
        this(x, y, w, h, text, onPress, DEFAULT_NARRATION);
    }

    public WildfireButton(int x, int y, int width, int height, Component message, Button.OnPress onPress, Tooltip tooltip) {
        this(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        setTooltip(tooltip);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int clr = 0x444444;
        if (!this.active) {
            clr = 0x222222;
        } else if (this.isHoveredOrFocused()) {
            clr = 0x666666;
        }
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), FastColor.ARGB32.color(0x54, clr));
        renderContents(graphics);
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    protected void renderContents(@NotNull GuiGraphics graphics) {
        int color = active ? 0xFFFFFF : 0x666666;
        //GuiHelper.renderScrollingString(graphics, this, Minecraft.getInstance().font, 2, color);
        //TODO - 1.21: Re-evaluate the +1 to the height
        drawScrollingString(graphics, getMessage(), getX(), getY(), TextAlignment.CENTER, color, getWidth(), getHeight() + 1, 2, false,
              getTimeOpened());
    }
}
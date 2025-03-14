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

import com.wildfire.main.WildfireGender;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WildfireToast implements Toast {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("toast/tutorial");
    private static final ResourceLocation ICON = WildfireGender.rl("textures/bc_ribbon.png");
    public static final int PROGRESS_BAR_WIDTH = 154;
    public static final int PROGRESS_BAR_HEIGHT = 1;
    private final List<FormattedCharSequence> text;
    private Visibility visibility = Visibility.SHOW;
    private long lastTime;
    private float lastProgress;
    private float progress;
    private final boolean hasProgressBar;
    private final int displayDuration;

    public WildfireToast(Font font, Component title, @Nullable Component description, boolean hasProgressBar, int i) {
        this.text = new ArrayList<>(2);
        //TODO - 1.21.4: CommonColors.PURPLE
        this.text.addAll(font.split(title.copy().withColor(0xFF500050), 126));
        if (description != null) {
            this.text.addAll(font.split(description, 126));
        }

        this.hasProgressBar = hasProgressBar;
        this.displayDuration = i;
    }

    public WildfireToast(Font font, Component title, @Nullable Component description, boolean hasProgressBar) {
        this(font, title, description, hasProgressBar, 0);
    }

    //TODO - 1.21.4: Uncomment
    /*@Override
    public void update(ToastManager manager, long time) {
        if(WildfireGenderClient.INSTANCE.toggleEditGUI.isDown()) {
            this.visibility = Visibility.HIDE;
        }
        //this.visibility = (double)time >= 10000.0 * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }*/

    @Override
    public int height() {
        return 7 + getTextHeight() + 3;
    }

    private int getTextHeight() {
        return Math.max(this.text.size(), 2) * 11;
    }

    @NotNull
    @Override
    public Visibility render(@NotNull GuiGraphics graphics, @NotNull ToastComponent toast, long timeSinceLastVisible) {
        int height = height();
        graphics.blitSprite(TEXTURE, 0, 0, width(), height);

        graphics.blit(ICON, 6, 6, 0, 0, 20, 20, 20, 20, 20, 20);
        int j = this.text.size() * 11;
        int k = 7 + (this.getTextHeight() - j) / 2;

        for (int l = 0; l < this.text.size(); l++) {
            graphics.drawString(toast.getMinecraft().font, this.text.get(l), 30, k + l * 11, 0xFF000000, false);
        }

        if (this.hasProgressBar) {
            int l = height - 4;
            graphics.fill(3, l, 157, l + 1, -1);
            int filledColor;
            if (this.progress >= this.lastProgress) {
                filledColor = 0xFF005500;
            } else {
                filledColor = 0xFF550000;
            }

            graphics.fill(3, l, (int)(3.0F + 154.0F * this.lastProgress), l + 1, filledColor);
        }
        return visibility;
    }

    public void hide() {
        this.visibility = Visibility.HIDE;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }
}
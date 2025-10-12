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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wildfire.main.WildfireEventHandler;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireGenderClient;
import com.wildfire.main.config.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.Toast.Visibility;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WildfireToast implements Toast {
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/advancement");
    private static final Identifier ICON = Identifier.of(WildfireGender.MODID, "textures/bc_ribbon.png");
    public static final int PROGRESS_BAR_WIDTH = 154;
    public static final int PROGRESS_BAR_HEIGHT = 1;
    private final List<OrderedText> text;
    private Visibility visibility = Visibility.SHOW;
    private long lastTime;
    private float lastProgress;
    private float progress;
    private final boolean hasProgressBar;
    private final int displayDuration;

    public WildfireToast(TextRenderer textRenderer, Text title, @Nullable Text description, boolean hasProgressBar, int i) {
        this.text = new ArrayList<>(2);
        this.text.addAll(textRenderer.wrapLines(title.copy().withColor(Colors.LIGHT_PINK), 126));
        if (description != null) {
            this.text.addAll(textRenderer.wrapLines(description, 126));
        }

        this.hasProgressBar = hasProgressBar;
        this.displayDuration = i;
    }

    public WildfireToast(TextRenderer textRenderer, Text title, @Nullable Text description, boolean hasProgressBar) {
        this(textRenderer, title, description, hasProgressBar, 0);
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if(WildfireEventHandler.getConfigKeybind().isPressed()) {
            this.visibility = Visibility.HIDE;
            ClientConfig.INSTANCE.set(ClientConfig.SHOW_TOAST, false);
            ClientConfig.INSTANCE.save();
        }
        //this.visibility = (double)time >= 10000.0 * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public int getHeight() {
        return 7 + this.getTextHeight() + 3;
    }

    private int getTextHeight() {
        return Math.max(this.text.size(), 2) * 11;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        int i = this.getHeight();
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.getWidth(), i);

        context.drawTexture(RenderPipelines.GUI_TEXTURED, ICON, 6, 6, 0, 0, 20, 20, 20, 20, 20, 20);
        int j = this.text.size() * 11;
        int k = 7 + (this.getTextHeight() - j) / 2;

        for (int l = 0; l < this.text.size(); l++) {
            context.drawText(textRenderer, (OrderedText)this.text.get(l), 30, k + l * 11, 0xFFFFFFFF, false);
        }
    }

    public void hide() {
        this.visibility = Visibility.HIDE;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }
}

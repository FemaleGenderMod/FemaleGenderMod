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

import com.wildfire.gui.screen.BaseWildfireScreen;
import com.wildfire.main.WildfireEventHandler;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class WildfireToast implements Toast {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("toast/advancement");
	private static final Identifier ICON = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/bc_ribbon.png");
	private final List<FormattedCharSequence> text;
	private Visibility visibility = Visibility.SHOW;

	public WildfireToast(Font textRenderer, Component title, @Nullable Component description) {
		this.text = new ArrayList<>(2);
		this.text.addAll(textRenderer.split(title.copy().withColor(CommonColors.COSMOS_PINK), 126));
		if (description != null) {
			this.text.addAll(textRenderer.split(description, 126));
		}
	}

	@Override
	public Visibility getWantedVisibility() {
		return this.visibility;
	}

	@Override
	public void update(ToastManager manager, long time) {
		if(shouldHide()) {
			hide();
			ClientConfig.INSTANCE.set(ClientConfig.SHOW_TOAST, false);
			CompletableFuture.runAsync(ClientConfig.INSTANCE::save);
		}
	}

	@Override
	public int height() {
		return 7 + this.getTextHeight() + 3;
	}

	private int getTextHeight() {
		return Math.max(this.text.size(), 2) * 11;
	}

	@Override
	public void render(GuiGraphics context, Font textRenderer, long startTime) {
		int i = this.height();
		context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.width(), i);

		context.blit(RenderPipelines.GUI_TEXTURED, ICON, 6, 6, 0, 0, 20, 20, 20, 20, 20, 20);
		int j = this.text.size() * 11;
		int k = 7 + (this.getTextHeight() - j) / 2;

		for (int l = 0; l < this.text.size(); l++) {
			context.drawString(textRenderer, this.text.get(l), 30, k + l * 11, 0xFFFFFFFF, false);
		}
	}

	private boolean shouldHide() {
		Minecraft client = Minecraft.getInstance();
		if(client.screen instanceof BaseWildfireScreen) {
			return true;
		}
		return WildfireEventHandler.getConfigKeybind().isDown();
	}

	public void hide() {
		this.visibility = Visibility.HIDE;
	}
}

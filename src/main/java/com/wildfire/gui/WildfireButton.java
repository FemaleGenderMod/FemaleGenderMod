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

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class WildfireButton extends Button {

	private final @Nullable ButtonRenderer renderer;
	private final Supplier<Component> messageSupplier;
	public boolean transparent = false;

	private WildfireButton(int x, int y, int w, int h, Supplier<Component> text, Button.OnPress onPress, CreateNarration narrationSupplier, @Nullable ButtonRenderer renderer) {
		super(x, y, w, h, text.get(), onPress, narrationSupplier);
		messageSupplier = text;
		this.renderer = renderer;
	}

	public void updateMessage() {
		setMessage(messageSupplier.get());
	}

	protected void drawInner(GuiGraphics ctx, int mouseX, int mouseY, float partialTicks) {
		if(renderer != null) {
			renderer.render(this, ctx, mouseX, mouseY, partialTicks);
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		int textColor = active ? 0xFFFFFF : 0x666666;
		int i = this.getX() + 2;
		int j = this.getX() + this.getWidth() - 2;
		GuiUtils.drawScrollableTextWithoutShadow(GuiUtils.Justify.CENTER, ctx, font, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), textColor);
	}

	@Override
	protected void renderContents(GuiGraphics ctx, int mouseX, int mouseY, float partialTicks) {
		int clr = 0x444444 + (84 << 24);
		if(this.isHoveredOrFocused()) clr = 0x666666 + (84 << 24);
		if(!active) clr = 0x222222 + (84 << 24);
		if(!transparent) ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), clr);

		drawInner(ctx, mouseX, mouseY, partialTicks);
		if(isHovered()) {
			ctx.requestCursor(active ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
		}
	}

	public WildfireButton setTransparent(boolean b) {
		this.transparent = b;
		return this;
	}

	public WildfireButton setActive(boolean b) {
		this.active = b;
		return this;
	}

	@SuppressWarnings({"NotNullFieldNotInitialized", "UnusedReturnValue"})
	public static final class Builder {
		private Supplier<Component> messageSupplier;
		private int x, y, width, height;
		private PressAction onPress;
		private CreateNarration narrationSupplier = DEFAULT_NARRATION;
		private @Nullable Tooltip tooltip = null;
		private @Nullable ButtonRenderer renderer = null;
		private boolean active = true;

		public Builder message(Supplier<Component> messageSupplier) {
			this.messageSupplier = messageSupplier;
			return this;
		}

		public Builder position(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		public Builder size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public Builder onPress(PressAction onPress) {
			this.onPress = onPress;
			return this;
		}

		public Builder narration(CreateNarration narrationSupplier) {
			this.narrationSupplier = narrationSupplier;
			return this;
		}

		public Builder tooltip(@Nullable Tooltip tooltip) {
			this.tooltip = tooltip;
			return this;
		}

		public Builder active(boolean active) {
			this.active = active;
			return this;
		}

		public Builder renderer(@Nullable ButtonRenderer renderer) {
			this.renderer = renderer;
			return this;
		}

		public WildfireButton build() {
			var built = new WildfireButton(x, y, width, height, messageSupplier, onPress, narrationSupplier, renderer);
			built.setActive(active);
			if(tooltip != null) {
				built.setTooltip(tooltip);
			}
			return built;
		}
	}

	@FunctionalInterface
	public interface PressAction extends Button.OnPress {
		default void onPress(Button button) {
			onPress((WildfireButton) button);
		}

		void onPress(WildfireButton button);
	}

	@FunctionalInterface
	public interface ButtonRenderer {
		void render(WildfireButton button, GuiGraphics ctx, int mouseX, int mouseY, float partialTicks);
	}
}
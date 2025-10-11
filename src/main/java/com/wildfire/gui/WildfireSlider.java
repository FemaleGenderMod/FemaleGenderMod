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

import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.types.FloatConfigKey;
import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class WildfireSlider extends ClickableWidget {
	private double value;
	private final double minValue;
	private final double maxValue;
	private final FloatConsumer valueUpdate;
	private final Float2ObjectFunction<Text> messageUpdate;
	private final FloatConsumer onSave;

	private float lastValue;
	private boolean changed;
	private boolean dragging;

	private double mouseStep = 0;
	private double arrowKeyStep = 0.05;

	private WildfireSlider(int xPos, int yPos, int width, int height, double minVal, double maxVal, double currentVal, FloatConsumer valueUpdate,
	                      Float2ObjectFunction<Text> messageUpdate, FloatConsumer onSave) {
		super(xPos, yPos, width, height, Text.empty());
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

	private void setMouseStep(double mouseStep) {
		this.mouseStep = mouseStep;
	}

	protected void updateMessage() {
		setMessage(messageUpdate.get(lastValue));
	}

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
	public void onRelease(Click event) {
		this.dragging = false;
		save();
	}

	@Override
	public void onClick(Click event, boolean doubleClick) {
		this.dragging = true;
		this.setValueFromMouse(event.x());
	}

	@Override
	public boolean keyPressed(KeyInput event) {
		int keyCode = event.key();
		if(keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
			value += (keyCode == GLFW.GLFW_KEY_LEFT ? -arrowKeyStep : arrowKeyStep);
			value = WildfireHelper.snapToStep(MathHelper.clamp(value, 0, 1), arrowKeyStep);
			applyValue();
			updateMessage();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	protected void onDrag(Click event, double d, double e) {
		this.setValueFromMouse(event.x());
	}

	@Override
	public boolean keyReleased(KeyInput event) {
		var keyCode = event.key();
		if(keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
			save();
			return true;
		}
		return super.keyReleased(event);
	}

	protected MutableText getNarrationMessage() {
		return Text.translatable("gui.narrate.slider", this.getMessage());
	}

	@Override
	protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
		if (!this.visible) {
			return;
		}
		int xP = getX() + 2;
		ctx.fill(xP - 2, getY(), getX() + this.width, getY() + this.height, 0x222222 + (128 << 24));
		int xPos = getX() + 2 + (int) (this.value * (float)(this.width - 3));

		ctx.fill(getX() + 1, getY() + 1, xPos - 1, getY() + this.height - 1, active?(0x222266 + (180 << 24)):(0x111133 + (180 << 24)));

		if(active) {
			int xPos2 = this.getX() + 3 + (int) (this.value * (float) (this.width - 4));
			ctx.fill(xPos2 - 2, getY() + 1, xPos2, getY() + this.height - 1, 0xFFFFFF + (120 << 24));
		}
		TextRenderer font = MinecraftClient.getInstance().textRenderer;
		int i = this.getX() + 2;
		int j = this.getX() + this.getWidth() - 2;

		int textColor = (isSelected()&&active) || changed ? 0xFFFF55 : 0xFFFFFF;
		if(!active) {
			textColor = 0x666666;
		}
		GuiUtils.drawScrollableTextWithoutShadow(GuiUtils.Justify.CENTER, ctx, font, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), textColor);

		if(isHovered() || dragging) {
			if(!active) {
				ctx.setCursor(StandardCursors.NOT_ALLOWED);
			} else {
				ctx.setCursor(dragging ? StandardCursors.RESIZE_EW : StandardCursors.POINTING_HAND);
			}
		}
	}

	public float getFloatValue() {
		return (float) getValue();
	}

	public double getValue() {
		return this.value * (maxValue - minValue) + minValue;
	}

	public void setValue(double value) {
		setValueInternal(value);
		applyValue();
	}

	private void setValueInternal(double value) {
		this.value = MathHelper.clamp((value - this.minValue) / (this.maxValue - this.minValue), 0, 1);
		this.lastValue = (float) value;
		updateMessage();
		//Note: Does not call applyValue
	}

	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		this.setValueFromMouse(mouseX);
	}

	@Override
	public void appendClickableNarrations(NarrationMessageBuilder builder) {
		builder.put(NarrationPart.TITLE, Text.translatable("gui.narrate.slider", this.getMessage()));
		if(active) {
			if(isFocused()) {
				builder.put(NarrationPart.USAGE, Text.translatable("narration.slider.usage.focused"));
			} else {
				builder.put(NarrationPart.USAGE, Text.translatable("narration.slider.usage.hovered"));
			}
		}
	}

	private void setValueFromMouse(double mouseX) {
		this.value = ((mouseX - (double)(this.getX() + 4)) / (double)(this.getWidth() - 8));
		this.value = MathHelper.clamp(this.value, 0, 1);

		if (mouseStep > 0) {
			double snapped = Math.round(this.value / mouseStep) * mouseStep;
			this.value = MathHelper.clamp(snapped, 0, 1);
		}

		applyValue();
		updateMessage();
	}

	@SuppressWarnings("UnusedReturnValue")
	public static final class Builder {
		private int x, y, width, height;
		private float min, max;
		private double value;
		private Double step = null;
		private Double mouseStep = null;
		private boolean active = true;
		private Float2ObjectFunction<Text> messageSupplier;
		private FloatConsumer onUpdate, onSave;

		public Builder message(@NotNull Float2ObjectFunction<Text> messageSupplier) {
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

		public Builder update(@NotNull FloatConsumer onUpdate) {
			this.onUpdate = onUpdate;
			return this;
		}

		public Builder save(@NotNull FloatConsumer onSave) {
			this.onSave = onSave;
			return this;
		}

		public Builder range(@NotNull FloatConfigKey key) {
			return range(key.getMinInclusive(), key.getMaxInclusive());
		}

		public Builder range(float min, float max) {
			this.min = min;
			this.max = max;
			return this;
		}

		public Builder current(double value) {
			this.value = value;
			return this;
		}

		public Builder active(boolean active) {
			this.active = active;
			return this;
		}

		public Builder step(double step) {
			this.step = step;
			return this;
		}

		public Builder mouseStep(double step) {
			this.mouseStep = step;
			return this;
		}

		public WildfireSlider build() {
			var built = new WildfireSlider(x, y, width, height, min, max, value, onUpdate, messageSupplier, onSave);
			built.active = active;
			if(step != null) {
				built.setArrowKeyStep(step);
			}
			if(mouseStep != null) {
				built.setMouseStep(mouseStep);
			}
			return built;
		}
	}

}

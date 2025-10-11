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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class WildfireButton extends ButtonWidget {

   private final @Nullable ButtonRenderer renderer;
   private final Supplier<Text> messageSupplier;
   public boolean transparent = false;

   private WildfireButton(int x, int y, int w, int h, Supplier<Text> text, ButtonWidget.PressAction onPress, NarrationSupplier narrationSupplier, @Nullable ButtonRenderer renderer) {
      super(x, y, w, h, text.get(), onPress, narrationSupplier);
      messageSupplier = text;
      this.renderer = renderer;
   }

   public void updateMessage() {
      setMessage(messageSupplier.get());
   }

   protected void drawInner(DrawContext ctx, int mouseX, int mouseY, float partialTicks) {
      if(renderer != null) {
         renderer.render(this, ctx, mouseX, mouseY, partialTicks);
         return;
      }
      MinecraftClient minecraft = MinecraftClient.getInstance();
      TextRenderer font = minecraft.textRenderer;
      int textColor = active ? 0xFFFFFF : 0x666666;
      int i = this.getX() + 2;
      int j = this.getX() + this.getWidth() - 2;
      GuiUtils.drawScrollableTextWithoutShadow(GuiUtils.Justify.CENTER, ctx, font, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), textColor);
   }

   @Override
   protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float partialTicks) {
      int clr = 0x444444 + (84 << 24);
      if(this.isSelected()) clr = 0x666666 + (84 << 24);
      if(!active) clr = 0x222222 + (84 << 24);
      if(!transparent) ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), clr);

      drawInner(ctx, mouseX, mouseY, partialTicks);
      if(isHovered()) {
         ctx.setCursor(active ? StandardCursors.POINTING_HAND : StandardCursors.NOT_ALLOWED);
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

   public static final class Builder {
      private Supplier<Text> messageSupplier;
      private int x, y, width, height;
      private PressAction onPress;
      private NarrationSupplier narrationSupplier = DEFAULT_NARRATION_SUPPLIER;
      private Tooltip tooltip = null;
      private ButtonRenderer renderer = null;
      private boolean active = true;

      public Builder message(@NotNull Supplier<Text> messageSupplier) {
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

      public Builder onPress(@NotNull PressAction onPress) {
         this.onPress = onPress;
         return this;
      }

      public Builder narration(@NotNull NarrationSupplier narrationSupplier) {
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
   public interface PressAction extends ButtonWidget.PressAction {
      default void onPress(ButtonWidget button) {
         onPress((WildfireButton) button);
      }

      void onPress(WildfireButton button);
   }

   @FunctionalInterface
   public interface ButtonRenderer {
      void render(WildfireButton button, DrawContext ctx, int mouseX, int mouseY, float partialTicks);
   }
}
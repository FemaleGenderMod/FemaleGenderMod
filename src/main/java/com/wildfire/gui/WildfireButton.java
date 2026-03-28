package com.wildfire.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WildfireButton extends Button {
    public boolean transparent;

    public WildfireButton(int x, int y, int w, int h, Component text, Button.OnPress onPress, Button.CreateNarration narrationSupplier) {
        super(x, y, w, h, text, onPress, narrationSupplier);
        this.transparent = false;
    }

    public WildfireButton(int x, int y, int w, int h, Component text, Button.OnPress onPress) {
        this(x, y, w, h, text, onPress, DEFAULT_NARRATION);
    }

    public WildfireButton(int x, int y, int w, int h, Component text, Button.OnPress onPress, Tooltip tooltip) {
        this(x, y, w, h, text, onPress, DEFAULT_NARRATION);
        this.setTooltip(tooltip);
    }

    @Override
    protected void renderWidget(GuiGraphics ctx, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        // Definición de colores basada en el estado del botón
        int clr = 1413760068; // Color base
        if (this.isHoveredOrFocused()) {
            clr = 1415997030; // Color cuando pasas el mouse
        }

        if (!this.active) {
            clr = 1411523106; // Color cuando está desactivado
        }

        // Dibujar el fondo del botón si no es transparente
        if (!this.transparent) {
            ctx.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, clr);
        }

        // Color del texto
        int textColor = this.active ? 16777215 : 6710886;

        // Dibujar el texto con scroll usando nuestra utilidad de GuiUtils
        int textLeft = this.getX() + 2;
        int textRight = this.getX() + this.width - 2;

        GuiUtils.drawScrollableTextWithoutShadow(
                ctx,
                font,
                this.getMessage(),
                textLeft,
                this.getY(),
                textRight,
                this.getY() + this.height,
                textColor
        );

        // Resetear el color del shader por si acaso
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public WildfireButton setTransparent(boolean b) {
        this.transparent = b;
        return this;
    }

    public WildfireButton setActive(boolean b) {
        this.active = b;
        return this;
    }
}
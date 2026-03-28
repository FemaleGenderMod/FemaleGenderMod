package com.wildfire.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wildfire.main.config.FloatConfigKey;
import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WildfireSlider extends AbstractWidget {
    private double value; // Valor normalizado 0.0 - 1.0
    private final double minValue;
    private final double maxValue;
    private final FloatConsumer valueUpdate;
    private final Float2ObjectFunction<Component> messageUpdate;
    private final FloatConsumer onSave;
    private float lastValue;
    private boolean changed;

    public WildfireSlider(int xPos, int yPos, int width, int height, FloatConfigKey config, double currentVal, FloatConsumer valueUpdate, Float2ObjectFunction<Component> messageUpdate, FloatConsumer onSave) {
        this(xPos, yPos, width, height, (double)config.getMinInclusive(), (double)config.getMaxInclusive(), currentVal, valueUpdate, messageUpdate, onSave);
    }

    public WildfireSlider(int xPos, int yPos, int width, int height, double minVal, double maxVal, double currentVal, FloatConsumer valueUpdate, Float2ObjectFunction<Component> messageUpdate, FloatConsumer onSave) {
        super(xPos, yPos, width, height, Component.empty());
        this.minValue = minVal;
        this.maxValue = maxVal;
        this.valueUpdate = valueUpdate;
        this.messageUpdate = messageUpdate;
        this.onSave = onSave;
        this.setValueInternal(currentVal);
    }

    protected void updateMessage() {
        this.setMessage(this.messageUpdate.get(this.lastValue));
    }

    protected void applyValue() {
        float newValue = this.getFloatValue();
        if (this.lastValue != newValue) {
            this.valueUpdate.accept(newValue);
            this.lastValue = newValue;
            this.changed = true;
        }
    }

    public void save() {
        if (this.changed) {
            this.onSave.accept(this.lastValue);
            this.changed = false;
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.save();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setValueFromMouse(mouseX);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        // 263 = Izquierda, 262 = Derecha
        if (keyCode == 263 || keyCode == 262) {
            this.save();
        }
        return result;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return Component.translatable("gui.narrate.slider", this.getMessage());
    }

    @Override
    protected void renderWidget(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            RenderSystem.disableDepthTest();

            // Dibujar fondo del slider
            int xP = this.getX() + 2;
            ctx.fill(xP - 2, this.getY(), this.getX() + this.width - 1, this.getY() + this.height, -2145246686);

            // Dibujar progreso (barra de color)
            int xPos = this.getX() + 2 + (int)(this.value * (double)((float)(this.width - 3)));
            ctx.fill(this.getX() + 1, this.getY() + 1, xPos - 1, this.getY() + this.height - 1, -1272831386);

            // Dibujar el "indicador" o manija
            int xPos2 = this.getX() + 3 + (int)(this.value * (double)((float)(this.width - 5)));
            ctx.fill(xPos2 - 2, this.getY() + 1, xPos2, this.getY() + this.height - 1, 2030043135);

            RenderSystem.enableDepthTest();

            // Dibujar el texto encima
            Font font = Minecraft.getInstance().font;
            int i = this.getX() + 2;
            int j = this.getX() + this.width - 2;

            // Si el mouse está encima o ha cambiado, usamos un color amarillento (16777045), si no, blanco.
            int textColor = (!this.isHovered && !this.changed) ? 16777215 : 16777045;

            GuiUtils.drawScrollableTextWithoutShadow(ctx, font, this.getMessage(), i, this.getY(), j, this.getY() + this.height, textColor);
        }
    }

    public float getFloatValue() {
        return (float)this.getValue();
    }

    public double getValue() {
        return this.value * (this.maxValue - this.minValue) + this.minValue;
    }

    public void setValue(double value) {
        this.setValueInternal(value);
        this.applyValue();
    }

    private void setValueInternal(double value) {
        this.value = Mth.clamp((value - this.minValue) / (this.maxValue - this.minValue), 0.0, 1.0);
        this.lastValue = (float)value;
        this.updateMessage();
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.setValueFromMouse(mouseX);
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        // En Forge 1.21.1 este método reemplaza a method_47399
    }

    private void setValueFromMouse(double mouseX) {
        this.value = (mouseX - (double)(this.getX() + 4)) / (double)(this.width - 8);
        this.value = Mth.clamp(this.value, 0.0, 1.0);
        this.applyValue();
        this.updateMessage();
    }
}
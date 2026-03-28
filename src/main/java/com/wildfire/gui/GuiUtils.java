package com.wildfire.gui;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public final class GuiUtils {

    private GuiUtils() {
        throw new UnsupportedOperationException();
    }

    public static void drawCenteredText(GuiGraphics ctx, Font font, Component text, int x, int y, int color) {
        // En Mojang: width() reemplaza a method_27525()
        int centeredX = x - font.width(text) / 2;
        ctx.drawString(font, text, centeredX, y, color, false);
    }

    public static void drawScrollableTextWithoutShadow(GuiGraphics ctx, Font font, Component text, int left, int top, int right, int bottom, int color) {
        int textWidth = font.width(text);
        int j = (top + bottom - 9) / 2 + 1;
        int containerWidth = right - left;

        if (textWidth > containerWidth) {
            int overflow = textWidth - containerWidth;
            // Util.getMillis() reemplaza al tiempo de Fabric
            double time = (double) Util.getMillis() / 1000.0;
            double speed = Math.max((double) overflow * 0.5, 3.0);
            double sin = Math.sin((Math.PI / 2.0) * Math.cos((Math.PI * 2.0) * time / speed)) / 2.0 + 0.5;
            // Mth.lerp para la transición suave del scroll
            double scrollOffset = Mth.lerp(sin, 0.0, (double) overflow);

            ctx.enableScissor(left, top, right, bottom);
            ctx.drawString(font, text, left - (int) scrollOffset, j, color, false);
            ctx.disableScissor();
        } else {
            drawCenteredText(ctx, font, text, (left + right) / 2, j, color);
        }
    }

    public static void drawEntityOnScreen(GuiGraphics ctx, int x, int y, int size, float mouseX, float mouseY, LivingEntity entity) {
        float f = (float) Math.atan((double) (mouseX / 40.0F));
        float g = (float) Math.atan((double) (mouseY / 40.0F));

        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float) Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(g * 20.0F * ((float) Math.PI / 180.0F));
        quaternionf.mul(quaternionf1);

        // Guardamos rotaciones originales
        float bodyRot = entity.yBodyRot;
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        float yHeadRotO = entity.yHeadRotO;
        float yHeadRot = entity.yHeadRot;

        // Aplicamos rotación hacia el mouse
        entity.yBodyRot = 180.0F + f * 20.0F;
        entity.setYRot(180.0F + f * 40.0F);
        entity.setXRot(-g * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        // Escalado dinámico basado en el tamaño de la entidad
        float renderScale = (float) size / entity.getScale();

        // InventoryScreen.renderEntityInInventoryFollowsMouse es el método oficial para esto// Cambiamos el nombre del método a 'renderEntityInInventory'
        InventoryScreen.renderEntityInInventory(
                ctx,
                (float) x,
                (float) y,
                renderScale,
                new Vector3f(),
                quaternionf,
                quaternionf1, // Asegúrate de que coincida con el nombre de tu variable
                entity
        );

        // Restauramos rotaciones originales para no afectar al jugador en el mundo
        entity.yBodyRot = bodyRot;
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.yHeadRotO = yHeadRotO;
        entity.yHeadRot = yHeadRot;
    }
}
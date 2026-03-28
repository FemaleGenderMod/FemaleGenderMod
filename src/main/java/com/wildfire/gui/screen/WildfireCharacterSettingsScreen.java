package com.wildfire.gui.screen;

import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.WildfireButton;
import com.wildfire.gui.WildfireSlider;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.entitydata.PlayerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Objects;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class WildfireCharacterSettingsScreen extends BaseWildfireScreen {
    private static final Component ENABLED = Component.translatable("wildfire_gender.label.enabled").withStyle(ChatFormatting.GREEN);
    private static final Component DISABLED = Component.translatable("wildfire_gender.label.disabled").withStyle(ChatFormatting.RED);
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("wildfire_gender", "textures/gui/settings_bg.png");

    private WildfireSlider bounceSlider;
    private WildfireSlider floppySlider;
    private boolean bounceWarning;

    public WildfireCharacterSettingsScreen(Screen parent, UUID uuid) {
        super(Component.translatable("wildfire_gender.char_settings.title"), parent, uuid);
    }

    @Override
    protected void init() {
        super.init();
        PlayerConfig aPlr = Objects.requireNonNull(this.getPlayer(), "getPlayer()");
        int x = this.width / 2;
        int y = this.height / 2;
        int yPos = y - 47;
        int xPos = x - 78 - 1;

        // Botón Cerrar X
        this.addRenderableWidget(new WildfireButton(this.width / 2 + 73, yPos - 11, 9, 9, Component.literal("X"), (button) -> {
            Minecraft.getInstance().setScreen(this.parent);
        }));

        // Botón Físicas de Pecho
        this.addRenderableWidget(new WildfireButton(xPos, yPos, 157, 20, Component.translatable("wildfire_gender.char_settings.physics", aPlr.hasBreastPhysics() ? ENABLED : DISABLED), (button) -> {
            boolean enablePhysics = !aPlr.hasBreastPhysics();
            if (aPlr.updateBreastPhysics(enablePhysics)) {
                button.setMessage(Component.translatable("wildfire_gender.char_settings.physics", enablePhysics ? ENABLED : DISABLED));
                PlayerConfig.saveGenderInfo(aPlr);
            }
        }));

        // Botón Ocultar en Armadura
        this.addRenderableWidget(new WildfireButton(xPos, yPos + 20, 157, 20, Component.translatable("wildfire_gender.char_settings.hide_in_armor", aPlr.showBreastsInArmor() ? DISABLED : ENABLED), (button) -> {
            boolean enableShowInArmor = !aPlr.showBreastsInArmor();
            if (aPlr.updateShowBreastsInArmor(enableShowInArmor)) {
                button.setMessage(Component.translatable("wildfire_gender.char_settings.hide_in_armor", enableShowInArmor ? DISABLED : ENABLED));
                PlayerConfig.saveGenderInfo(aPlr);
            }
        }));

        // Botón Override de Físicas en Armadura
        WildfireButton btnOverride = this.addRenderableWidget(new WildfireButton(xPos, yPos + 40, 157, 20, Component.translatable("wildfire_gender.char_settings.override_armor_physics", aPlr.getArmorPhysicsOverride() ? ENABLED : DISABLED), (button) -> {
            boolean enableArmorPhysicsOverride = !aPlr.getArmorPhysicsOverride();
            if (aPlr.updateArmorPhysicsOverride(enableArmorPhysicsOverride)) {
                button.setMessage(Component.translatable("wildfire_gender.char_settings.override_armor_physics", enableArmorPhysicsOverride ? ENABLED : DISABLED));
                PlayerConfig.saveGenderInfo(aPlr);
            }
        }));
        btnOverride.setTooltip(Tooltip.create(Component.translatable("wildfire_gender.tooltip.override_armor_physics.line1")
                .append("\n\n")
                .append(Component.translatable("wildfire_gender.tooltip.override_armor_physics.line2"))));

        // Slider de Rebote (Bounce)
        this.bounceSlider = this.addRenderableWidget(new WildfireSlider(xPos, yPos + 60, 158, 20, Configuration.BOUNCE_MULTIPLIER, (double)aPlr.getBounceMultiplier(), (value) -> {}, (value) -> {
            float bounceText = 3.0F * value;
            int v = Math.round(bounceText * 100.0F);
            this.bounceWarning = v > 100;
            return Component.translatable("wildfire_gender.slider.bounce", v);
        }, (value) -> {
            if (aPlr.updateBounceMultiplier(value)) {
                PlayerConfig.saveGenderInfo(aPlr);
            }
        }));

        // Slider de Floppiness
        this.floppySlider = this.addRenderableWidget(new WildfireSlider(xPos, yPos + 80, 158, 20, Configuration.FLOPPY_MULTIPLIER, (double)aPlr.getFloppiness(), (value) -> {}, (value) -> {
            return Component.translatable("wildfire_gender.slider.floppy", Math.round(value * 100.0F));
        }, (value) -> {
            if (aPlr.updateFloppiness(value)) {
                PlayerConfig.saveGenderInfo(aPlr);
            }
        }));

        // Botón Sonidos de Daño
        WildfireButton btnHurt = this.addRenderableWidget(new WildfireButton(xPos, yPos + 100, 157, 20, Component.translatable("wildfire_gender.char_settings.hurt_sounds", aPlr.hasHurtSounds() ? ENABLED : DISABLED), (button) -> {
            boolean enableHurtSounds = !aPlr.hasHurtSounds();
            if (aPlr.updateHurtSounds(enableHurtSounds)) {
                button.setMessage(Component.translatable("wildfire_gender.char_settings.hurt_sounds", enableHurtSounds ? ENABLED : DISABLED));
                PlayerConfig.saveGenderInfo(aPlr);
            }
        }));
        btnHurt.setTooltip(Tooltip.create(Component.translatable("wildfire_gender.tooltip.hurt_sounds")));
    }

    @Override
    public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        // Dibujamos el fondo del menú
        ctx.blit(BACKGROUND, (this.width - 172) / 2, (this.height - 124) / 2, 0, 0, 172, 144);
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);

        if (this.minecraft != null && this.minecraft.level != null) {
            Player plrEntity = this.minecraft.level.getPlayerByUUID(this.playerUUID);
            int x = this.width / 2;
            int y = this.height / 2;
            int yPos = y - 47;

            // Título del menú
            ctx.drawString(this.font, this.title, x - 79, yPos - 10, 4473924, false);

            if (plrEntity != null) {
                // Nombre del jugador sobre el menú
                GuiUtils.drawCenteredText(ctx, this.font, plrEntity.getDisplayName(), x, yPos - 30, 16777215);
            }

            // Advertencia si el rebote es muy alto
            if (this.bounceWarning) {
                GuiUtils.drawCenteredText(ctx, this.font, Component.translatable("wildfire_gender.tooltip.bounce_warning").withStyle(ChatFormatting.ITALIC), x, y + 90, 16737894);
            }
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        // Asegurar que los sliders guarden su estado al soltar el ratón
        if (this.bounceSlider != null) this.bounceSlider.save();
        if (this.floppySlider != null) this.floppySlider.save();
        return super.mouseReleased(mouseX, mouseY, state);
    }
}
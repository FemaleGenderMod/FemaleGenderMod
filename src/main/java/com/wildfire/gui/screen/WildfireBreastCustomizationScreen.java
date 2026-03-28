package com.wildfire.gui.screen;

import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.WildfireBreastPresetList;
import com.wildfire.gui.WildfireButton;
import com.wildfire.gui.WildfireSlider;
import com.wildfire.main.config.BreastPresetConfiguration;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.FloatConfigKey;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.PlayerConfig;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Objects;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class WildfireBreastCustomizationScreen extends BaseWildfireScreen {
    private WildfireSlider breastSlider;
    private WildfireSlider xOffsetBoobSlider;
    private WildfireSlider yOffsetBoobSlider;
    private WildfireSlider zOffsetBoobSlider;
    private WildfireSlider cleavageSlider;
    private WildfireButton btnDualPhysics;
    private WildfireButton btnPresets;
    private WildfireButton btnCustomization;
    private WildfireButton btnAddPreset;
    private WildfireButton btnDeletePreset;
    private WildfireBreastPresetList PRESET_LIST;
    private int currentTab = 0;

    public WildfireBreastCustomizationScreen(Screen parent, UUID uuid) {
        super(Component.translatable("wildfire_gender.appearance_settings.title"), parent, uuid);
    }

    @Override
    protected void init() {
        super.init();
        int j = this.height / 2 - 11;
        PlayerConfig plr = Objects.requireNonNull(this.getPlayer(), "getPlayer()");
        Breasts breasts = plr.getBreasts();
        FloatConsumer onSave = (value) -> PlayerConfig.saveGenderInfo(plr);

        // Botón cerrar X
        this.addRenderableWidget(new WildfireButton(this.width / 2 + 178, j - 72, 9, 9, Component.literal("X"), (button) -> {
            Minecraft.getInstance().setScreen(this.parent);
        }));

        // Tab de Personalización
        this.btnCustomization = this.addRenderableWidget(new WildfireButton(this.width / 2 + 30, j - 60, 78, 10, Component.translatable("wildfire_gender.breast_customization.tab_customization"), (button) -> {
            this.currentTab = 0;
            this.btnCustomization.active = false;
            this.btnPresets.active = true;
            this.btnAddPreset.visible = false;
            this.btnDeletePreset.visible = false;
            this.updateWidgetVisibility();
        }));
        this.btnCustomization.active = false;

        // Tab de Presets
        this.btnPresets = this.addRenderableWidget(new WildfireButton(this.width / 2 + 31 + 79, j - 60, 78, 10, Component.translatable("wildfire_gender.breast_customization.tab_presets"), (button) -> {
            if (!FMLEnvironment.production) { // Equivalente a isDevelopmentEnvironment
                this.currentTab = 1;
                this.btnCustomization.active = true;
                this.btnPresets.active = false;
                this.btnAddPreset.visible = true;
                this.btnDeletePreset.visible = true;
                this.PRESET_LIST.refreshList();
                this.updateWidgetVisibility();
            }
        }));

        if (FMLEnvironment.production) {
            this.btnPresets.setTooltip(Tooltip.create(Component.translatable("wildfire_gender.coming_soon")));
        }

        // Botones de Presets (Añadir/Borrar)
        this.btnAddPreset = this.addRenderableWidget(new WildfireButton(this.width / 2 + 31 + 79, j + 80, 78, 12, Component.translatable("wildfire_gender.breast_customization.presets.add_new"), (button) -> this.createNewPreset("Test Preset")));
        this.btnAddPreset.visible = false;

        this.btnDeletePreset = this.addRenderableWidget(new WildfireButton(this.width / 2 + 30, j + 80, 78, 12, Component.translatable("wildfire_gender.breast_customization.presets.delete"), (button) -> {
            // Lógica de borrar pendiente en el original
        }));
        this.btnDeletePreset.active = false;
        this.btnDeletePreset.visible = false;

        // Sliders de configuración
        int xPos = this.width / 2 + 30;

        this.breastSlider = this.addRenderableWidget(new WildfireSlider(xPos, j - 48, 158, 20, Configuration.BUST_SIZE, (double)plr.getBustSize(), plr::updateBustSize, (value) -> Component.translatable("wildfire_gender.wardrobe.slider.breast_size", Math.round(value * 1.25F * 100.0F)), onSave));

        this.xOffsetBoobSlider = this.addRenderableWidget(new WildfireSlider(xPos, j - 27, 158, 20, Configuration.BREASTS_OFFSET_X, (double)breasts.getXOffset(), breasts::updateXOffset, (value) -> Component.translatable("wildfire_gender.wardrobe.slider.separation", Math.round((float)Math.round(value * 100.0F) / 100.0F * 10.0F)), onSave));

        this.yOffsetBoobSlider = this.addRenderableWidget(new WildfireSlider(xPos, j - 6, 158, 20, Configuration.BREASTS_OFFSET_Y, (double)breasts.getYOffset(), breasts::updateYOffset, (value) -> Component.translatable("wildfire_gender.wardrobe.slider.height", Math.round((float)Math.round(value * 100.0F) / 100.0F * 10.0F)), onSave));

        this.zOffsetBoobSlider = this.addRenderableWidget(new WildfireSlider(xPos, j + 15, 158, 20, Configuration.BREASTS_OFFSET_Z, (double)breasts.getZOffset(), breasts::updateZOffset, (value) -> Component.translatable("wildfire_gender.wardrobe.slider.depth", Math.round((float)Math.round(value * 100.0F) / 100.0F * 10.0F)), onSave));

        this.cleavageSlider = this.addRenderableWidget(new WildfireSlider(xPos, j + 36, 158, 20, Configuration.BREASTS_CLEAVAGE, (double)breasts.getCleavage(), breasts::updateCleavage, (value) -> Component.translatable("wildfire_gender.wardrobe.slider.rotation", Math.round((float)Math.round(value * 100.0F) / 100.0F * 100.0F)), onSave));

        // Botón Física Dual (Uniboob)
        this.btnDualPhysics = this.addRenderableWidget(new WildfireButton(xPos, j + 57, 158, 20, Component.translatable("wildfire_gender.breast_customization.dual_physics", Component.translatable(breasts.isUniboob() ? "wildfire_gender.label.no" : "wildfire_gender.label.yes")), (button) -> {
            boolean isUniboob = !breasts.isUniboob();
            if (breasts.updateUniboob(isUniboob)) {
                button.setMessage(Component.translatable("wildfire_gender.breast_customization.dual_physics", Component.translatable(isUniboob ? "wildfire_gender.label.no" : "wildfire_gender.label.yes")));
                PlayerConfig.saveGenderInfo(plr);
            }
        }));

        // Lista de Presets
        this.PRESET_LIST = new WildfireBreastPresetList(this, 156, j - 48);
        this.PRESET_LIST.setX(this.width / 2 + 30); // method_46421
        this.PRESET_LIST.setY(125); // method_53533 (Asumiendo que es la altura o posición)
        this.addWidget(this.PRESET_LIST); // method_25429

        this.currentTab = 0;
        this.updateWidgetVisibility();
    }

    private void createNewPreset(String presetName) {
        BreastPresetConfiguration cfg = new BreastPresetConfiguration(presetName);
        PlayerConfig plr = Objects.requireNonNull(this.getPlayer(), "getPlayer()");
        cfg.set(BreastPresetConfiguration.PRESET_NAME, presetName);
        cfg.set(BreastPresetConfiguration.BUST_SIZE, plr.getBustSize());
        cfg.set(BreastPresetConfiguration.BREASTS_UNIBOOB, plr.getBreasts().isUniboob());
        cfg.set(BreastPresetConfiguration.BREASTS_CLEAVAGE, plr.getBreasts().getCleavage());
        cfg.set(BreastPresetConfiguration.BREASTS_OFFSET_X, plr.getBreasts().getXOffset());
        cfg.set(BreastPresetConfiguration.BREASTS_OFFSET_Y, plr.getBreasts().getYOffset());
        cfg.set(BreastPresetConfiguration.BREASTS_OFFSET_Z, plr.getBreasts().getZOffset());
        cfg.save();
        this.PRESET_LIST.refreshList();
    }

    private void updateWidgetVisibility() {
        PlayerConfig plr = this.getPlayer();
        if (plr != null) {
            boolean canHaveBreasts = plr.getGender().canHaveBreasts();
            boolean isTab0 = this.currentTab == 0;

            this.breastSlider.visible = canHaveBreasts && isTab0;
            this.xOffsetBoobSlider.visible = canHaveBreasts && isTab0;
            this.yOffsetBoobSlider.visible = canHaveBreasts && isTab0;
            this.zOffsetBoobSlider.visible = canHaveBreasts && isTab0;
            this.cleavageSlider.visible = canHaveBreasts && isTab0;
            this.btnDualPhysics.visible = canHaveBreasts && isTab0;
            this.PRESET_LIST.visible = this.currentTab == 1;
        }
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);

        int x = this.width / 2;
        int y = this.height / 2;

        // Dibujar el fondo oscuro del panel derecho
        ctx.fill(x + 28, y - 64 - 21, x + 190, y + 68, 1426063360);
        ctx.fill(x + 29, y - 63 - 21, x + 189, y - 60, 1426063360);

        // Título de la pantalla
        ctx.drawString(this.font, this.title, x + 32, y - 60 - 21, 16777215, false);

        if (this.currentTab == 1) {
            // Fondo de la lista de presets
            ctx.fill(this.PRESET_LIST.getRowLeft(), this.PRESET_LIST.getY(), this.PRESET_LIST.getRowRight(), this.PRESET_LIST.getBottom(), 1426063360);
            this.PRESET_LIST.render(ctx, mouseX, mouseY, delta);

            if (this.PRESET_LIST.getPresetList().length == 0) {
                ctx.drawCenteredString(this.font, "No Presets Found", x + 109, y - 4, 16777215);
            }
        }

        // Vista previa del jugador
        if (this.minecraft != null && this.minecraft.level != null) {
            Player ent = this.minecraft.level.getPlayerByUUID(this.playerUUID);
            if (ent != null) {
                int xP = this.width / 2 - 102;
                int yP = this.height / 2 + 275;

                // Efecto de Scissor para recortar la vista previa
                ctx.enableScissor(this.width / 2 - 235, this.height / 2 - 150, this.width / 2 + 25, yP + 35);
                GuiUtils.drawEntityOnScreen(ctx, xP, yP, 200, -20.0F, -20.0F, ent);
                ctx.disableScissor();
            }
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        // Guardar valores al soltar los deslizadores
        this.breastSlider.save();
        this.xOffsetBoobSlider.save();
        this.yOffsetBoobSlider.save();
        this.zOffsetBoobSlider.save();
        this.cleavageSlider.save();
        return super.mouseReleased(mouseX, mouseY, state);
    }
}
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

package com.wildfire.client.gui.screen;

import com.wildfire.client.gui.WildfireButton;
import com.wildfire.client.gui.WildfireSlider;
import com.wildfire.main.Gender;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.BreastPresetConfiguration;
import com.wildfire.main.config.ClientConfiguration;
import com.wildfire.main.config.GeneralClientConfig;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.PlayerConfig;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class WildfireBreastCustomizationScreen extends BaseWildfireScreen {

    private static final float ANGLE = (float) Math.atan(-0.5);
    private static final float PREVIEW_Y_BODY_ROT = 180.0F + ANGLE * 20.0F;
    private static final float PREVIEW_Y_ROT = 180.0F + ANGLE * 40.0F;
    private static final float PREVIEW_X_ROT = -ANGLE * 20.0F;
    private static final Quaternionf CAMERA_ORIENTATION = (new Quaternionf()).rotateX(ANGLE * 20.0F * ((float) Math.PI / 180F));
    private static final Quaternionf PREVIEW_ANGLE = Util.make(new Quaternionf().rotateZ(Mth.PI), preview -> preview.mul(CAMERA_ORIENTATION));

    private static final Component ENABLED = Component.translatable("wildfire_gender.label.enabled").withStyle(ChatFormatting.GREEN);
    private static final Component DISABLED = Component.translatable("wildfire_gender.label.disabled").withStyle(ChatFormatting.RED);

    private static final ResourceLocation BACKGROUND_FEMALE = WildfireGender.rl("textures/gui/breast_customization.png");
    private static final ResourceLocation BACKGROUND_OTHER = WildfireGender.rl("textures/gui/breast_customization_other.png");

    //Customization Tab
    private WildfireSlider breastSlider, xOffsetBoobSlider, yOffsetBoobSlider, zOffsetBoobSlider, cleavageSlider;
    private WildfireButton btnDualPhysics, btnPhysics, btnCustomization, btnMiscellaneous;

    //Breast Physics Tab
    private WildfireSlider bounceSlider, floppySlider;
    private WildfireButton btnOverrideArmorPhys, btnBreastPhysics;

    //Miscellaneous Tab
    private WildfireSlider voicePitchSlider;
    private WildfireButton btnHurtSounds, btnHideInArmor, btnShowTooltips;
    private WildfireButton btnHolidayThemes;

    //Presets Code
    //private WildfireButton btnAddPreset, btnDeletePreset;
    //private WildfireBreastPresetList PRESET_LIST;

    private Tab currentTab = Tab.CUSTOMIZATION;

    public WildfireBreastCustomizationScreen(Screen parent, UUID uuid) {
        super(Component.translatable("wildfire_gender.appearance_settings.title"), parent, uuid);
    }

    @Override
    public void init() {
        PlayerConfig plr = getPlayer();
        FloatConsumer onSave = value -> {
            //Just save as we updated the actual value in value change
            PlayerConfig.saveGenderInfo(plr);
        };

        int j = this.height / 2 - 11;
        int tabOffsetY = j - 3 - 21;

        initCustomizationTab(plr, onSave, j, tabOffsetY);
        initPhysicsTab(plr, onSave, j, tabOffsetY);
        initMiscTab(plr, onSave, j, tabOffsetY);
        initPresetTab(plr, onSave, j, tabOffsetY);

        //Set default visibilities
        updateTab(Tab.CUSTOMIZATION);

        super.init();
    }

    private void initCustomizationTab(PlayerConfig plr, FloatConsumer onSave, int j, int tabOffsetY) {
        Breasts breasts = plr.getBreasts();

        this.btnCustomization = addRenderableWidget(new WildfireButton(this.width / 2 - 130, j - 52, 172 / 2 - 2, 12,
              Component.translatable("wildfire_gender.breast_customization.tab_customization"), button -> updateTab(Tab.CUSTOMIZATION))
        );
        this.btnCustomization.active = false;

        this.breastSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36, tabOffsetY - 2, 166, 20, ClientConfiguration.BUST_SIZE, plr.getBustSize(),
              plr::updateBustSize, value -> Component.translatable("wildfire_gender.wardrobe.slider.breast_size", Math.round(value * 1.25f * 100)), onSave));
        this.breastSlider.setArrowKeyStep(0.01);

        this.xOffsetBoobSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36, tabOffsetY + 22, 166 / 2 - 2, 20, ClientConfiguration.BREASTS_OFFSET_X, breasts.getXOffset(),
              breasts::updateXOffset, value -> Component.translatable("wildfire_gender.wardrobe.slider.separation", Math.round((Math.round(value * 100f) / 100f) * 10)), onSave));
        this.yOffsetBoobSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36 + 166 / 2 + 2, tabOffsetY + 22, 166 / 2 - 2, 20, ClientConfiguration.BREASTS_OFFSET_Y, breasts.getYOffset(),
              breasts::updateYOffset, value -> Component.translatable("wildfire_gender.wardrobe.slider.height", Math.round((Math.round(value * 100f) / 100f) * 10)), onSave));

        this.zOffsetBoobSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36, tabOffsetY + 46, 166 / 2 - 2, 20, ClientConfiguration.BREASTS_OFFSET_Z, breasts.getZOffset(),
              breasts::updateZOffset, value -> Component.translatable("wildfire_gender.wardrobe.slider.depth", Math.round((Math.round(value * 100f) / 100f) * 10)), onSave));
        this.zOffsetBoobSlider.setArrowKeyStep(0.1);
        this.cleavageSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36 + 166 / 2 + 2, tabOffsetY + 46, 166 / 2 - 2, 20, ClientConfiguration.BREASTS_CLEAVAGE, breasts.getCleavage(),
              breasts::updateCleavage, value -> Component.translatable("wildfire_gender.wardrobe.slider.rotation", Math.round((Math.round(value * 100f) / 100f) * 100)), onSave));
        this.cleavageSlider.setArrowKeyStep(0.1);
    }

    private void initPhysicsTab(PlayerConfig plr, FloatConsumer onSave, int j, int tabOffsetY) {
        Breasts breasts = plr.getBreasts();

        this.btnPhysics = addRenderableWidget(new WildfireButton(this.width / 2 - 42, j - 52, 172 / 2 - 2, 12,
              Component.translatable("wildfire_gender.breast_customization.tab_physics"), button -> updateTab(Tab.PHYSICS))
        );

        this.btnBreastPhysics = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY - 2, 166, 20,
              Component.translatable("wildfire_gender.char_settings.physics", plr.hasBreastPhysics() ? ENABLED : DISABLED), button -> {
            boolean enablePhysics = !plr.hasBreastPhysics();
            if (plr.updateBreastPhysics(enablePhysics)) {

                this.bounceSlider.active = plr.hasBreastPhysics();
                this.floppySlider.active = plr.hasBreastPhysics();
                this.btnOverrideArmorPhys.active = plr.hasBreastPhysics();
                this.btnDualPhysics.active = plr.hasBreastPhysics();

                button.setMessage(Component.translatable("wildfire_gender.char_settings.physics", enablePhysics ? ENABLED : DISABLED));
                PlayerConfig.saveGenderInfo(plr);
            }
        }));

        this.btnDualPhysics = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY + 22, 166, 20,
              Component.translatable("wildfire_gender.breast_customization.dual_physics", breasts.isUniboob() ? CommonComponents.GUI_NO : CommonComponents.GUI_YES), button -> {
            boolean isUniboob = !breasts.isUniboob();
            if (breasts.updateUniboob(isUniboob)) {
                button.setMessage(Component.translatable("wildfire_gender.breast_customization.dual_physics",
                      isUniboob ? CommonComponents.GUI_NO : CommonComponents.GUI_YES));
                PlayerConfig.saveGenderInfo(plr);
            }
        }));
        this.btnDualPhysics.active = plr.hasBreastPhysics();

        this.btnOverrideArmorPhys = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY + 70, 166, 20,
              Component.translatable("wildfire_gender.char_settings.override_armor_physics", plr.getArmorPhysicsOverride() ? ENABLED : DISABLED), button -> {
            boolean enableArmorPhysicsOverride = !plr.getArmorPhysicsOverride();
            if (plr.updateArmorPhysicsOverride(enableArmorPhysicsOverride)) {
                button.setMessage(Component.translatable("wildfire_gender.char_settings.override_armor_physics", plr.getArmorPhysicsOverride() ? ENABLED : DISABLED));
                PlayerConfig.saveGenderInfo(plr);
            }
        }, Tooltip.create(Component.translatable("wildfire_gender.tooltip.override_armor_physics.line1")
              .append("\n\n")
              .append(Component.translatable("wildfire_gender.tooltip.override_armor_physics.line2")))
        ));
        this.btnOverrideArmorPhys.active = plr.hasBreastPhysics();

        this.bounceSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36, tabOffsetY + 46, 166 / 2 - 2, 20, ClientConfiguration.BOUNCE_MULTIPLIER, plr.getBounceMultiplier(), value -> {
        }, value -> {
            float bounceText = 3 * value;
            int v = Math.round(bounceText * 100);
            //bounceWarning = v > 100;
            return Component.translatable("wildfire_gender.slider.bounce", v);
        }, value -> {
            if (plr.updateBounceMultiplier(value)) {
                PlayerConfig.saveGenderInfo(plr);
            }
        }));
        this.bounceSlider.active = plr.hasBreastPhysics();
        this.bounceSlider.setArrowKeyStep(0.005);

        this.floppySlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36 + 166 / 2 + 2, tabOffsetY + 46, 166 / 2 - 2, 20, ClientConfiguration.FLOPPY_MULTIPLIER, plr.getFloppiness(), value -> {
        }, value -> Component.translatable("wildfire_gender.slider.floppy", Math.round(value * 100)), value -> {
            if (plr.updateFloppiness(value)) {
                PlayerConfig.saveGenderInfo(plr);
            }
        }));
        this.floppySlider.active = plr.hasBreastPhysics();
        this.floppySlider.setArrowKeyStep(0.01);
    }

    private void initMiscTab(PlayerConfig plr, FloatConsumer onSave, int j, int tabOffsetY) {
        this.btnMiscellaneous = addRenderableWidget(new WildfireButton(this.width / 2 + 46, j - 52, 172 / 2 - 2, 12,
              Component.translatable("wildfire_gender.breast_customization.tab_miscellaneous"), button -> updateTab(Tab.MISC))
        );

        this.btnHurtSounds = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY - 2, 166, 20,
              Component.translatable("wildfire_gender.char_settings.hurt_sounds", plr.hasHurtSounds() ? ENABLED : DISABLED), button -> {
            boolean enableHurtSounds = !plr.hasHurtSounds();
            if (plr.updateHurtSounds(enableHurtSounds)) {
                voicePitchSlider.active = plr.hasHurtSounds();
                button.setMessage(Component.translatable("wildfire_gender.char_settings.hurt_sounds", enableHurtSounds ? ENABLED : DISABLED));
                PlayerConfig.saveGenderInfo(plr);
            }
        }, Tooltip.create(Component.translatable("wildfire_gender.tooltip.hurt_sounds"))));

        this.voicePitchSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36, tabOffsetY + 22, 166 / 2 - 2, 20, ClientConfiguration.VOICE_PITCH, plr.getVoicePitch(), value -> {
        }, value -> Component.translatable("wildfire_gender.slider.voice_pitch", Math.round(value * 100)), value -> {
            if (plr.updateVoicePitch(value)) {
                PlayerConfig.saveGenderInfo(plr);
                Player player = minecraft.player;
                SoundEvent hurtSound = plr.getGender().getHurtSound();
                if (player != null && hurtSound != null) {
                    float pitch = (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F /*+ 1.0F*/; // +1 is from getVoicePitch()
                    player.playSound(hurtSound, 1f, pitch + plr.getVoicePitch());
                }
            }
        }));
        voicePitchSlider.active = plr.hasHurtSounds();
        this.voicePitchSlider.setArrowKeyStep(0.01);

        btnHideInArmor = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY + 46, 166, 20,
              Component.translatable("wildfire_gender.char_settings.hide_in_armor", plr.showBreastsInArmor() ? DISABLED : ENABLED), button -> {
            boolean enableShowInArmor = !plr.showBreastsInArmor();
            if (plr.updateShowBreastsInArmor(enableShowInArmor)) {
                button.setMessage(Component.translatable("wildfire_gender.char_settings.hide_in_armor", enableShowInArmor ? DISABLED : ENABLED));
                PlayerConfig.saveGenderInfo(plr);
            }
        }));

        btnShowTooltips = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY + 70, 166, 20,
              Component.translatable("wildfire_gender.char_settings.show_armor_stat", GeneralClientConfig.INSTANCE.armorStat.get() ? ENABLED : DISABLED), button -> {
            boolean displayArmorStat = GeneralClientConfig.INSTANCE.armorStat.get();
            GeneralClientConfig.INSTANCE.armorStat.set(!displayArmorStat);
            GeneralClientConfig.INSTANCE.save();
            button.setMessage(Component.translatable("wildfire_gender.char_settings.show_armor_stat", displayArmorStat ? DISABLED : ENABLED));
        }));

        btnHolidayThemes = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY + 94, 166, 20,
              Component.translatable("wildfire_gender.misc.holiday_themes", plr.hasHolidayThemes() ? ENABLED : DISABLED), button -> {
            boolean enableHolidayThemes = !plr.hasHolidayThemes();
            if (plr.updateHolidayThemes(enableHolidayThemes)) {
                button.setMessage(Component.translatable("wildfire_gender.misc.holiday_themes", plr.hasHolidayThemes() ? ENABLED : DISABLED));
            }
        }, Tooltip.create(Component.translatable("wildfire_gender.tooltip.holiday_themes.line1"))
                /*.append("\n\n")
                .append(Text.translatable("wildfire_gender.tooltip.holiday_themes.line2")))*/
        ));
    }

    private void initPresetTab(PlayerConfig plr, FloatConsumer onSave, int j, int tabOffsetY) {
        //PRESET_LIST = addWidget(new WildfireBreastPresetList(this, 156, j - 48, 125));
        //PRESET_LIST.setX(this.width / 2 + 30);
    }

    private void updateTab(Tab tab) {
        currentTab = tab;
        boolean customization = currentTab == Tab.CUSTOMIZATION;
        this.btnCustomization.active = !customization;
        this.breastSlider.visible = customization;
        this.xOffsetBoobSlider.visible = customization;
        this.yOffsetBoobSlider.visible = customization;
        this.zOffsetBoobSlider.visible = customization;
        this.cleavageSlider.visible = customization;

        boolean physics = currentTab == Tab.PHYSICS;
        this.btnPhysics.active = !physics;
        this.btnBreastPhysics.visible = physics;
        this.btnDualPhysics.visible = physics;
        this.bounceSlider.visible = physics;
        this.floppySlider.visible = physics;
        this.btnOverrideArmorPhys.visible = physics;

        boolean miscellaneous = currentTab == Tab.MISC;
        this.btnMiscellaneous.active = !miscellaneous;
        this.btnHideInArmor.visible = miscellaneous;
        this.btnHurtSounds.visible = miscellaneous;
        this.voicePitchSlider.visible = miscellaneous;
        this.btnShowTooltips.visible = miscellaneous;
        this.btnHolidayThemes.visible = miscellaneous;
    }

    private void createNewPreset(String presetName) {
        PlayerConfig player = this.getPlayer();
        BreastPresetConfiguration cfg = new BreastPresetConfiguration(presetName);
        cfg.set(BreastPresetConfiguration.PRESET_NAME, presetName);
        cfg.set(BreastPresetConfiguration.BUST_SIZE, player.getBustSize());
        cfg.set(BreastPresetConfiguration.BREASTS_UNIBOOB, player.getBreasts().isUniboob());
        cfg.set(BreastPresetConfiguration.BREASTS_CLEAVAGE, player.getBreasts().getCleavage());
        cfg.set(BreastPresetConfiguration.BREASTS_OFFSET_X, player.getBreasts().getXOffset());
        cfg.set(BreastPresetConfiguration.BREASTS_OFFSET_Y, player.getBreasts().getYOffset());
        cfg.set(BreastPresetConfiguration.BREASTS_OFFSET_Z, player.getBreasts().getZOffset());
        cfg.save();

        //PRESET_LIST.refreshList();
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        PlayerConfig plr = getPlayer();
        if (plr == null) {
            return;
        }
        ResourceLocation backgroundTexture = switch (plr.getGender()) {
            case Gender.MALE -> null;
            case Gender.FEMALE -> BACKGROUND_FEMALE;
            case Gender.OTHER -> BACKGROUND_OTHER;
        };

        if (backgroundTexture != null) {
            //TODO - 1.21.4: RenderType::guiTextured,
            graphics.blit(backgroundTexture, (this.width - 272) / 2, (this.height - 138) / 2, 0, 0, 272, 130, 512, 512);
        }

        //TODO - 1.21.4: RenderType::guiTextured,
        graphics.blit(currentTab.background, (this.width) / 2 - 42, (this.height) / 2 - 43, 0, 0, 178, currentTab.backgroundHeight, 512, 512);

        int x = this.width / 2;
        int y = this.height / 2;
        Component title = getTitle();
        graphics.drawString(font, title, x + font.width(title), y - 82, 0xFFFFFF, false);

        if (minecraft != null && minecraft.level != null) {
            Player ent = minecraft.level.getPlayerByUUID(this.playerUUID);
            if (ent != null) {
                WildfireHelper.withEntityAngles(ent, PREVIEW_Y_BODY_ROT, PREVIEW_Y_ROT, PREVIEW_X_ROT, entity -> InventoryScreen.renderEntityInInventory(graphics,
                      x - 102, y + 75, 200, new Vector3f(0, entity.getBbHeight() / 2F, 0), PREVIEW_ANGLE, CAMERA_ORIENTATION, entity));
            }
        }
    }

    /*@Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = this.width / 2;
        int y = this.height / 2;

        if (currentTab == Tab.PRESETS) {
            PRESET_LIST.render(graphics, mouseX, mouseY, partialTick);
            if (!PRESET_LIST.hasPresets()) {
                graphics.drawCenteredString(font, Component.translatable("wildfire_gender.breast_customization.presets.none"), x + ((190 + 28) / 2), y - 4, 0xFFFFFF);
            }
        }
    }*/

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        //Ensure all sliders are saved
        breastSlider.save();
        xOffsetBoobSlider.save();
        yOffsetBoobSlider.save();
        zOffsetBoobSlider.save();
        cleavageSlider.save();
        floppySlider.save();
        bounceSlider.save();
        voicePitchSlider.save();
        return super.mouseReleased(mouseX, mouseY, state);
    }

    private enum Tab {
        CUSTOMIZATION("breast_customization_tab", 80),
        PHYSICS("breast_physics_tab", 104),
        MISC("miscellaneous_tab", 128);

        private final ResourceLocation background;
        private final int backgroundHeight;

        Tab(String tab, int backgroundHeight) {
            this.background = WildfireGender.rl("textures/gui/tabs/" + tab + ".png");
            this.backgroundHeight = backgroundHeight;
        }
    }
}

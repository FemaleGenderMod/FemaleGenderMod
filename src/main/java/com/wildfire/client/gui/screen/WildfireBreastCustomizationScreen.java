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
import com.wildfire.main.WildfireLang;
import com.wildfire.main.config.BreastPresetConfiguration;
import com.wildfire.main.config.ClientConfiguration;
import com.wildfire.main.config.GeneralClientConfig;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.text.TextComponentUtil;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import java.util.UUID;
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
        super(WildfireLang.APPEARANCE_SETTINGS.translate(), parent, uuid);
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
              WildfireLang.BREAST_CUSTOMIZATION_TAB_CUSTOMIZATION.translate(), button -> updateTab(Tab.CUSTOMIZATION))
        );
        this.btnCustomization.active = false;

        this.breastSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36, tabOffsetY - 2, 166, 20, ClientConfiguration.BUST_SIZE, plr.getBustSize(),
              plr::updateBustSize, value -> WildfireLang.WARDROBE_SLIDER_BREAST_SIZE.translate(Math.round(value * 1.25f * 100)), onSave));
        this.breastSlider.setArrowKeyStep(0.01);

        this.xOffsetBoobSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36, tabOffsetY + 22, 166 / 2 - 2, 20, ClientConfiguration.BREASTS_OFFSET_X, breasts.getXOffset(),
              breasts::updateXOffset, value -> WildfireLang.WARDROBE_SLIDER_SEPARATION.translate(Math.round((Math.round(value * 100f) / 100f) * 10)), onSave));
        this.yOffsetBoobSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36 + 166 / 2 + 2, tabOffsetY + 22, 166 / 2 - 2, 20, ClientConfiguration.BREASTS_OFFSET_Y, breasts.getYOffset(),
              breasts::updateYOffset, value -> WildfireLang.WARDROBE_SLIDER_HEIGHT.translate(Math.round((Math.round(value * 100f) / 100f) * 10)), onSave));

        this.zOffsetBoobSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36, tabOffsetY + 46, 166 / 2 - 2, 20, ClientConfiguration.BREASTS_OFFSET_Z, breasts.getZOffset(),
              breasts::updateZOffset, value -> WildfireLang.WARDROBE_SLIDER_DEPTH.translate(Math.round((Math.round(value * 100f) / 100f) * 10)), onSave));
        this.zOffsetBoobSlider.setArrowKeyStep(0.1);
        this.cleavageSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36 + 166 / 2 + 2, tabOffsetY + 46, 166 / 2 - 2, 20, ClientConfiguration.BREASTS_CLEAVAGE, breasts.getCleavage(),
              breasts::updateCleavage, value -> WildfireLang.WARDROBE_SLIDER_ROTATION.translate(Math.round((Math.round(value * 100f) / 100f) * 100)), onSave));
        this.cleavageSlider.setArrowKeyStep(0.1);
    }

    private void initPhysicsTab(PlayerConfig plr, FloatConsumer onSave, int j, int tabOffsetY) {
        Breasts breasts = plr.getBreasts();

        this.btnPhysics = addRenderableWidget(new WildfireButton(this.width / 2 - 42, j - 52, 172 / 2 - 2, 12,
              WildfireLang.BREAST_CUSTOMIZATION_TAB_PHYSICS.translate(), button -> updateTab(Tab.PHYSICS))
        );

        this.btnBreastPhysics = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY - 2, 166, 20,
              WildfireLang.CHAR_SETTING_PHYSICS.translate(plr.hasBreastPhysics()), button -> {
            boolean enablePhysics = !plr.hasBreastPhysics();
            if (plr.updateBreastPhysics(enablePhysics)) {

                this.bounceSlider.active = plr.hasBreastPhysics();
                this.floppySlider.active = plr.hasBreastPhysics();
                this.btnOverrideArmorPhys.active = plr.hasBreastPhysics();
                this.btnDualPhysics.active = plr.hasBreastPhysics();

                button.setMessage(WildfireLang.CHAR_SETTING_PHYSICS.translate(enablePhysics));
                PlayerConfig.saveGenderInfo(plr);
            }
        }));

        this.btnDualPhysics = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY + 22, 166, 20,
              WildfireLang.BREAST_CUSTOMIZATION_DUAL_PHYSICS.translate(breasts.isUniboob() ? CommonComponents.GUI_NO : CommonComponents.GUI_YES), button -> {
            boolean isUniboob = !breasts.isUniboob();
            if (breasts.updateUniboob(isUniboob)) {
                button.setMessage(WildfireLang.BREAST_CUSTOMIZATION_DUAL_PHYSICS.translate(isUniboob ? CommonComponents.GUI_NO : CommonComponents.GUI_YES));
                PlayerConfig.saveGenderInfo(plr);
            }
        }));
        this.btnDualPhysics.active = plr.hasBreastPhysics();

        this.btnOverrideArmorPhys = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY + 70, 166, 20,
              WildfireLang.CHAR_SETTING_OVERRIDE_PHYSICS.translate(plr.getArmorPhysicsOverride()), button -> {
            boolean enableArmorPhysicsOverride = !plr.getArmorPhysicsOverride();
            if (plr.updateArmorPhysicsOverride(enableArmorPhysicsOverride)) {
                button.setMessage(WildfireLang.CHAR_SETTING_OVERRIDE_PHYSICS.translate(enableArmorPhysicsOverride));
                PlayerConfig.saveGenderInfo(plr);
            }
        }, Tooltip.create(TextComponentUtil.build(WildfireLang.TOOLTIP_OVERRIDE_PHYSICS_1, "\n\n", WildfireLang.TOOLTIP_OVERRIDE_PHYSICS_2))));
        this.btnOverrideArmorPhys.active = plr.hasBreastPhysics();

        this.bounceSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36, tabOffsetY + 46, 166 / 2 - 2, 20, ClientConfiguration.BOUNCE_MULTIPLIER, plr.getBounceMultiplier(), value -> {
        }, value -> {
            float bounceText = 3 * value;
            int v = Math.round(bounceText * 100);
            //bounceWarning = v > 100;
            return WildfireLang.SLIDER_BOUNCE.translate(v);
        }, value -> {
            if (plr.updateBounceMultiplier(value)) {
                PlayerConfig.saveGenderInfo(plr);
            }
        }));
        this.bounceSlider.active = plr.hasBreastPhysics();
        this.bounceSlider.setArrowKeyStep(0.005);

        this.floppySlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36 + 166 / 2 + 2, tabOffsetY + 46, 166 / 2 - 2, 20, ClientConfiguration.FLOPPY_MULTIPLIER, plr.getFloppiness(), value -> {
        }, value -> WildfireLang.SLIDER_FLOPPY.translate(Math.round(value * 100)), value -> {
            if (plr.updateFloppiness(value)) {
                PlayerConfig.saveGenderInfo(plr);
            }
        }));
        this.floppySlider.active = plr.hasBreastPhysics();
        this.floppySlider.setArrowKeyStep(0.01);
    }

    private void initMiscTab(PlayerConfig plr, FloatConsumer onSave, int j, int tabOffsetY) {
        this.btnMiscellaneous = addRenderableWidget(new WildfireButton(this.width / 2 + 46, j - 52, 172 / 2 - 2, 12,
              WildfireLang.BREAST_CUSTOMIZATION_TAB_MISC.translate(), button -> updateTab(Tab.MISC))
        );

        this.btnHurtSounds = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY - 2, 166, 20,
              WildfireLang.CHAR_SETTING_HURT_SOUNDS.translate(plr.hasHurtSounds()), button -> {
            boolean enableHurtSounds = !plr.hasHurtSounds();
            if (plr.updateHurtSounds(enableHurtSounds)) {
                voicePitchSlider.active = plr.hasHurtSounds();
                button.setMessage(WildfireLang.CHAR_SETTING_HURT_SOUNDS.translate(enableHurtSounds));
                PlayerConfig.saveGenderInfo(plr);
            }
        }, Tooltip.create(WildfireLang.TOOLTIP_HURT_SOUNDS.translate())));

        this.voicePitchSlider = addRenderableWidget(new WildfireSlider(this.width / 2 - 36, tabOffsetY + 22, 166 / 2 - 2, 20, ClientConfiguration.VOICE_PITCH, plr.getVoicePitch(), value -> {
        }, value -> WildfireLang.SLIDER_VOICE_PITCH.translate(Math.round(value * 100)), value -> {
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
              WildfireLang.CHAR_SETTING_HIDE_IN_ARMOR.translate(!plr.showBreastsInArmor()), button -> {
            boolean enableShowInArmor = !plr.showBreastsInArmor();
            if (plr.updateShowBreastsInArmor(enableShowInArmor)) {
                button.setMessage(WildfireLang.CHAR_SETTING_HIDE_IN_ARMOR.translate(!enableShowInArmor));
                PlayerConfig.saveGenderInfo(plr);
            }
        }));

        btnShowTooltips = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY + 70, 166, 20,
              WildfireLang.CHAR_SETTING_SHOW_ARMOR_STAT.translate(GeneralClientConfig.INSTANCE.armorStat.get()), button -> {
            boolean displayArmorStat = !GeneralClientConfig.INSTANCE.armorStat.get();
            GeneralClientConfig.INSTANCE.armorStat.set(displayArmorStat);
            GeneralClientConfig.INSTANCE.save();
            button.setMessage(WildfireLang.CHAR_SETTING_SHOW_ARMOR_STAT.translate(displayArmorStat));
        }));

        btnHolidayThemes = addRenderableWidget(new WildfireButton(this.width / 2 - 36, tabOffsetY + 94, 166, 20,
              WildfireLang.HOLIDAY_THEMES.translate(plr.hasHolidayThemes()), button -> {
            boolean enableHolidayThemes = !plr.hasHolidayThemes();
            if (plr.updateHolidayThemes(enableHolidayThemes)) {
                button.setMessage(WildfireLang.HOLIDAY_THEMES.translate(plr.hasHolidayThemes()));
            }
        }, Tooltip.create(WildfireLang.TOOLTIP_HOLIDAY_THEMES_1.translate())));
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
            graphics.blit(backgroundTexture, (this.width - 272) / 2, (this.height - 138) / 2, 0, 0, 272, 130, 512, 512);
        }

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

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

package com.wildfire.gui.screen;

import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.WildfireBreastPresetList;
import com.wildfire.gui.WildfireButton;
import com.wildfire.gui.WildfireSlider;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.GlobalConfig;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.BreastPresetConfiguration;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class WildfireBreastCustomizationScreen extends BaseWildfireScreen {

    private static final Text ENABLED = Text.translatable("wildfire_gender.label.enabled").formatted(Formatting.GREEN);
    private static final Text DISABLED = Text.translatable("wildfire_gender.label.disabled").formatted(Formatting.RED);

    private static final Identifier BACKGROUND_FEMALE = Identifier.of(WildfireGender.MODID, "textures/gui/breast_customization.png");
    private static final Identifier BACKGROUND_OTHER = Identifier.of(WildfireGender.MODID, "textures/gui/breast_customization_other.png");

    private static final Identifier BACKGROUND_CUSTOMIZATION = Identifier.of(WildfireGender.MODID, "textures/gui/tabs/breast_customization_tab.png");
    private static final Identifier BACKGROUND_PHYSICS = Identifier.of(WildfireGender.MODID, "textures/gui/tabs/breast_physics_tab.png");
    private static final Identifier BACKGROUND_MISC = Identifier.of(WildfireGender.MODID, "textures/gui/tabs/miscellaneous_tab.png");

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

    private WildfireBreastPresetList PRESET_LIST;
    private int currentTab = 0; // 0 = customization, 1 = presets

    public WildfireBreastCustomizationScreen(Screen parent, UUID uuid) {
        super(Text.translatable("wildfire_gender.appearance_settings.title"), parent, uuid);
    }

    @Override
    public void init() {
        int j = this.height / 2 - 11;

        int xPos = this.width / 2;
        int yPos = this.height / 2;

        PlayerConfig plr = Objects.requireNonNull(getPlayer(), "getPlayer()");
        Breasts breasts = plr.getBreasts();
        FloatConsumer onSave = value -> {
            //Just save as we updated the actual value in value change
            PlayerConfig.saveGenderInfo(plr);
        };

        //Customization Tab
        this.addDrawableChild(btnCustomization = new WildfireButton(this.width / 2 - 130, j - 52, 172/2 - 2, 12,
                Text.translatable("wildfire_gender.breast_customization.tab_customization"), button -> {
            currentTab = 0;
            updateTabs();

        })).setActive(false);

        //Breast Physics Tab
        this.addDrawableChild(btnPhysics = new WildfireButton(this.width / 2 - 42, j - 52, 172 / 2 - 2, 12,
                Text.translatable("wildfire_gender.breast_customization.tab_physics"), button -> {

            currentTab = 1;
            updateTabs();
            //PRESET_LIST.refreshList();
        }));

        //Miscellaneous
        this.addDrawableChild(btnMiscellaneous = new WildfireButton(this.width / 2 + 46, j - 52, 172 / 2 - 2, 12,
                Text.translatable("wildfire_gender.breast_customization.tab_miscellaneous"), button -> {

            currentTab = 2;
            updateTabs();
            //PRESET_LIST.refreshList();
        }));

        //Customization Tab Below
        int tabOffsetY = j-3 - 21;

        this.addDrawableChild(this.breastSlider = new WildfireSlider(this.width / 2 - 36, tabOffsetY - 2, 166, 20, Configuration.BUST_SIZE, plr.getBustSize(),
              plr::updateBustSize, value -> Text.translatable("wildfire_gender.wardrobe.slider.breast_size", Math.round(value * 1.25f * 100)), onSave));
        this.breastSlider.setArrowKeyStep(0.01);

        //Customization
        this.addDrawableChild(this.xOffsetBoobSlider = new WildfireSlider(this.width / 2 - 36, tabOffsetY + 22, 166 / 2 - 2, 20, Configuration.BREASTS_OFFSET_X, breasts.getXOffset(),
              breasts::updateXOffset, value -> Text.translatable("wildfire_gender.wardrobe.slider.separation", Math.round((Math.round(value * 100f) / 100f) * 10)), onSave));
        this.addDrawableChild(this.yOffsetBoobSlider = new WildfireSlider(this.width / 2 - 36 + 166/2 + 2, tabOffsetY + 22, 166 / 2 - 2, 20, Configuration.BREASTS_OFFSET_Y, breasts.getYOffset(),
              breasts::updateYOffset, value -> Text.translatable("wildfire_gender.wardrobe.slider.height", Math.round((Math.round(value * 100f) / 100f) * 10)), onSave));

        this.addDrawableChild(this.zOffsetBoobSlider = new WildfireSlider(this.width / 2 - 36, tabOffsetY + 46, 166 / 2 - 2, 20, Configuration.BREASTS_OFFSET_Z, breasts.getZOffset(),
              breasts::updateZOffset, value -> Text.translatable("wildfire_gender.wardrobe.slider.depth", Math.round((Math.round(value * 100f) / 100f) * 10)), onSave));
        this.zOffsetBoobSlider.setArrowKeyStep(0.1);
        this.addDrawableChild(this.cleavageSlider = new WildfireSlider(this.width / 2 - 36 + 166/2 + 2, tabOffsetY + 46, 166 / 2 - 2, 20, Configuration.BREASTS_CLEAVAGE, breasts.getCleavage(),
              breasts::updateCleavage, value -> Text.translatable("wildfire_gender.wardrobe.slider.rotation", Math.round((Math.round(value * 100f) / 100f) * 100)), onSave));
        this.cleavageSlider.setArrowKeyStep(0.1);

        //Breast Physics Tab

        this.addDrawableChild(this.btnBreastPhysics = new WildfireButton(this.width / 2 - 36, tabOffsetY - 2, 166, 20,
                Text.translatable("wildfire_gender.char_settings.physics", plr.hasBreastPhysics() ? ENABLED : DISABLED), button -> {
            boolean enablePhysics = !plr.hasBreastPhysics();
            if (plr.updateBreastPhysics(enablePhysics)) {

                this.bounceSlider.active = plr.hasBreastPhysics();
                this.floppySlider.active = plr.hasBreastPhysics();
                this.btnOverrideArmorPhys.active = plr.hasBreastPhysics();
                this.btnDualPhysics.active = plr.hasBreastPhysics();

                button.setMessage(Text.translatable("wildfire_gender.char_settings.physics", enablePhysics ? ENABLED : DISABLED));
                PlayerConfig.saveGenderInfo(plr);
            }
        }));

        this.addDrawableChild(this.btnDualPhysics = new WildfireButton(this.width / 2 - 36, tabOffsetY + 22, 166, 20,
                Text.translatable("wildfire_gender.breast_customization.dual_physics", Text.translatable(breasts.isUniboob() ? "wildfire_gender.label.no" : "wildfire_gender.label.yes")), button -> {
            boolean isUniboob = !breasts.isUniboob();
            if (breasts.updateUniboob(isUniboob)) {
                button.setMessage(Text.translatable("wildfire_gender.breast_customization.dual_physics", Text.translatable(isUniboob ? "wildfire_gender.label.no" : "wildfire_gender.label.yes")));
                PlayerConfig.saveGenderInfo(plr);
            }
        }));
        this.btnDualPhysics.active = plr.hasBreastPhysics();

        //this.btnHideInArmor.active = aPlr.hasBreastPhysics();


        this.addDrawableChild(btnOverrideArmorPhys = new WildfireButton(this.width / 2 - 36, tabOffsetY + 70, 166, 20,
                Text.translatable("wildfire_gender.char_settings.override_armor_physics", plr.getArmorPhysicsOverride() ? ENABLED : DISABLED), button -> {
            boolean enableArmorPhysicsOverride = !plr.getArmorPhysicsOverride();
            if (plr.updateArmorPhysicsOverride(enableArmorPhysicsOverride )) {
                button.setMessage(Text.translatable("wildfire_gender.char_settings.override_armor_physics", plr.getArmorPhysicsOverride() ? ENABLED : DISABLED));
                PlayerConfig.saveGenderInfo(plr);
            }
        }, Tooltip.of(Text.translatable("wildfire_gender.tooltip.override_armor_physics.line1")
                .append("\n\n")
                .append(Text.translatable("wildfire_gender.tooltip.override_armor_physics.line2")))
        ));
        this.btnOverrideArmorPhys.active = plr.hasBreastPhysics();

        this.addDrawableChild(this.bounceSlider = new WildfireSlider(this.width / 2 - 36, tabOffsetY + 46, 166 / 2 - 2, 20, Configuration.BOUNCE_MULTIPLIER, plr.getBounceMultiplier(), value -> {
        }, value -> {
            float bounceText = 3 * value;
            int v = Math.round(bounceText * 100);
            //bounceWarning = v > 100;
            return Text.translatable("wildfire_gender.slider.bounce", v);
        }, value -> {
            if (plr.updateBounceMultiplier(value)) {
                PlayerConfig.saveGenderInfo(plr);
            }
        }));
        this.bounceSlider.active = plr.hasBreastPhysics();
        this.bounceSlider.setArrowKeyStep(0.005);

        this.addDrawableChild(this.floppySlider = new WildfireSlider(this.width / 2 - 36 + 166/2 + 2, tabOffsetY + 46, 166 / 2 - 2, 20, Configuration.FLOPPY_MULTIPLIER, plr.getFloppiness(), value -> {
        }, value -> Text.translatable("wildfire_gender.slider.floppy", Math.round(value * 100)), value -> {
            if (plr.updateFloppiness(value)) {
                PlayerConfig.saveGenderInfo(plr);
            }
        }));
        this.floppySlider.active = plr.hasBreastPhysics();
        this.floppySlider.setArrowKeyStep(0.01);


        //Miscellaneous Tab


        this.addDrawableChild(this.btnHurtSounds = new WildfireButton(this.width / 2 - 36, tabOffsetY - 2, 166, 20,
                Text.translatable("wildfire_gender.char_settings.hurt_sounds", plr.hasHurtSounds() ? ENABLED : DISABLED), button -> {
            boolean enableHurtSounds = !plr.hasHurtSounds();
            if (plr.updateHurtSounds(enableHurtSounds)) {
                voicePitchSlider.active = plr.hasHurtSounds();
                button.setMessage(Text.translatable("wildfire_gender.char_settings.hurt_sounds", enableHurtSounds ? ENABLED : DISABLED));
                PlayerConfig.saveGenderInfo(plr);
            }
        }, Tooltip.of(Text.translatable("wildfire_gender.tooltip.hurt_sounds"))));

        this.addDrawableChild(this.voicePitchSlider = new WildfireSlider(this.width / 2 - 36, tabOffsetY + 22, 166 / 2 - 2, 20, Configuration.VOICE_PITCH, plr.getVoicePitch(), value -> {
        }, value -> Text.translatable("wildfire_gender.slider.voice_pitch", Math.round(value * 100)), value -> {
            if (plr.updateVoicePitch(value)) {
                PlayerConfig.saveGenderInfo(plr);
                if(client.player != null) {
                    SoundEvent hurtSound = plr.getGender().getHurtSound();
                    if(hurtSound != null) {
                        float pitch = (client.player.getRandom().nextFloat() - client.player.getRandom().nextFloat()) * 0.2F /*+ 1.0F*/; // +1 is from getVoicePitch()
                        client.player.playSound(hurtSound, 1f, pitch + plr.getVoicePitch());
                    }
                }
            }
        }));
        voicePitchSlider.active = plr.hasHurtSounds();
        this.voicePitchSlider.setArrowKeyStep(0.01);

        this.addDrawableChild(btnHideInArmor = new WildfireButton(this.width / 2 - 36, tabOffsetY + 46, 166, 20,
                Text.translatable("wildfire_gender.char_settings.hide_in_armor", plr.showBreastsInArmor() ? DISABLED : ENABLED), button -> {
            boolean enableShowInArmor = !plr.showBreastsInArmor();
            if (plr.updateShowBreastsInArmor(enableShowInArmor)) {
                button.setMessage(Text.translatable("wildfire_gender.char_settings.hide_in_armor", enableShowInArmor ? DISABLED : ENABLED));
                PlayerConfig.saveGenderInfo(plr);
            }
        }));

        var config = GlobalConfig.INSTANCE;

        this.addDrawableChild(btnShowTooltips = new WildfireButton(this.width / 2 - 36, tabOffsetY + 70, 166, 20,
                Text.translatable("wildfire_gender.char_settings.show_armor_stat", config.get(GlobalConfig.ARMOR_STAT) ? ENABLED : DISABLED), button -> {
            config.set(GlobalConfig.ARMOR_STAT, !config.get(GlobalConfig.ARMOR_STAT));
            config.save();
            button.setMessage(Text.translatable("wildfire_gender.char_settings.show_armor_stat", config.get(GlobalConfig.ARMOR_STAT) ? ENABLED : DISABLED));
        }));

        this.addDrawableChild(btnHolidayThemes = new WildfireButton(this.width / 2 - 36, tabOffsetY + 94, 166, 20,
                Text.translatable("wildfire_gender.misc.holiday_themes", plr.hasHolidayThemes() ? ENABLED : DISABLED), button -> {
            boolean enableHolidayThemes = !plr.hasHolidayThemes();
            if(plr.updateHolidayThemes(enableHolidayThemes)) {
                button.setMessage(Text.translatable("wildfire_gender.misc.holiday_themes", plr.hasHolidayThemes() ? ENABLED : DISABLED));
            }
        }, Tooltip.of(Text.translatable("wildfire_gender.tooltip.holiday_themes.line1"))
                /*.append("\n\n")
                .append(Text.translatable("wildfire_gender.tooltip.holiday_themes.line2")))*/
        ));

        //Preset Tab Below
        PRESET_LIST = new WildfireBreastPresetList(this, 156, (j - 48));
        PRESET_LIST.setX(this.width / 2 + 30);
        PRESET_LIST.setHeight(125);

        this.addSelectableChild(this.PRESET_LIST);

        updateTabs();

        super.init();
    }

    private void updateTabs() {
        btnCustomization.active = currentTab != 0;
        btnPhysics.active = currentTab != 1;
        btnMiscellaneous.active = currentTab != 2;

        this.breastSlider.visible = currentTab == 0;
        this.xOffsetBoobSlider.visible = currentTab == 0;
        this.yOffsetBoobSlider.visible = currentTab == 0;
        this.zOffsetBoobSlider.visible = currentTab == 0;
        this.cleavageSlider.visible = currentTab == 0;

        this.btnBreastPhysics.visible = currentTab == 1;
        this.btnDualPhysics.visible = currentTab == 1;
        this.bounceSlider.visible = currentTab == 1;
        this.floppySlider.visible = currentTab == 1;
        this.btnOverrideArmorPhys.visible = currentTab == 1;

        this.btnHideInArmor.visible = currentTab == 2;
        this.btnHurtSounds.visible = currentTab == 2;
        this.voicePitchSlider.visible = currentTab == 2;
        this.btnShowTooltips.visible = currentTab == 2;
        this.btnHolidayThemes.visible = currentTab == 2;
    }


    private void createNewPreset(String presetName) {
        BreastPresetConfiguration cfg = new BreastPresetConfiguration(presetName);
        PlayerConfig plr = Objects.requireNonNull(getPlayer(), "getPlayer()");
        cfg.set(BreastPresetConfiguration.PRESET_NAME, presetName);
        cfg.set(BreastPresetConfiguration.BUST_SIZE, plr.getBustSize());
        cfg.set(BreastPresetConfiguration.BREASTS_UNIBOOB, plr.getBreasts().isUniboob());
        cfg.set(BreastPresetConfiguration.BREASTS_CLEAVAGE, plr.getBreasts().getCleavage());
        cfg.set(BreastPresetConfiguration.BREASTS_OFFSET_X, plr.getBreasts().getXOffset());
        cfg.set(BreastPresetConfiguration.BREASTS_OFFSET_Y, plr.getBreasts().getYOffset());
        cfg.set(BreastPresetConfiguration.BREASTS_OFFSET_Z, plr.getBreasts().getZOffset());
        cfg.save();

        PRESET_LIST.refreshList();
    }

    private void updatePresetTab() {
        PlayerConfig plr = getPlayer();
        if(plr == null) return;
        boolean canHaveBreasts = plr.getGender().canHaveBreasts();
        breastSlider.visible = canHaveBreasts && currentTab == 0;
        xOffsetBoobSlider.visible = canHaveBreasts && currentTab == 0;
        yOffsetBoobSlider.visible = canHaveBreasts && currentTab == 0;
        zOffsetBoobSlider.visible = canHaveBreasts && currentTab == 0;
        cleavageSlider.visible = canHaveBreasts && currentTab == 0;
        btnDualPhysics.visible = canHaveBreasts && currentTab == 0;
        PRESET_LIST.visible = currentTab == 1;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(ctx);

        PlayerConfig plr = getPlayer();
        if(plr == null) return;
        Identifier backgroundTexture = switch(plr.getGender()) {
            case Gender.MALE -> null;
            case Gender.FEMALE -> BACKGROUND_FEMALE;
            case Gender.OTHER -> BACKGROUND_OTHER;
        };

        if(backgroundTexture != null) {
            ctx.drawTexture(RenderLayer::getGuiTextured, backgroundTexture, (this.width - 272) / 2, (this.height - 138) / 2, 0, 0, 272, 130, 512, 512);
        }

        if(currentTab == 0) {
            ctx.drawTexture(RenderLayer::getGuiTextured, BACKGROUND_CUSTOMIZATION, (this.width) / 2 - 42, (this.height) / 2 - 43, 0, 0, 178, 80, 512, 512);
        } else if(currentTab == 1) {
            ctx.drawTexture(RenderLayer::getGuiTextured, BACKGROUND_PHYSICS, (this.width) / 2 - 42, (this.height) / 2 - 43, 0, 0, 178, 104, 512, 512);
        } else if(currentTab == 2) {
            ctx.drawTexture(RenderLayer::getGuiTextured, BACKGROUND_MISC, (this.width) / 2 - 42, (this.height) / 2 - 43, 0, 0, 178, 128, 512, 512);
        }

        int x = this.width / 2;
        int y = this.height / 2;
        //ctx.fill(x + 28, y - 64 - 21, x + 190, y + 68, 0x55000000);
        //ctx.fill(x + 29, y - 63 - 21, x + 189, y - 60, 0x55000000);
        ctx.drawText(textRenderer, getTitle(), x - textRenderer.getWidth(getTitle()) / 2, y - 82, 0xFFFFFF, false);

        if(client != null && client.world != null) {
            int xP = this.width / 2 - 90;
            int yP = this.height / 2 + 44;
            PlayerEntity ent = client.world.getPlayerByUuid(this.playerUUID);
            if(ent != null) {
                ctx.enableScissor(xP - 38, yP - 97, xP + 38, yP + 9);
                GuiUtils.drawEntityOnScreen(ctx, xP, yP + 60, 70, (xP - mouseX), (yP - 46 - mouseY), ent);
                ctx.disableScissor();
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if(client == null || client.player == null || client.world == null) return;
        //updatePresetTab();
        super.render(ctx, mouseX, mouseY, delta);

        int x = this.width / 2;
        int y = this.height / 2;

        //Breast physics
        if(currentTab == 1) {
            /*PRESET_LIST.render(ctx, mouseX, mouseY, delta);
            if(PRESET_LIST.getPresetList().length == 0) {
                ctx.drawText(textRenderer, "No Presets Found", x + ((190 + 28) / 2) - textRenderer.getWidth("No Presets Found") / 2, y - 4, 0xFFFFFF, false);
            }*/
        }
    }

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
}

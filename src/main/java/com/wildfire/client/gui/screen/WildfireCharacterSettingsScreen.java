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
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLang;
import com.wildfire.main.config.ClientConfiguration;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.text.TextComponentUtil;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class WildfireCharacterSettingsScreen extends BaseWildfireScreen {

    private static final ResourceLocation BACKGROUND = WildfireGender.rl("textures/gui/settings_bg.png");

    private WildfireSlider bounceSlider, floppySlider, voicePitchSlider;
    private WildfireButton btnHideInArmor, btnOverrideArmorPhys;
    private boolean bounceWarning;

    protected WildfireCharacterSettingsScreen(Screen parent, UUID uuid) {
        super(WildfireLang.CHAR_SETTINGS.translate(), parent, uuid);
    }

    @Override
    public void init() {
        PlayerConfig aPlr = getPlayer();

        int x = this.width / 2;
        int y = this.height / 2;

        int yPos = y - 47;
        int xPos = x - 156 / 2 - 1;

        this.addRenderableWidget(new WildfireButton(xPos, yPos, 157, 20, WildfireLang.CHAR_SETTING_PHYSICS.translate(aPlr.hasBreastPhysics()), button -> {
            boolean enablePhysics = !aPlr.hasBreastPhysics();
            if (aPlr.updateBreastPhysics(enablePhysics)) {
                this.bounceSlider.active = aPlr.hasBreastPhysics();
                this.floppySlider.active = aPlr.hasBreastPhysics();
                //this.btnHideInArmor.active = aPlr.hasBreastPhysics();
                this.btnOverrideArmorPhys.active = aPlr.hasBreastPhysics();

                button.setMessage(WildfireLang.CHAR_SETTING_PHYSICS.translate(enablePhysics));
                PlayerConfig.saveGenderInfo(aPlr);
            }
        }));

        btnHideInArmor = this.addRenderableWidget(new WildfireButton(xPos, yPos + 20, 157, 20,
              WildfireLang.CHAR_SETTING_HIDE_IN_ARMOR.translate(!aPlr.showBreastsInArmor()), button -> {
            boolean enableShowInArmor = !aPlr.showBreastsInArmor();
            if (aPlr.updateShowBreastsInArmor(enableShowInArmor)) {
                button.setMessage(WildfireLang.CHAR_SETTING_HIDE_IN_ARMOR.translate(!enableShowInArmor));
                PlayerConfig.saveGenderInfo(aPlr);
            }
        }, Tooltip.create(WildfireLang.TOOLTIP_HIDE_IN_ARMOR.translate())));

        this.btnOverrideArmorPhys = addRenderableWidget(new WildfireButton(xPos, yPos + 40, 157, 20,
              WildfireLang.CHAR_SETTING_OVERRIDE_PHYSICS.translate(aPlr.getArmorPhysicsOverride()), button -> {
            boolean enableArmorPhysicsOverride = !aPlr.getArmorPhysicsOverride();
            if (aPlr.updateArmorPhysicsOverride(enableArmorPhysicsOverride)) {
                button.setMessage(WildfireLang.CHAR_SETTING_OVERRIDE_PHYSICS.translate(enableArmorPhysicsOverride));
                PlayerConfig.saveGenderInfo(aPlr);
            }
        }, Tooltip.create(TextComponentUtil.build(WildfireLang.TOOLTIP_OVERRIDE_PHYSICS_1, "\n\n", WildfireLang.TOOLTIP_OVERRIDE_PHYSICS_2))));
        this.btnOverrideArmorPhys.active = aPlr.hasBreastPhysics();

        this.bounceSlider = this.addRenderableWidget(new WildfireSlider(xPos, yPos + 60, 157, 20, ClientConfiguration.BOUNCE_MULTIPLIER, aPlr.getBounceMultiplier(), value -> {
        }, value -> {
            int v = Math.round(value * 300);
            bounceWarning = v > 100;
            return WildfireLang.SLIDER_BOUNCE.translate(v);
        }, value -> {
            if (aPlr.updateBounceMultiplier(value)) {
                PlayerConfig.saveGenderInfo(aPlr);
            }
        }));
        this.bounceSlider.active = aPlr.hasBreastPhysics();
        this.bounceSlider.setArrowKeyStep(0.005);

        this.floppySlider = this.addRenderableWidget(new WildfireSlider(xPos, yPos + 80, 157, 20, ClientConfiguration.FLOPPY_MULTIPLIER, aPlr.getFloppiness(), value -> {
        }, value -> WildfireLang.SLIDER_FLOPPY.translate(Math.round(value * 100)), value -> {
            if (aPlr.updateFloppiness(value)) {
                PlayerConfig.saveGenderInfo(aPlr);
            }
        }));
        this.floppySlider.active = aPlr.hasBreastPhysics();
        this.floppySlider.setArrowKeyStep(0.01);

        addRenderableWidget(new WildfireButton(xPos, yPos + 100, 157, 20,
              WildfireLang.CHAR_SETTING_HURT_SOUNDS.translate(aPlr.hasHurtSounds()), button -> {
            boolean enableHurtSounds = !aPlr.hasHurtSounds();
            if (aPlr.updateHurtSounds(enableHurtSounds)) {
                voicePitchSlider.active = aPlr.hasHurtSounds();
                button.setMessage(WildfireLang.CHAR_SETTING_HURT_SOUNDS.translate(enableHurtSounds));
                PlayerConfig.saveGenderInfo(aPlr);
            }
        }, Tooltip.create(WildfireLang.TOOLTIP_HURT_SOUNDS.translate())));

        this.voicePitchSlider = addRenderableWidget(new WildfireSlider(xPos, yPos + 120, 158, 20, ClientConfiguration.VOICE_PITCH, aPlr.getVoicePitch(), value -> {
        }, value -> WildfireLang.SLIDER_VOICE_PITCH.translate(Math.round(value * 100)), value -> {
            if (aPlr.updateVoicePitch(value)) {
                PlayerConfig.saveGenderInfo(aPlr);
                Player player = minecraft.player;
                SoundEvent hurtSound = aPlr.getGender().getHurtSound();
                if (player != null && hurtSound != null) {
                    float pitch = (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F /*+ 1.0F*/; // +1 is from getVoicePitch()
                    player.playSound(hurtSound, 1f, pitch + aPlr.getVoicePitch());
                }
            }
        }));
        voicePitchSlider.active = aPlr.hasHurtSounds();
        this.voicePitchSlider.setArrowKeyStep(0.01);

        addRenderableWidget(new WildfireButton(this.width / 2 + 73, yPos - 11, 9, 9, TextComponentUtil.getString("X"),
              button -> onClose(), text -> AbstractWidget.wrapDefaultNarrationMessage(CommonComponents.GUI_DONE)));

        super.init();
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(BACKGROUND, (this.width - 172) / 2, (this.height - 124) / 2, 0, 0, 172, 164, 256, 256);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        int x = this.width / 2;
        int y = this.height / 2;
        int yPos = y - 47;

        graphics.drawString(this.font, title, x - 79, yPos - 10, 4473924, false);

        if (minecraft != null && minecraft.level != null) {
            Player plrEntity = minecraft.level.getPlayerByUUID(this.playerUUID);
            if (plrEntity != null) {
                graphics.drawCenteredString(this.font, plrEntity.getDisplayName(), x, yPos - 30, 0xFFFFFF);
            }
        }

        if (bounceWarning) {
            graphics.drawCenteredString(font, WildfireLang.TOOLTIP_BOUNCE_WARNING.translateColored(ChatFormatting.ITALIC), x, y + 90, 0xFF6666);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        //Ensure all sliders are saved
        bounceSlider.save();
        floppySlider.save();
        return super.mouseReleased(mouseX, mouseY, state);
    }
}
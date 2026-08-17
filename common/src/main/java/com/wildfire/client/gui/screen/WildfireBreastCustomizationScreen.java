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

import com.wildfire.client.gui.WildfireSlider;
import com.wildfire.common.WildfireGender;
import com.wildfire.client.config.ClientConfig;
import com.wildfire.common.WildfireLang;
import com.wildfire.common.config.GenderConfigTranslations;
import com.wildfire.common.config.value.ConfigValue;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;
import java.util.UUID;

/// @apiNote Only use this on the client side
public class WildfireBreastCustomizationScreen extends BaseWildfireScreen {

    private static final int FULL_WIDTH = 166;
    private static final int HALF_WIDTH = FULL_WIDTH / 2 - 2;

    //~ if >=26.2 'net.minecraft.ChatFormatting' -> 'TextColor' {
    private static final Component ENABLED = WildfireLang.LABEL_ENABLED.translateColored(TextColor.GREEN);
    private static final Component DISABLED = WildfireLang.LABEL_DISABLED.translateColored(TextColor.RED);
    //~}

    private static final Identifier BACKGROUND_FEMALE = WildfireGender.id("textures/gui/breast_customization.png");
    private static final Identifier BACKGROUND_OTHER = WildfireGender.id("textures/gui/breast_customization_other.png");

    private static final Identifier BACKGROUND_CUSTOMIZATION = WildfireGender.id("textures/gui/tabs/breast_customization_tab.png");
    private static final Identifier BACKGROUND_PHYSICS = WildfireGender.id("textures/gui/tabs/breast_physics_tab.png");
    private static final Identifier BACKGROUND_MISC = WildfireGender.id("textures/gui/tabs/miscellaneous_tab.png");

    private Tab currentTab = Tab.CUSTOMIZATION;

    public WildfireBreastCustomizationScreen(Screen parent, UUID uuid) {
        super(WildfireLang.APPEARANCE_SETTINGS_TITLE.translate(), parent, uuid);
    }

    @Override
    public void init() {
        super.init();
        int y = this.height / 2 - 11;

        addButton(builder -> builder
                .message(WildfireLang.CUSTOMIZATION_TAB_CUSTOMIZATION::translate)
                .position(this.width / 2 - 130, y - 52)
                .size(172/2 - 2, 12)
                .onPress(_ -> {
                    currentTab = Tab.CUSTOMIZATION;
                    rebuildWidgets();
                })
                .active(currentTab != Tab.CUSTOMIZATION));

        addButton(builder -> builder
                .message(WildfireLang.CUSTOMIZATION_TAB_PHYSICS::translate)
                .position(this.width / 2 - 42, y - 52)
                .size(172/2 - 2, 12)
                .onPress(_ -> {
                    currentTab = Tab.PHYSICS;
                    rebuildWidgets();
                })
                .active(currentTab != Tab.PHYSICS));

        addButton(builder -> builder
                .message(WildfireLang.CUSTOMIZATION_TAB_MISC::translate)
                .position(this.width / 2 + 46, y - 52)
                .size(172/2 - 2, 12)
                .onPress(_ -> {
                    currentTab = Tab.MISC;
                    rebuildWidgets();
                })
                .active(currentTab != Tab.MISC));

        final int tabOffsetY = y - 3 - 21;
        switch(currentTab) {
            case CUSTOMIZATION -> initCustomizationTab(tabOffsetY);
            case PHYSICS -> initPhysicsTab(tabOffsetY);
            case MISC -> initMiscTab(tabOffsetY);
        }

        disableJump();
    }

    @Override
    public void removed() {
        disableJump();
    }

    private boolean disableJump() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.autoJumpTime > 0) {
            player.autoJumpTime = 0;
            return true;
        }
        return false;
    }

    private void initCustomizationTab(final int tabOffsetY) {
        final var plr = Objects.requireNonNull(getPlayer(), "getPlayer()");

        addSlider(builder -> builder
                .message(value -> WildfireLang.WARDROBE_SLIDER_BREAST_SIZE.translate(Math.round(value * 1.25f * 100)))
                .position(this.width / 2 - 36, tabOffsetY - 2)
                .size(FULL_WIDTH, 20)
                .forConfig(() -> plr.breasts().bustSize())
                .step(0.01)
                .mouseStep(0.001));

        addSlider(builder -> builder
                .message(value -> WildfireLang.WARDROBE_SLIDER_SEPARATION.translate(Math.round((Math.round(value * 100f) / 100f) * 10)))
                .position(this.width / 2 - 36, tabOffsetY + 22)
                .size(HALF_WIDTH, 20)
                .forConfig(() -> plr.breasts().xOffset())
                .mouseStep(0.05));

        addSlider(builder -> builder
                .message(value -> WildfireLang.WARDROBE_SLIDER_HEIGHT.translate(Math.round((Math.round(value * 100f) / 100f) * 10)))
                .position(this.width / 2 - 36 + HALF_WIDTH + 4, tabOffsetY + 22)
                .size(HALF_WIDTH, 20)
                .forConfig(() -> plr.breasts().yOffset())
                .mouseStep(0.05));

        addSlider(builder -> builder
                .message(value -> WildfireLang.WARDROBE_SLIDER_DEPTH.translate(Math.round((Math.round(value * 100f) / 100f) * 10)))
                .position(this.width / 2 - 36, tabOffsetY + 46)
                .size(HALF_WIDTH, 20)
                .forConfig(() -> plr.breasts().zOffset())
                .step(0.1)
                .mouseStep(0.05));
        addSlider(builder -> builder
                .message(value -> WildfireLang.WARDROBE_SLIDER_ROTATION.translate(Math.round((Math.round(value * 100f) / 100f) * 100)))
                .position(this.width / 2 - 36 + HALF_WIDTH + 4, tabOffsetY + 46)
                .size(HALF_WIDTH, 20)
                .forConfig(() -> plr.breasts().cleavage())
                .step(0.1)
                .mouseStep(0.1));


        addButton(builder -> builder
                .message(WildfireLang.UV_EDITOR::translate)
                .position(this.width / 2 - 36, this.height / 2 + 43)
                .size(120, 15)
                .onPress(_ -> {
                    //~ if >=26.2 'minecraft.setScreen' -> 'minecraft.gui.setScreen'
                    minecraft.gui.setScreen(new WildfireBreastUVEditorScreen(this, playerUUID));
                }));
    }

    private void initPhysicsTab(final int tabOffsetY) {
        final var plr = Objects.requireNonNull(getPlayer(), "getPlayer()");
        final var ref = new Object() {
            @UnknownNullability
            AbstractWidget bounceSlider, floppySlider, overridePhysics, dualPhysics;
        };

        addButton(builder -> builder
                .message(WildfireLang.CHAR_SETTINGS_JUMP::translate)
                .position(this.width / 2 - 130, this.height / 2 + 65)
                .size(80, 15)
                .onPress(button -> {
                    //Note: We use the auto jump system rather than setting the jump key as that doesn't work in UIs for neo
                    if (disableJump()) {
                        button.setMessage(WildfireLang.CHAR_SETTINGS_JUMP.translate());
                    } else {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player != null) {
                            //Just set it to jump for a minecraft day, that should be more than enough time for them to configure their settings
                            // and if not they can just re-enable jumping
                            player.autoJumpTime = SharedConstants.TICKS_PER_GAME_DAY;
                            button.setMessage(WildfireLang.CHAR_SETTINGS_JUMPING.translate());
                        }
                    }
                }));

        addButton(builder -> builder
                .message(() -> WildfireLang.CHAR_SETTINGS_PHYSICS.translate(plr.breasts().physics().enabled().get() ? ENABLED : DISABLED))
                .position(this.width / 2 - 36, tabOffsetY - 2)
                .size(FULL_WIDTH, 20)
                .onPress(button -> {
                    if (plr.breasts().physics().enabled().update(ConfigValue.TOGGLE)) {
                        plr.save();
                        button.updateMessage();
                        boolean breastPhysics = plr.breasts().physics().enabled().get();
                        ref.bounceSlider.active = breastPhysics;
                        ref.floppySlider.active = breastPhysics;
                        ref.overridePhysics.active = breastPhysics;
                        ref.dualPhysics.active = breastPhysics;
                    }
                }));

        ref.dualPhysics = addButton(builder -> builder
                .message(() -> WildfireLang.CUSTOMIZATION_DUAL_PHYSICS.translate(plr.breasts().physics().uniboob().get() ? CommonComponents.GUI_NO : CommonComponents.GUI_YES))
                .position(this.width / 2 - 36, tabOffsetY + 22)
                .size(FULL_WIDTH, 20)
                .onPress(button -> {
                    if (plr.breasts().physics().uniboob().update(ConfigValue.TOGGLE)) {
                        plr.save();
                        button.updateMessage();
                    }
                })
                .active(plr.breasts().physics().enabled()));

        ref.overridePhysics = addButton(builder -> builder
                .message(() -> WildfireLang.CHAR_SETTINGS_OVERRIDE_PHYSICS.translate(ClientConfig.config().overrides().armorPhysics().get() ? ENABLED : DISABLED))
                .position(this.width / 2 - 36, tabOffsetY + 70)
                .size(FULL_WIDTH, 20)
                .onPress(button -> {
                    if (ClientConfig.config().overrides().armorPhysics().update(ConfigValue.TOGGLE)) {
                        ClientConfig.INSTANCE.save();
                        button.updateMessage();
                    }
                })
                .tooltip(Tooltip.create(GenderConfigTranslations.CLIENT_OVERRIDE_ARMOR_PHYSICS.tooltipComponent()))
                .active(plr.breasts().physics().enabled()));

        ref.bounceSlider = addSlider(builder -> builder
                .message(value -> WildfireLang.WARDROBE_SLIDER_BOUNCE.translate(Math.round(3 * value * 100)))
                .position(this.width / 2 - 36, tabOffsetY + 46)
                .size(HALF_WIDTH, 20)
                .forConfig(() -> plr.breasts().physics().bounceMultiplier())
                .step(0.005)
                .active(plr.breasts().physics().enabled()));

        ref.floppySlider = addSlider(builder -> builder
                .message(value -> WildfireLang.WARDROBE_SLIDER_FLOPPY.translate(Math.round(value * 100)))
                .position(this.width / 2 - 36 + HALF_WIDTH + 2, tabOffsetY + 46)
                .size(HALF_WIDTH, 20)
                .forConfig(() -> plr.breasts().physics().floppiness())
                .step(0.01)
                .active(plr.breasts().physics().enabled()));
    }

    private void initMiscTab(final int tabOffsetY) {
        final var plr = Objects.requireNonNull(getPlayer(), "getPlayer()");
        final var ref = new Object() {
            @UnknownNullability
            AbstractWidget pitchSlider;
        };

        addButton(builder -> builder
                .message(() -> WildfireLang.CHAR_SETTINGS_HURT_SOUNDS.translate(plr.sounds().hurt().get() ? ENABLED : DISABLED))
                .position(this.width / 2 - 36, tabOffsetY - 2)
                .size(FULL_WIDTH, 20)
                .onPress(button -> {
                    if (plr.sounds().hurt().update(ConfigValue.TOGGLE)) {
                        plr.save();
                        ref.pitchSlider.active = plr.sounds().hurt().get();
                        button.updateMessage();
                    }
                })
                .tooltip(Tooltip.create(WildfireLang.CHAR_SETTINGS_HURT_SOUNDS_TOOLTIP.translate())));

        ref.pitchSlider = addSlider(builder -> builder
                .message(value -> WildfireLang.WARDROBE_SLIDER_PITCH.translate(Math.round(value * 100)))
                .position(this.width / 2 - 36, tabOffsetY + 22)
                .size(HALF_WIDTH, 20)
                .forConfig(() -> plr.sounds().voicePitch())
                .save(_ -> {
                    plr.save();
                    var clientPlayer = Objects.requireNonNull(minecraft).player;
                    if(clientPlayer != null) {
                        plr.tryPlayHurtSound(clientPlayer);
                    }
                })
                .step(0.01)
                .active(plr.sounds().hurt()));

        addButton(builder -> builder
                .message(() -> WildfireLang.CHAR_SETTINGS_HIDE_IN_ARMOR.translate(plr.showBreastsInArmor().get() ? DISABLED : ENABLED))
                .position(this.width / 2 - 36, tabOffsetY + 46)
                .size(FULL_WIDTH, 20)
                .onPress(button -> {
                    if (plr.showBreastsInArmor().update(ConfigValue.TOGGLE)) {
                        plr.save();
                        button.updateMessage();
                    }
                }));

        addButton(builder -> builder
                .message(() -> WildfireLang.CHAR_SETTINGS_ARMOR_STAT.translate(ClientConfig.config().armorStat().get() ? ENABLED : DISABLED))
                .position(this.width / 2 - 36, tabOffsetY + 70)
                .size(FULL_WIDTH, 20)
                .onPress(button -> {
                    if (ClientConfig.config().armorStat().update(ConfigValue.TOGGLE)) {
                        ClientConfig.INSTANCE.save();
                        button.updateMessage();
                    }
                }));

        addButton(builder -> builder
                .message(() -> WildfireLang.HOLIDAY_THEMES.translate(plr.holidayThemes().get() ? ENABLED : DISABLED))
                .position(this.width / 2 - 36, tabOffsetY + 94)
                .size(FULL_WIDTH, 20)
                .onPress(button -> {
                    if (plr.holidayThemes().update(ConfigValue.TOGGLE)) {
                        plr.save();
                        button.updateMessage();
                    }
                })
                .tooltip(Tooltip.create(WildfireLang.HOLIDAY_THEMES_TOOLTIP.line(1))));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractTransparentBackground(graphics);

        var plr = getPlayer();
        if(plr == null) return;
        Identifier backgroundTexture = switch(plr.gender().get()) {
            case FEMALE -> BACKGROUND_FEMALE;
            case OTHER -> BACKGROUND_OTHER;
            default -> null;
        };

        if(backgroundTexture != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, (this.width - 272) / 2, (this.height - 138) / 2, 0, 0, 272, 130, 512, 512);
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, currentTab.background, this.width / 2 - 42, this.height / 2 - 43, 0, 0, 178, currentTab.backgroundHeight, 512, 512);
        drawScrollingString(graphics, getTitle(), 0, (height / 2) - 82, TextAlignment.CENTER, CommonColors.WHITE, graphics.guiWidth(), 5, false);

        renderPlayerInFrame(graphics, this.width / 2 - 90, this.height / 2 + 44, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent arg) {
        //Ensure all sliders are saved
        children().forEach(child -> {
            if(child instanceof WildfireSlider slider) {
                slider.save();
            }
        });
        return super.mouseReleased(arg);
    }

    /*@Override
    public boolean keyPressed(KeyInput input) {
        if(currentTab == Tab.PHYSICS) {
            if (input.getKeycode() == MinecraftClient.getInstance().options.jumpKey.getDefaultKey().getCode()) {
                MinecraftClient.getInstance().options.jumpKey.setPressed(true);
            }
        }
        return super.keyPressed(input);
    }*/

    private enum Tab {
        CUSTOMIZATION(BACKGROUND_CUSTOMIZATION, 80),
        PHYSICS(BACKGROUND_PHYSICS, 104),
        MISC(BACKGROUND_MISC, 128),
        ;

        final Identifier background;
        final int backgroundHeight;

        Tab(Identifier background, int backgroundHeight) {
            this.background = background;
            this.backgroundHeight = backgroundHeight;
        }
    }
}

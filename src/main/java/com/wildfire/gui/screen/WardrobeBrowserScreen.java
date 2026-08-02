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

import com.wildfire.gui.SyncedPlayerList;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLang;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.contributors.Contributors;
import com.wildfire.main.entitydata.PlayerConfig;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.scores.PlayerTeam;

import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WardrobeBrowserScreen extends BaseWildfireScreen {
    private static final Identifier BACKGROUND_MALE = WildfireGender.id("textures/gui/wardrobe_bg_male.png");
    private static final Identifier BACKGROUND_FEMALE = WildfireGender.id("textures/gui/wardrobe_bg_female.png");
    private static final Identifier BACKGROUND_OTHER = WildfireGender.id("textures/gui/wardrobe_bg_other.png");

    private static final Identifier TXTR_RIBBON = WildfireGender.id("bc_ribbon");
    private static final Identifier CLOUD_ICON = WildfireGender.id("cloud");

    private static final boolean isBreastCancerAwarenessMonth = Month.from(ZonedDateTime.now()) == Month.OCTOBER;

    private final WidgetTooltipHolder contribTooltip = new WidgetTooltipHolder();

    public WardrobeBrowserScreen(@Nullable Screen parent, UUID uuid) {
        super(WildfireLang.WARDROBE_TITLE.translate(), parent, uuid);
    }

    public static BaseWildfireScreen create(LocalPlayer player, @Nullable Screen parent) {
        if(ClientConfig.INSTANCE.get(ClientConfig.FIRST_TIME_LOAD) && CloudSync.isAvailable()) {
            return new WildfireFirstTimeSetupScreen(parent, player.getUUID());
        } else {
            return new WardrobeBrowserScreen(parent, player.getUUID());
        }
    }

    public static void open(Minecraft client, LocalPlayer player) {
        //~ if >=26.2 'setScreen' -> 'gui.setScreen'
        client.gui.setScreen(create(player, null));
    }

    @Override
    public void init() {
        super.init();
        final var client = Objects.requireNonNull(this.minecraft, "client");
        int y = this.height / 2;
        PlayerConfig plr = Objects.requireNonNull(getPlayer(), "getPlayer()");

        addButton(builder -> builder
                .message(() -> WildfireLang.PLAYER_LIST_MODE.translate(ClientConfig.INSTANCE.get(ClientConfig.ALWAYS_SHOW_LIST).text()))
                .tooltip(ClientConfig.INSTANCE.get(ClientConfig.ALWAYS_SHOW_LIST).tooltip())
                .position(126, 4)
                .size(185, 10)
                .onPress(button -> {
                    var config = ClientConfig.INSTANCE;
                    var newVal = config.get(ClientConfig.ALWAYS_SHOW_LIST).next();
                    config.set(ClientConfig.ALWAYS_SHOW_LIST, newVal);
                    config.save();
                    button.updateMessage();
                    button.setTooltip(newVal.tooltip());
                }));

        addButton(builder -> builder
                .message(() -> plr.getGender().getDisplayName())
                .position(this.width / 2 - 130, this.height / 2 + 33)
                .size(80, 15)
                .onPress(_ -> {
                    plr.updateGender(plr.getGender().next());
                    plr.save();
                    rebuildWidgets();
                }));

        addButton(builder -> builder
                .message(() -> WildfireLang.GENERIC_ELLIPSIS_SUFFIX.translate(WildfireLang.APPEARANCE_SETTINGS_TITLE.translate()))
                .position(this.width / 2 - 36, this.height / 2 - 63)
                .size(157, 20)
                .onPress(_ -> {
                    //~ if >=26.2 'setScreen' -> 'gui.setScreen'
                    client.gui.setScreen(new WildfireBreastCustomizationScreen(this, this.playerUUID));
                })
                .active(plr.getGender().canHaveBreasts()));

        addButton(builder -> {
            builder.message(WildfireLang.CLOUD_SETTINGS::translate);
            builder.position(this.width / 2 - 36, y + 30);
            builder.size(24, 18);
            builder.renderer((button, ctx, _, _, _) ->
                ctx.blitSprite(RenderPipelines.GUI_TEXTURED, CLOUD_ICON, button.getX() + 2, button.getY() + 2, 20, 14)
            );
            builder.onPress(_ -> {
                //~ if >=26.2 'setScreen' -> 'gui.setScreen'
                client.gui.setScreen(new WildfireCloudSyncScreen(this, this.playerUUID));
            });
            var cloudUnavailable = CloudSync.unavailableReason();
            if(cloudUnavailable != null) {
                builder.tooltip(Tooltip.create(cloudUnavailable.text()));
                builder.active(false);
            } else {
                builder.tooltip(Tooltip.create(WildfireLang.CLOUD_TOOLTIP.translate()));
            }
        });

        addButton(builder -> builder
                .message(() -> WildfireLang.GENERIC_ELLIPSIS_SUFFIX.translate(WildfireLang.CREDITS_TITLE.translate()))
                .position(this.width / 2 + 2, this.height / 2 + 33)
                .size(78, 15)
                .onPress(_ -> {
                    //~ if >=26.2 'setScreen' -> 'gui.setScreen'
                    client.gui.setScreen(new WildfireCreditsScreen(this, this.playerUUID));
                }));

        /*this.addDrawableChild(new WildfireButton(this.width / 2 + 111, y - 63, 9, 9, Text.literal("X"),
            button -> close(), text -> GuiUtils.doneNarrationText()));*/
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractTransparentBackground(graphics);

        PlayerConfig plr = getPlayer();
        if(plr == null) return;
        Identifier backgroundTexture = switch(plr.getGender()) {
            case MALE -> BACKGROUND_MALE;
            case FEMALE -> BACKGROUND_FEMALE;
            case OTHER -> BACKGROUND_OTHER;
        };

        graphics.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, (this.width - 272) / 2, (this.height - 138) / 2, 0, 0, 268, 124, 512, 512);

        renderPlayerInFrame(graphics, this.width / 2 - 90, this.height / 2 + 18, mouseX, mouseY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int x = this.width / 2;
        int y = this.height / 2;
        drawScrollingString(graphics, getTitle(), 0, y - 82, TextAlignment.CENTER, CommonColors.WHITE, graphics.guiWidth(), 5, false);

        drawCreatorContributorText(graphics, mouseX, mouseY, y + 65 + (isBreastCancerAwarenessMonth ? 30 : 0));

        if(isBreastCancerAwarenessMonth) {
            int bcaY = y - 45;
            graphics.fill(x - 159, bcaY + 106, x + 159, bcaY + 136, ARGB.black(0x55));
            drawScrollingString(graphics, WildfireLang.CANCER_AWARENESS_TITLE.translate().withStyle(style -> style.withBold(true).withItalic(true)),
                x - 153, bcaY + 117, TextAlignment.LEFT, CommonColors.WHITE, 283, 5, false);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TXTR_RIBBON, x + 130, bcaY + 109, 26, 26);
        }

        SyncedPlayerList.drawSyncedPlayers(this, graphics, 126, graphics.guiWidth());
    }

    private void drawCreatorContributorText(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int creatorY) {
        final var client = Objects.requireNonNull(this.minecraft);
        if(client.player == null || client.level == null) return;
        Map<UUID, PlayerInfo> entries = client.player.connection.getOnlinePlayers()
                .stream().collect(Collectors.toMap(entry -> entry.getProfile().id(), Function.identity()));

        final boolean withCreator = entries.containsKey(Contributors.CREATOR_UUID);
        final var foundContributors = Contributors.getContributorUUIDs().stream()
                .filter(it -> !it.equals(Contributors.CREATOR_UUID))
                .map(entries::get)
                .filter(Objects::nonNull)
                .toList();

        if(!withCreator && foundContributors.isEmpty()) {
            return;
        }

        final Component text;
        final var toList = new ArrayList<>(foundContributors);
        if(withCreator && !foundContributors.isEmpty()) {
            text = WildfireLang.LABEL_WITH_BOTH.translate();
            toList.addFirst(entries.get(Contributors.CREATOR_UUID));
        } else if(withCreator) {
            text = WildfireLang.LABEL_WITH_CREATOR.translate();
        } else {
            text = WildfireLang.LABEL_WITH_CONTRIBUTOR.translate();
        }

        int textWidth = font.width(text);
        drawCenteredTextWrapped(graphics, text, this.width / 2, creatorY, 300, 0xFFFF00FF);

        // Render a tooltip with the relevant player names when hovered over
        int lines = (int) Math.ceil(textWidth / 300.0);
        if(!toList.isEmpty()
                && mouseX > this.width / 2 - textWidth / 2 && mouseX < this.width / 2 + textWidth / 2
                && mouseY > creatorY - 2 && mouseY < creatorY + (9 * lines)) {
            var contributorNames = toList.stream()
                    .map(entry -> PlayerTeam.formatNameForTeam(entry.getTeam(), Component.nullToEmpty(entry.getProfile().name())))
                    .toList();

            contribTooltip.set(Tooltip.create(ComponentUtils.formatList(contributorNames, Component.literal("\n"))));
            contribTooltip.refreshTooltipForNextRenderPass(graphics, mouseX, mouseY, true, true, ScreenRectangle.empty());
        }
    }
}

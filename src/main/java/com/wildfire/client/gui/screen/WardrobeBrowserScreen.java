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

import com.wildfire.client.WildfireGenderClient;
import com.wildfire.client.gui.GuiHelper;
import com.wildfire.client.gui.WildfireButton;
import com.wildfire.main.Gender;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLang;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.cloud.SyncUnavailable;
import com.wildfire.main.config.GeneralClientConfig;
import com.wildfire.main.config.enums.ShowPlayerListMode;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.text.ILangEntry;
import com.wildfire.main.text.TextComponentUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;

public class WardrobeBrowserScreen extends BaseWildfireScreen {

    private static final ResourceLocation BACKGROUND_MALE = WildfireGender.rl("textures/gui/wardrobe_bg_male.png");
    private static final ResourceLocation BACKGROUND_FEMALE = WildfireGender.rl("textures/gui/wardrobe_bg_female.png");
    private static final ResourceLocation BACKGROUND_OTHER = WildfireGender.rl("textures/gui/wardrobe_bg_other.png");

    private static final ResourceLocation TXTR_RIBBON = WildfireGender.rl("textures/bc_ribbon.png");
    private static final ResourceLocation CLOUD_ICON = WildfireGender.rl("textures/cloud.png");

    private final boolean isBreastCancerAwarenessMonth = Calendar.getInstance().get(Calendar.MONTH) == Calendar.OCTOBER;
    private final WidgetTooltipHolder contribTooltip = new WidgetTooltipHolder();

    public WardrobeBrowserScreen(Screen parent, UUID uuid) {
        super(WildfireLang.WARDROBE.translate(), parent, uuid);
    }

    @Override
    public void init() {
        if (minecraft == null) {
            return;
        }
        int y = this.height / 2;
        PlayerConfig plr = getPlayer();

        addRenderableWidget(new WildfireButton(126, 4, 185, 10, WildfireLang.PLAYER_LIST_MODE.translate(GeneralClientConfig.INSTANCE.alwaysShowList.get()), button -> {
            ShowPlayerListMode newVal = GeneralClientConfig.INSTANCE.alwaysShowList.get().next();
            GeneralClientConfig.INSTANCE.alwaysShowList.set(newVal);
            GeneralClientConfig.INSTANCE.save();
            button.setMessage(WildfireLang.PLAYER_LIST_MODE.translate(newVal));
            button.setTooltip(newVal.tooltip());
        })).setTooltip(GeneralClientConfig.INSTANCE.alwaysShowList.get().tooltip());

        addRenderableWidget(new WildfireButton(this.width / 2 - 130, y + 33, 80, 15, plr.getGender().getTextComponent(), button -> {
            Gender gender = switch (plr.getGender()) {
                case MALE -> Gender.FEMALE;
                case FEMALE -> Gender.OTHER;
                case OTHER -> Gender.MALE;
            };
            if (plr.updateGender(gender)) {
                button.setMessage(TextComponentUtil.build(WildfireLang.GENDER, " - ", gender));
                PlayerConfig.saveGenderInfo(plr);
                rebuildWidgets();
            }
        }));

        addRenderableWidget(new WildfireButton(this.width / 2 - 36, y - 63, 157, 20, TextComponentUtil.build(WildfireLang.APPEARANCE_SETTINGS, CommonComponents.ELLIPSIS),
              button -> minecraft.setScreen(new WildfireBreastCustomizationScreen(WardrobeBrowserScreen.this, this.playerUUID))
        )).active = plr.getGender().canHaveBreasts();

        WildfireButton cloud = new WildfireButton(this.width / 2 - 36, y + 30, 24, 18, WildfireLang.CLOUD_SETTINGS.translate(),
              button -> minecraft.setScreen(new WildfireCloudSyncScreen(this, this.playerUUID))) {
            @Override
            protected void renderContents(@NotNull GuiGraphics graphics) {
                //TODO - 1.21.4: RenderType::guiTextured,
                graphics.blit(CLOUD_ICON, getX() + 2, getY() + 2, 0, 0, 20, 14, 32, 26, 32, 26);
            }
        };

        SyncUnavailable cloudUnavailable = CloudSync.unavailableReason();
        if (cloudUnavailable != null) {
            cloud.setTooltip(Tooltip.create(cloudUnavailable.getTextComponent()));
            cloud.active = false;
        } else {
            cloud.setTooltip(Tooltip.create(WildfireLang.CLOUD_TOOLTIP.translate()));
        }
        addRenderableWidget(cloud);
        super.init();
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        int x = this.width / 2;
        int y = this.height / 2;
        ResourceLocation backgroundTexture = switch (getPlayer().getGender()) {
            case Gender.MALE -> BACKGROUND_MALE;
            case Gender.FEMALE -> BACKGROUND_FEMALE;
            case Gender.OTHER -> BACKGROUND_OTHER;
        };
        //TODO - 1.21.4: RenderType::guiTextured,
        graphics.blit(backgroundTexture, x - 136, y - 69, 0, 0, 268, 124, 512, 512);

        if (minecraft != null && minecraft.level != null) {
            Player ent = minecraft.level.getPlayerByUUID(this.playerUUID);
            if (ent != null) {
                //TODO - 1.21: Figure out the correct positioning for this
                InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x - 164, y - 80, x, y + 80, 70, 0, mouseX, mouseY, ent);
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = this.width / 2;
        int y = this.height / 2;

        graphics.drawString(font, getTitle(), x - font.width(getTitle()) / 2, y - 82, 0xFFFFFF, false);

        if (isBreastCancerAwarenessMonth) {
            int bcaY = y - 45;
            graphics.fill(x - 159, bcaY + 106, x + 159, bcaY + 136, 0x55000000);
            //TODO - 1.21: Validate this formats the component properly
            graphics.drawString(font, TextComponentUtil.build(ChatFormatting.BOLD, ChatFormatting.ITALIC, WildfireLang.CANCER_AWARENESS), this.width / 2 - 148, bcaY + 117, 0xFFFFFF, false);
            //TODO - 1.21.4: RenderType::guiTextured,
            graphics.blit(TXTR_RIBBON, x + 130, bcaY + 109, 26, 26, 0, 0, 20, 20, 20, 20);
            y += 30;
        }

        drawCreatorContributorText(graphics, mouseX, mouseY, y + 65);

        //Render in front of the UI when it's open.
        GuiHelper.drawSyncedPlayers(graphics, font, WildfireGenderClient.collectPlayerEntries());
    }

    private void drawCreatorContributorText(@NotNull GuiGraphics graphics, int mouseX, int mouseY, int creatorY) {
        if (minecraft == null || minecraft.player == null || minecraft.level == null) {
            return;
        }
        Map<UUID, PlayerInfo> entries = minecraft.player.connection.getOnlinePlayers()
              .stream().collect(Collectors.toMap(entry -> entry.getProfile().getId(), Function.identity()));

        final ILangEntry langEntry;
        final List<PlayerInfo> foundContributors = WildfireGender.CONTRIBUTOR_UUIDS.stream().map(entries::get).filter(Objects::nonNull).collect(Collectors.toList());
        if (entries.containsKey(WildfireGender.CREATOR_UUID)) {
            langEntry = foundContributors.isEmpty() ? WildfireLang.LABEL_WITH_CREATOR : WildfireLang.LABEL_WITH_BOTH;
            foundContributors.addFirst(entries.get(WildfireGender.CREATOR_UUID));
        } else if (!foundContributors.isEmpty()) {
            langEntry = WildfireLang.LABEL_WITH_CONTRIBUTOR;
        } else {
            return;
        }

        Component text = langEntry.translate();
        int textWidth = font.width(text);
        drawCenteredTextWrapped(graphics, text, this.width / 2, creatorY, 300, 0xFF00FF);

        // Render a tooltip with the relevant player names when hovered over
        int lines = (int) Math.ceil(textWidth / 300.0);
        if (mouseX > this.width / 2 - textWidth / 2 && mouseX < this.width / 2 + textWidth / 2
            && mouseY > creatorY - 2 && mouseY < creatorY + (9 * lines)) {
            List<Component> contributorNames = new ArrayList<>();
            for (PlayerInfo entry : foundContributors) {
                contributorNames.add(PlayerTeam.formatNameForTeam(entry.getTeam(), TextComponentUtil.build(entry.getProfile())));
            }

            contribTooltip.set(Tooltip.create(CommonComponents.joinLines(contributorNames)));
            contribTooltip.refreshTooltipForNextRenderPass(true, true, ScreenRectangle.empty());
        }
    }
}
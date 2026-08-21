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

package com.wildfire.client.gui;

import com.wildfire.client.gui.IFancyFontRenderer.TextAlignment;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.WildfireLang;
import com.wildfire.api.Gender;
import com.wildfire.client.contributors.Contributors;
import com.wildfire.common.entitydata.PlayerConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public final class SyncedPlayerList {
    private SyncedPlayerList() {
        throw new UnsupportedOperationException();
    }

    private static int ticks = 0;
    private static volatile List<SyncedPlayer> syncedPlayers = Collections.emptyList();

    private static long TIME_STARTED = -1;
    private static final IFancyFontRenderer FALLBACK_FONT_RENDERER = () -> TIME_STARTED;

    public static void resetTimer() {
        TIME_STARTED = -1;
    }

    public static void drawSyncedPlayers(GuiGraphicsExtractor graphics) {
        if (TIME_STARTED == -1) {
            TIME_STARTED = Util.getMillis();
        }
        int screenWidth = graphics.guiWidth();
        int width = screenWidth;
        //~ if >=26.2 'getTabList' -> 'hud.getTabList'
        if (Minecraft.getInstance().gui.hud.getTabList().visible) {
            //Where it starts drawing, given we start at 0, this works for calculating the width
            int maxLineWidth = getTabOverlayMaxLineWidth(screenWidth);
            if (maxLineWidth > 0) {
                width = screenWidth / 2 - maxLineWidth / 2 - 1;
                //TODO: If we really care, we could calculate the height of the list so that if we are rendering lots of synced players,
                // then we can let later ones scroll further. For now I don't think it is worth it though
            }
        }
        drawSyncedPlayers(FALLBACK_FONT_RENDERER, graphics, width, width);
    }

    public static void drawSyncedPlayers(IFancyFontRenderer fontRenderer, GuiGraphicsExtractor graphics, int titleWidth, int playerWidth) {
        if(syncedPlayers.isEmpty()) {
            return;
        }

        //~ if >=26.2 'net.minecraft.ChatFormatting' -> 'TextColor'
        var header = WildfireLang.WARDROBE_PLAYERS_USING.translateColored(TextColor.AQUA);
        fontRenderer.drawScrollingString(graphics, header, 0, 5, TextAlignment.LEFT, CommonColors.WHITE, titleWidth, 5, false);

        int yPos = 18;
        for(var entry : syncedPlayers) {
            Component text = WildfireLang.GENERIC_DASH_EXPLANATION.translate(entry.coloredName(), entry.gender().getDisplayName());
            fontRenderer.drawScrollingString(graphics, text, 5, yPos, TextAlignment.LEFT, CommonColors.WHITE, playerWidth - 5, 5, false);
            yPos += 10;
        }
    }

    // TODO this design is largely redundant now, as this was designed at a point where it was assumed
    //		that HUD rendering would also receive the same render split treatment as entities did, which
    //		appears to now be incorrect
    public static void onTick(Minecraft minecraft) {
        if(ticks++ % 5 != 0) {
            return;
        }

        var clientPlayer = minecraft.player;
        if(clientPlayer == null) {
            syncedPlayers = Collections.emptyList();
            return;
        }

        var list = new ArrayList<SyncedPlayer>();

        for(var entry : clientPlayer.connection.getListedOnlinePlayers()) {
            if(Objects.equals(entry.getProfile().id(), clientPlayer.getUUID())) {
                continue;
            }

            var config = WildfireGender.getPlayerById(entry.getProfile().id());
            if(config == null || config.syncStatus == PlayerConfigHolder.SyncStatus.UNKNOWN) {
                continue;
            }

            var color = Contributors.getColor(entry.getProfile().id());
            //~ if >=26.2 'fromRgb(0xFFFFFF)' -> 'WHITE'
            list.add(new SyncedPlayer(entry.getProfile().name(), color == null ? TextColor.WHITE : color, config.gender().get()));

            if(list.size() >= 40) {
                break;
            }
        }

        syncedPlayers = list;
    }

    private record SyncedPlayer(String name, TextColor color, Gender gender) {

        public Component coloredName() {
            //~ if >=26.2 'color.getValue()' -> 'color'
            return Component.literal(name).withColor(color);
        }
    }

    /// @implNote Trimmed down logic from [PlayerTabOverlay#extractRenderState]
    private static int getTabOverlayMaxLineWidth(int screenWidth) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return 0;
        }
        //~ if >=26.2 'getTabList' -> 'hud.getTabList'
        PlayerTabOverlay tabList = mc.gui.hud.getTabList();

        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective displayObjective = scoreboard.getDisplayObjective(DisplaySlot.LIST);

        List<PlayerInfo> playerInfos = tabList.getPlayerInfos();
        int spacerWidth = mc.font.width(" ");
        int maxNameWidth = 0;
        int maxScoreWidth = 0;

        for (PlayerInfo info : playerInfos) {
            maxNameWidth = Math.max(maxNameWidth, mc.font.width(tabList.getNameForDisplay(info)));
            int playerScoreWidth = 0;
            if (displayObjective != null && displayObjective.getRenderType() != ObjectiveCriteria.RenderType.HEARTS) {
                ScoreHolder scoreHolder = ScoreHolder.fromGameProfile(info.getProfile());
                ReadOnlyScoreInfo scoreInfo = scoreboard.getPlayerScoreInfo(scoreHolder, displayObjective);
                if (scoreInfo != null) {
                    NumberFormat objectiveDefaultFormat = displayObjective.numberFormatOrDefault(StyledFormat.PLAYER_LIST_DEFAULT);
                    playerScoreWidth = mc.font.width(scoreInfo.formatValue(objectiveDefaultFormat));
                }
                maxScoreWidth = Math.max(maxScoreWidth, playerScoreWidth > 0 ? spacerWidth + playerScoreWidth : 0);
            }
        }

        int slots = playerInfos.size();
        int rows = slots;

        int cols;
        for (cols = 1; rows > PlayerTabOverlay.MAX_ROWS_PER_COL; rows = (slots + cols - 1) / cols) {
            cols++;
        }

        //~ if >=26.2 'mc.isLocalServer() || mc.getConnection().getConnection().isEncrypted()' -> 'mc.getConnection().onlineMode()'
        boolean showHead = mc.getConnection().onlineMode();
        int widthForScore;
        if (displayObjective != null) {
            if (displayObjective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
                widthForScore = 90;
            } else {
                widthForScore = maxScoreWidth;
            }
        } else {
            widthForScore = 0;
        }

        int slotWidth = Math.min(cols * ((showHead ? 9 : 0) + maxNameWidth + widthForScore + 13), screenWidth - 50) / cols;
        int maxLineWidth = slotWidth * cols + (cols - 1) * 5;
        if (tabList.header != null) {
            for (FormattedCharSequence line : mc.font.split(tabList.header, screenWidth - 50)) {
                maxLineWidth = Math.max(maxLineWidth, mc.font.width(line));
            }
        }
        if (tabList.footer != null) {
            for (FormattedCharSequence line : mc.font.split(tabList.footer, screenWidth - 50)) {
                maxLineWidth = Math.max(maxLineWidth, mc.font.width(line));
            }
        }
        return maxLineWidth;
    }
}

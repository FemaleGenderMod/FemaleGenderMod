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

import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.PlayerConfig;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public class GuiHelper {

    private GuiHelper() {
    }

    public static void drawSyncedPlayers(GuiGraphics graphics, Font font, List<PlayerInfo> syncedPlayers) {
        if (syncedPlayers.isEmpty()) {
            return;
        }
        graphics.drawString(font, Component.translatable("wildfire_gender.wardrobe.players_using_mod").withStyle(ChatFormatting.AQUA), 5, 5, 0xFFFFFF);

        int yPos = 18;
        for (PlayerInfo entry : syncedPlayers) {
            PlayerConfig cfg = WildfireGender.getPlayerById(entry.getProfile().getId());
            if (cfg != null) {
                Component text = Component.literal(entry.getProfile().getName()).append(" - ").append(cfg.getGender().getDisplayName());
                graphics.drawString(font, text, 10, yPos, 0xFFFFFF, false);
                yPos += 10;
            }
        }
    }
}
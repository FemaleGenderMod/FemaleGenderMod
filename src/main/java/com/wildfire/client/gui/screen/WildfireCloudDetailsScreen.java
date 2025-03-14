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
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLang;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class WildfireCloudDetailsScreen extends BaseWildfireScreen {

    private static final ResourceLocation BACKGROUND = WildfireGender.rl("textures/gui/details_page.png");

    private WildfireButton nextPage, prevPage;
    private int currentPage = 0;

    public WildfireCloudDetailsScreen(Screen parent, UUID uuid) {
        super(WildfireLang.CLOUD_SETTINGS.translate(), parent, uuid);
    }

    @Override
    public void init() {
        int x = this.width / 2;
        int y = this.height / 2;

        currentPage = 0;
        nextPage = addRenderableWidget(new WildfireButton(x + 46, y + 74, 76, 20, WildfireLang.DETAILS_NEXT_PAGE.translate(), button -> {
            if (currentPage < 1) {
                currentPage++;
            }
        }));

        prevPage = addRenderableWidget(new WildfireButton(x - 128 + 6, y + 74, 76, 20, WildfireLang.DETAILS_PREV_PAGE.translate(), button -> {
            if (currentPage > 0) {
                currentPage--;
            }
        }));

        super.init();
    }


    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(BACKGROUND, (this.width - 256) / 2, (this.height - 200) / 2, 0, 0, 256, 200, 256, 256);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        int x = this.width / 2;
        int y = this.height / 2;

        drawCenteredText(graphics, WildfireLang.CLOUD_DETAILS.translate(), x, y - 94, 0x444444);

        if (currentPage == 0) {
            drawCenteredTextWrapped(graphics, WildfireLang.CLOUD_DETAILS_PAGE1.translate(), x, y - 75, 256 - 10, 0x00FF00);
        }
    }
}
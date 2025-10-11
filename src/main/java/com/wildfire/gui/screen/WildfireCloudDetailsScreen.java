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
import com.wildfire.main.WildfireGender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class WildfireCloudDetailsScreen extends BaseWildfireScreen {

	//TODO: PROPER TRANSLATIONS

	private static final Text TITLE = Text.translatable("wildfire_gender.cloud_details.title");

	private static final Text PAGE_1 = Text.translatable("wildfire_gender.cloud_details.title").formatted(Formatting.UNDERLINE);

	private static final Text NEXT_PAGE = Text.translatable("wildfire_gender.details.next_page");
	private static final Text PREV_PAGE = Text.translatable("wildfire_gender.details.prev_page");
	private static final Identifier BACKGROUND = Identifier.of(WildfireGender.MODID, "textures/gui/details_page.png");

	private int currentPage = 0;

	public WildfireCloudDetailsScreen(Screen parent, UUID uuid) {
		super(Text.translatable("wildfire_gender.cloud_settings"), parent, uuid);
	}

	@Override
	public void init() {
		int x = this.width / 2;
		int y = this.height / 2;

		currentPage = 0;

//		this.addDrawableChild(new WildfireButton(x + 46, y + 74, 76, 20,
//				NEXT_PAGE,
//				button -> {
//					if(currentPage < 1) {
//						currentPage++;
//					}
//				}));
//
//
//		this.addDrawableChild(new WildfireButton(x - 128 + 6, y + 74, 76, 20,
//				PREV_PAGE,
//				button -> {
//					if(currentPage > 0) {
//						currentPage--;
//					}
//				}));
	}


	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(ctx);
		ctx.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND, (this.width - 256) / 2, (this.height - 200) / 2, 0, 0, 256, 200, 256, 256);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		if (client == null || client.world == null) return;
		super.render(ctx, mouseX, mouseY, delta);

		int x = this.width / 2;
		int y = this.height / 2;

		GuiUtils.drawCenteredText(ctx, textRenderer, TITLE, x, y - 94, 4473924);

		if (currentPage == 0) {
			GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.translatable("wildfire_gender.cloud_details.page1"), x, y - 75, 256 - 10, 0x00FF00);
		}
	}

	@Override
	public void close() {
		super.close();
	}
}
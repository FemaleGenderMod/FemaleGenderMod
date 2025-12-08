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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class WildfireCloudDetailsScreen extends BaseWildfireScreen {

	//TODO: PROPER TRANSLATIONS

	private static final Component TITLE = Component.translatable("wildfire_gender.cloud_details.title");

	private static final Component PAGE_1 = Component.translatable("wildfire_gender.cloud_details.title").withStyle(ChatFormatting.UNDERLINE);

	private static final Component NEXT_PAGE = Component.translatable("wildfire_gender.details.next_page");
	private static final Component PREV_PAGE = Component.translatable("wildfire_gender.details.prev_page");
	private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(WildfireGender.MODID, "textures/gui/details_page.png");

	private int currentPage = 0;

	public WildfireCloudDetailsScreen(Screen parent, UUID uuid) {
		super(Component.translatable("wildfire_gender.cloud_settings"), parent, uuid);
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
	public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
		this.renderTransparentBackground(ctx);
		ctx.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, (this.width - 256) / 2, (this.height - 200) / 2, 0, 0, 256, 200, 256, 256);
	}

	@Override
	public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
		if (minecraft == null || minecraft.level == null) return;
		super.render(ctx, mouseX, mouseY, delta);

		int x = this.width / 2;
		int y = this.height / 2;

		GuiUtils.drawCenteredText(ctx, font, TITLE, x, y - 94, 4473924);

		if (currentPage == 0) {
			GuiUtils.drawCenteredTextWrapped(ctx, font, Component.translatable("wildfire_gender.cloud_details.page1"), x, y - 75, 256 - 10, 0x00FF00);
		}
	}

	@Override
	public void onClose() {
		super.onClose();
	}
}
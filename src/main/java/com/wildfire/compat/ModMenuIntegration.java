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

package com.wildfire.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.wildfire.gui.screen.WardrobeBrowserScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			var client = Minecraft.getInstance();
			var player = client.player;
			if(player == null) {
				return new NotInWorldScreen(client, screen);
			}
			return WardrobeBrowserScreen.create(player, screen);
		};
	}

	// TODO it'd be nice to support opening the proper mod ui outside a world (like what show me your skin does),
	//      but doing so requires implementing a fake player entity, which in turn requires bodge implementations
	//      of basic classes like the registry.
	//      so, for now, just make a screen that says this isn't supported.
	private static class NotInWorldScreen extends ConfirmScreen {
		private final Screen parent;

		public NotInWorldScreen(Minecraft client, Screen parent) {
			super(
					result -> client.setScreen(parent),
					Component.translatable("wildfire_gender.not_in_world.title").withStyle(ChatFormatting.RED),
					Component.translatable("wildfire_gender.not_in_world")
			);
			this.parent = parent;
		}

		@Override
		protected void addButtons(LinearLayout layout) {
			layout.addChild(Button.builder(CommonComponents.GUI_OK, (button) -> onClose()).build());
		}

		@Override
		public void onClose() {
			Objects.requireNonNull(minecraft, "client").setScreen(parent);
		}
	}
}

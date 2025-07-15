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

package com.wildfire.main.config.enums;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.function.ValueLists;

import java.util.function.IntFunction;

public enum ShowPlayerListMode {
	MOD_UI_ONLY,
	TAB_LIST_OPEN,
	ALWAYS;

	public static final IntFunction<ShowPlayerListMode> BY_ID = ValueLists.createIndexToValueFunction(ShowPlayerListMode::ordinal, values(), ValueLists.OutOfBoundsHandling.WRAP);

	public ShowPlayerListMode next() {
		return BY_ID.apply(this.ordinal() + 1);
	}

	public boolean isVisible() {
		return switch(this) {
			case MOD_UI_ONLY -> false;
			case TAB_LIST_OPEN -> MinecraftClient.getInstance().options.playerListKey.isPressed();
			case ALWAYS -> true;
		};
	}

	public Text text() {
		return Text.translatable("wildfire_gender.always_show_list." + name().toLowerCase());
	}

	public Tooltip tooltip() {
		if(this == TAB_LIST_OPEN) {
			var button = MinecraftClient.getInstance().options.playerListKey.getBoundKeyLocalizedText();
			return Tooltip.of(Text.translatable("wildfire_gender.always_show_list." + name().toLowerCase() + ".tooltip", button));
		}
		return Tooltip.of(Text.translatable("wildfire_gender.always_show_list." + name().toLowerCase() + ".tooltip"));
	}
}

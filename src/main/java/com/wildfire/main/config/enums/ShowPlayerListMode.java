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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

public enum ShowPlayerListMode {
	MOD_UI_ONLY,
	TAB_LIST_OPEN,
	ALWAYS;

	public static final IntFunction<ShowPlayerListMode> BY_ID = ByIdMap.continuous(ShowPlayerListMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);

	public ShowPlayerListMode next() {
		return BY_ID.apply(this.ordinal() + 1);
	}

	public boolean isVisible() {
		return switch(this) {
			case MOD_UI_ONLY -> false;
			case TAB_LIST_OPEN -> Minecraft.getInstance().options.keyPlayerList.isDown();
			case ALWAYS -> true;
		};
	}

	public Component text() {
		return Component.translatable("wildfire_gender.always_show_list." + name().toLowerCase());
	}

	public Tooltip tooltip() {
		if(this == TAB_LIST_OPEN) {
			var button = Minecraft.getInstance().options.keyPlayerList.getTranslatedKeyMessage();
			return Tooltip.create(Component.translatable("wildfire_gender.always_show_list." + name().toLowerCase() + ".tooltip", button));
		}
		return Tooltip.create(Component.translatable("wildfire_gender.always_show_list." + name().toLowerCase() + ".tooltip"));
	}
}

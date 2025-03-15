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

import com.wildfire.main.WildfireLang;
import com.wildfire.main.text.IHasTextComponent.IHasEnumNameTextComponent;
import com.wildfire.main.text.ILangEntry;
import java.util.function.IntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;
import org.jetbrains.annotations.NotNull;

public enum ShowPlayerListMode implements IHasEnumNameTextComponent {
	MOD_UI_ONLY(WildfireLang.PLAYER_LIST_MODE_MOD_UI, WildfireLang.PLAYER_LIST_MODE_MOD_UI_TOOLTIP),
	TAB_LIST_OPEN(WildfireLang.PLAYER_LIST_MODE_TAB_LIST, WildfireLang.PLAYER_LIST_MODE_TAB_LIST_TOOLTIP),
	ALWAYS(WildfireLang.PLAYER_LIST_MODE_ALWAYS, WildfireLang.PLAYER_LIST_MODE_ALWAYS_TOOLTIP);

	public static final IntFunction<ShowPlayerListMode> BY_ID = ByIdMap.continuous(ShowPlayerListMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);

	private final ILangEntry name;
	private final ILangEntry tooltip;

	ShowPlayerListMode(ILangEntry name, ILangEntry tooltip) {
		this.name = name;
		this.tooltip = tooltip;
	}

	public ShowPlayerListMode next() {
		return BY_ID.apply(this.ordinal() + 1);
	}

	public Tooltip tooltip() {
		if (this == TAB_LIST_OPEN) {
			return Tooltip.create(tooltip.translate(Component.keybind(Minecraft.getInstance().options.keyPlayerList.getName())));
		}
		return Tooltip.create(tooltip.translate());
	}

	@NotNull
	@Override
	public Component getTextComponent() {
		return name.translate();
	}
}

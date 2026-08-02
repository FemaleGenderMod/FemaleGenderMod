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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

public enum ShowPlayerListMode {
    MOD_UI_ONLY(WildfireLang.PLAYER_LIST_MODE_MOD_UI, WildfireLang.PLAYER_LIST_MODE_MOD_UI_TOOLTIP),
    TAB_LIST_OPEN(WildfireLang.PLAYER_LIST_MODE_TAB_LIST, WildfireLang.PLAYER_LIST_MODE_TAB_LIST_TOOLTIP),
    ALWAYS(WildfireLang.PLAYER_LIST_MODE_ALWAYS, WildfireLang.PLAYER_LIST_MODE_ALWAYS_TOOLTIP);

    public static final IntFunction<ShowPlayerListMode> BY_ID = ByIdMap.continuous(ShowPlayerListMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);

    private final WildfireLang name;
    private final WildfireLang tooltip;

    ShowPlayerListMode(WildfireLang name, WildfireLang tooltip) {
        this.name = name;
        this.tooltip = tooltip;
    }

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
        return this.name.translate();
    }

    public Tooltip tooltip() {
        if (this == TAB_LIST_OPEN) {
            return Tooltip.create(tooltip.translate(Minecraft.getInstance().options.keyPlayerList.getTranslatedKeyMessage()));
        }
        return Tooltip.create(tooltip.translate());
    }
}

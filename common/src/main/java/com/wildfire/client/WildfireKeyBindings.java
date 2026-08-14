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

package com.wildfire.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.WildfireHelper;
import com.wildfire.common.WildfireLang;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public interface WildfireKeyBindings {

    Identifier CATEGORY_ID = WildfireGender.id("generic");

    @ApiStatus.Internal
    static KeyMapping createConfigKeyBind(Category category) {
        return new KeyMapping(WildfireLang.KEY_CONFIG.getTranslationKey(), InputConstants.KEY_H, category);
    }

    @ApiStatus.Internal
    static KeyMapping createToggleKeybind(Category category) {
        return new KeyMapping(WildfireLang.KEY_TOGGLE.getTranslationKey(), InputConstants.UNKNOWN.getValue(), category);
    }

    WildfireKeyBindings INSTANCE = WildfireHelper.getService(WildfireKeyBindings.class);

    Category category();

    KeyMapping configKey();

    KeyMapping toggleKey();
}

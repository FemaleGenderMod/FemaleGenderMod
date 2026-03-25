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

package com.wildfire.main;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class WildfireSounds {
    private WildfireSounds() {
        throw new UnsupportedOperationException();
    }

    public static final SoundEvent FEMALE_HURT = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(WildfireGender.MODID, "female_hurt"));

    static void register() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, FEMALE_HURT.location(), FEMALE_HURT);
    }
}

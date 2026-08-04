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

package com.wildfire.main.config.types;

import java.util.Comparator;
import java.util.Objects;

public record ConfigRange<TYPE>(TYPE minInclusive, TYPE maxInclusive, Comparator<? super TYPE> comparator) {

    public boolean validate(TYPE value) {
        return Objects.compare(value, minInclusive, comparator) >= 0 && Objects.compare(value, maxInclusive, comparator) <= 0;
    }
}

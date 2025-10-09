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

package com.wildfire.main.uvs;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class UVLayout {
    private final EnumMap<UVDirection, UVQuad> quads = new EnumMap<>(UVDirection.class);

    public UVLayout(UVQuad east, UVQuad west, UVQuad down, UVQuad up, UVQuad north) {
        quads.put(UVDirection.EAST,  east);
        quads.put(UVDirection.WEST,  west);
        quads.put(UVDirection.DOWN,  down);
        quads.put(UVDirection.UP,    up);
        quads.put(UVDirection.NORTH, north);
    }

    //empty constructor for encode/decode
    public UVLayout() {
        quads.put(UVDirection.EAST,  null);
        quads.put(UVDirection.WEST,  null);
        quads.put(UVDirection.DOWN,  null);
        quads.put(UVDirection.UP,    null);
        quads.put(UVDirection.NORTH, null);
    }

    public void put(UVDirection dir, UVQuad quad) {
        quads.put(dir, quad);
    }

    public UVQuad get(UVDirection dir) {
        return quads.get(dir);
    }

    public boolean has(UVDirection dir) {
        return quads.containsKey(dir);
    }

    public Map<UVDirection, UVQuad> getAllSides() {
        return Collections.unmodifiableMap(quads);
    }

    public UVLayout copy() {
        var copy = new UVLayout();
        copy.quads.putAll(this.quads);
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UVLayout)) return false;

        UVLayout other = (UVLayout) obj;
        return quads.equals(other.quads);
    }
}

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

import com.wildfire.main.WildfireGender;
import net.minecraft.util.math.Direction;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class UVLayout {
    private final EnumMap<Direction, UVQuad> quads = new EnumMap<>(Direction.class);

    public UVLayout(UVQuad east, UVQuad west, UVQuad down, UVQuad up, UVQuad north) {
        quads.put(Direction.EAST,  east);
        quads.put(Direction.WEST,  west);
        quads.put(Direction.DOWN,  down);
        quads.put(Direction.UP,    up);
        quads.put(Direction.NORTH, north);
    }

    public UVLayout() {
    }

    public void put(Direction dir, UVQuad quad) {
        quads.put(dir, quad);
    }

    public UVQuad get(Direction dir) {
        return quads.get(dir);
    }

    public boolean has(Direction dir) {
        return quads.containsKey(dir);
    }

    public Map<Direction, UVQuad> getAllSides() {
        return Collections.unmodifiableMap(quads);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UVLayout other)) return false;

        for (Direction dir : WildfireGender.SERIALIZED_DIRECTIONS) {
            UVQuad q1 = this.get(dir);
            UVQuad q2 = other.get(dir);

            if (q1 == null && q2 == null) continue;
            if (q1 == null || q2 == null) return false;
            if (!q1.equals(q2)) return false;
        }
        return true;
    }
}

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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Int2DArrayConfigKey extends ConfigKey<int[][]> {

    public Int2DArrayConfigKey(String key, int[][] defaultValue) {
        super(key, defaultValue);
    }

    @Override
    protected int[][] read(JsonElement element) {
        if (element != null && element.isJsonArray()) {
            JsonArray outer = element.getAsJsonArray();
            int[][] result = new int[outer.size()][];
            for (int i = 0; i < outer.size(); i++) {
                JsonArray inner = outer.get(i).getAsJsonArray();
                result[i] = new int[inner.size()];
                for (int j = 0; j < inner.size(); j++) {
                    result[i][j] = inner.get(j).getAsInt();
                }
            }
            return result;
        }
        return defaultValue;
    }

    @Override
    public void save(JsonObject object, int[][] value) {
        JsonArray outer = new JsonArray();
        for (int[] inner : value) {
            JsonArray arr = new JsonArray();
            for (int num : inner) {
                arr.add(num);
            }
            outer.add(arr);
        }
        object.add(key, outer);
    }
}

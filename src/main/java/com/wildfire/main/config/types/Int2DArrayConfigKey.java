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
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;

import java.util.Arrays;

public class Int2DArrayConfigKey extends ConfigKey<int[][]> {

    public Int2DArrayConfigKey(String key, int[][] defaultValue) {
        super(key, WildfireHelper.deepClone(defaultValue));
    }

    @Override
    protected int[][] read(JsonElement element) {
        if (element != null && element.isJsonArray()) {
            JsonArray outer = element.getAsJsonArray();
            int[][] result = new int[outer.size()][4]; // always 4 elements per object
            for (int i = 0; i < outer.size(); i++) {
                JsonObject obj = outer.get(i).getAsJsonObject();
                result[i][0] = obj.get("x1").getAsInt();
                result[i][1] = obj.get("y1").getAsInt();
                result[i][2] = obj.get("x2").getAsInt();
                result[i][3] = obj.get("y2").getAsInt();
            }
            return result;
        }
        return defaultValue;
    }


    @Override
    public void save(JsonObject object, int[][] value) {
        JsonArray outer = new JsonArray();
        for (int[] inner : value) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x1", inner[0]);
            obj.addProperty("y1", inner[1]);
            obj.addProperty("x2", inner[2]);
            obj.addProperty("y2", inner[3]);
            outer.add(obj);
        }
        object.add(key, outer);
    }

    @Override
    public int[][] getDefault() {
        return WildfireHelper.deepClone(super.getDefault());
    }
}

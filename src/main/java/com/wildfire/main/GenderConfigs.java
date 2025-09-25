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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GenderConfigs {

    public static final JsonObject DEFAULT_FEMALE;
    public static final JsonObject DEFAULT_MALE;

    static {
        DEFAULT_FEMALE = loadConfig("modeldata/female_default.json");
        DEFAULT_MALE = loadConfig("modeldata/male_default.json");
    }

    private static JsonObject loadConfig(String cfgFile) {
        JsonObject fObj = new JsonObject();

        try {
            ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
            Identifier id = Identifier.of(WildfireGender.MODID, cfgFile);
            Resource resource = manager.getResource(id).orElseThrow();

            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
                for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    fObj.add(entry.getKey(), entry.getValue());
                }
            }
        } catch(IOException e) {
            WildfireGender.LOGGER.error("Failed to load config file", e);
        }

        return fObj;
    }
}

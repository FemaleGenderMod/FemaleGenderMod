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
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class GenderConfigs {

	private static final Gson GSON = new Gson();

	public static final JsonObject DEFAULT_FEMALE;
	public static final JsonObject DEFAULT_MALE;

	static {
		DEFAULT_FEMALE = loadConfig("modeldata/female_default.json");
		DEFAULT_MALE = loadConfig("modeldata/male_default.json");
	}

	private static JsonObject loadConfig(String cfgFile) {
		try {
			ResourceManager manager = Minecraft.getInstance().getResourceManager();
			Identifier id = WildfireGender.id(cfgFile);
			Resource resource = manager.getResource(id).orElseThrow();

			try(var reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
				return GSON.fromJson(reader, JsonObject.class);
			}
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}

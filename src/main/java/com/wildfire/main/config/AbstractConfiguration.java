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

package com.wildfire.main.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.types.BooleanConfigKey;
import com.wildfire.main.config.types.ConfigKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public abstract class AbstractConfiguration {

	private static final TypeAdapter<JsonObject> ADAPTER = new Gson().getAdapter(JsonObject.class);

	private final File cfgFile;
	private final JsonObject saveValues = new JsonObject();

	protected AbstractConfiguration(String directory, String cfgName) {
		Path saveDir = FabricLoader.getInstance().getConfigDir().resolve(directory);
		if(supportsSaving() && !Files.isDirectory(saveDir)) {
			try {
				Files.createDirectory(saveDir);
			} catch(IOException e) {
				WildfireGender.LOGGER.error("Failed to create config directory", e);
			}
		}
		cfgFile = saveDir.resolve(cfgName + ".json").toFile();
	}

	public static boolean supportsSaving() {
		return FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER;
	}

	public <TYPE> void set(ConfigKey<TYPE> key, TYPE value) {
		key.save(saveValues, value);
	}

	public <TYPE> TYPE get(ConfigKey<TYPE> key) {
		return key.read(saveValues);
	}

	public boolean toggle(BooleanConfigKey key) {
		var newValue = !get(key);
		set(key, newValue);
		return newValue;
	}

	@ApiStatus.Internal
	public @Nullable JsonElement get(String key) {
		return saveValues.get(key);
	}

	@ApiStatus.Internal
	public void set(String key, JsonElement element) {
		saveValues.add(key, element);
	}

	public <TYPE> void setDefault(ConfigKey<TYPE> key) {
		if(!saveValues.has(key.getKey())) {
			set(key, key.getDefault());
		}
	}

	public void removeParameter(ConfigKey<?> key) {
		removeParameter(key.getKey());
	}

	public void removeParameter(String key) {
		saveValues.remove(key);
	}

	public boolean exists() {
		return cfgFile.exists();
	}

	public void save() {
		if(!supportsSaving()) return;
		try(FileWriter writer = new FileWriter(cfgFile); JsonWriter jsonWriter = new JsonWriter(writer)) {
			jsonWriter.setIndent("\t");
			ADAPTER.write(jsonWriter, saveValues);
		} catch (IOException e) {
			WildfireGender.LOGGER.error("Failed to save config file", e);
		}
	}

	public void load() {
		if(!supportsSaving() || !cfgFile.exists()) return;
		try(FileReader configurationFile = new FileReader(cfgFile)) {
			JsonObject obj = new Gson().fromJson(configurationFile, JsonObject.class);
			for(Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				saveValues.add(entry.getKey(), entry.getValue());
			}
		} catch(IOException e) {
			WildfireGender.LOGGER.error("Failed to load config file", e);
		}
	}
}

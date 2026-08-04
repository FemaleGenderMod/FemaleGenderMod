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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.wildfire.main.WildfireGender;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.GsonHelper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AbstractConfiguration {

    private final File cfgFile;

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

    public boolean exists() {
        return cfgFile.exists();
    }

    public <TYPE> void save(Codec<TYPE> codec, TYPE value) {
        if (supportsSaving()) {
            //TODO: Do we want to log if it fails to encode?
            Optional<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, value).resultOrPartial();
            if (result.isPresent()) {
                try (FileWriter writer = new FileWriter(cfgFile, StandardCharsets.UTF_8); JsonWriter jsonWriter = new JsonWriter(writer)) {
                    jsonWriter.setIndent("\t");
                    GsonHelper.writeValue(jsonWriter, result.get(), Comparator.naturalOrder());
                } catch (IOException e) {
                    WildfireGender.LOGGER.error("Failed to save config file", e);
                }
            }
        }
    }

    public JsonObject read() {
        if (supportsSaving() && cfgFile.exists()) {
            try (FileReader configurationFile = new FileReader(cfgFile, StandardCharsets.UTF_8)) {
                return GsonHelper.parse(configurationFile);
            } catch (IOException e) {
                WildfireGender.LOGGER.error("Failed to load config file", e);
            }
        }
        return new JsonObject();
    }
}

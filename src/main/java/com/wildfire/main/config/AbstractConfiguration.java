package com.wildfire.main.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import com.wildfire.main.WildfireGender;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;

public abstract class AbstractConfiguration {
    private static final TypeAdapter<JsonObject> ADAPTER = (new Gson()).getAdapter(JsonObject.class);
    private final File CFG_FILE;
    public JsonObject SAVE_VALUES = new JsonObject();

    protected AbstractConfiguration(String directory, String cfgName) {
        // En Forge 1.21.1 usamos FMLPaths.CONFIGDIR para obtener la carpeta /config
        Path saveDir = FMLPaths.CONFIGDIR.get().resolve(directory);

        if (supportsSaving() && !Files.isDirectory(saveDir)) {
            try {
                Files.createDirectories(saveDir);
            } catch (IOException e) {
                WildfireGender.LOGGER.error("Failed to create config directory", e);
            }
        }

        this.CFG_FILE = saveDir.resolve(cfgName + ".json").toFile();
    }

    public static boolean supportsSaving() {
        // EnvType.SERVER de Fabric es Dist.DEDICATED_SERVER en Forge
        return FMLEnvironment.dist != Dist.DEDICATED_SERVER;
    }

    public <TYPE> void set(ConfigKey<TYPE> key, TYPE value) {
        key.save(this.SAVE_VALUES, value);
    }

    @SuppressWarnings("unchecked")
    public <TYPE> TYPE get(ConfigKey<TYPE> key) {
        return (TYPE)key.read(this.SAVE_VALUES);
    }

    public <TYPE> void setDefault(ConfigKey<TYPE> key) {
        if (!this.SAVE_VALUES.has(key.key)) {
            this.set(key, key.defaultValue);
        }
    }

    public void removeParameter(ConfigKey<?> key) {
        this.removeParameter(key.key);
    }

    public void removeParameter(String key) {
        this.SAVE_VALUES.remove(key);
    }

    public void save() {
        if (supportsSaving()) {
            // Uso de try-with-resources para limpiar los streams automáticamente
            try (FileWriter writer = new FileWriter(this.CFG_FILE);
                 JsonWriter jsonWriter = new JsonWriter(writer)) {

                jsonWriter.setIndent("\t");
                ADAPTER.write(jsonWriter, this.SAVE_VALUES);

            } catch (IOException e) {
                WildfireGender.LOGGER.error("Failed to save config file: " + this.CFG_FILE.getName(), e);
            }
        }
    }

    public void load() {
        if (supportsSaving() && this.CFG_FILE.exists()) {
            try (FileReader configurationFile = new FileReader(this.CFG_FILE)) {
                JsonObject obj = new Gson().fromJson(configurationFile, JsonObject.class);

                if (obj != null) {
                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                        this.SAVE_VALUES.add(entry.getKey(), entry.getValue());
                    }
                }
            } catch (IOException e) {
                WildfireGender.LOGGER.error("Failed to load config file: " + this.CFG_FILE.getName(), e);
            }
        }
    }
}
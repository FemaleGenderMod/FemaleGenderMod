package com.wildfire.main.config;

import net.neoforged.fml.loading.FMLPaths;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BreastPresetConfiguration extends AbstractConfiguration {
    private static final String PRESETS_DIR = "WildfireGender/presets";

    // Definición de las llaves de configuración para los presets
    public static final StringConfigKey PRESET_NAME = new StringConfigKey("preset_name", "");
    public static final FloatConfigKey BUST_SIZE = new FloatConfigKey("bust_size", 0.6F, 0.0F, 0.8F);
    public static final FloatConfigKey BREASTS_OFFSET_X = new FloatConfigKey("breasts_xOffset", 0.0F, -1.0F, 1.0F);
    public static final FloatConfigKey BREASTS_OFFSET_Y = new FloatConfigKey("breasts_yOffset", 0.0F, -1.0F, 1.0F);
    public static final FloatConfigKey BREASTS_OFFSET_Z = new FloatConfigKey("breasts_zOffset", 0.0F, -1.0F, 0.0F);
    public static final BooleanConfigKey BREASTS_UNIBOOB = new BooleanConfigKey("breasts_uniboob", true);
    public static final FloatConfigKey BREASTS_CLEAVAGE = new FloatConfigKey("breasts_cleavage", 0.0F, 0.0F, 0.1F);

    public BreastPresetConfiguration(String cfgName) {
        super(PRESETS_DIR, cfgName);
    }

    /**
     * Busca todos los archivos .json en la carpeta de presets y los carga en una lista.
     */
    public static BreastPresetConfiguration[] getBreastPresetConfigurationFiles() {
        List<BreastPresetConfiguration> presets = new ArrayList<>();

        // Obtenemos la ruta de configuración de Forge
        File saveDir = FMLPaths.CONFIGDIR.get().resolve(PRESETS_DIR).toFile();

        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        File[] presetFiles = saveDir.listFiles((dir, name) -> name.endsWith(".json"));

        if (presetFiles != null) {
            for (File f : presetFiles) {
                // Removemos la extensión ".json" para obtener el nombre del preset
                String name = f.getName().substring(0, f.getName().length() - 5);
                BreastPresetConfiguration cfg = new BreastPresetConfiguration(name);
                cfg.load();
                presets.add(cfg);
            }
        }

        return presets.toArray(new BreastPresetConfiguration[0]);
    }
}
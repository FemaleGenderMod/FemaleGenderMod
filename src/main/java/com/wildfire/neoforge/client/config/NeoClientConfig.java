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

package com.wildfire.neoforge.client.config;

import com.wildfire.client.config.ClientConfig;
import com.wildfire.client.config.ClientConfigInstance;
import com.wildfire.client.config.CloudSyncConfig;
import com.wildfire.client.config.ConfigOverrides;
import com.wildfire.common.LoaderAgnostics;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.config.Configuration;
import com.wildfire.common.config.GenderConfigTranslations;
import com.wildfire.common.config.enums.ShowPlayerListMode;
import com.wildfire.common.config.value.ConfigKey;
import com.wildfire.common.config.value.ConfigValue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jspecify.annotations.Nullable;

public class NeoClientConfig implements ClientConfig {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread result = new Thread(r, "Female-Gender-Mod-Client-Config-Saver");
        result.setDaemon(true);
        return result;
    });

    private final ModConfigSpec configSpec;
    private final ClientConfigInstance configInstance;
    private final BooleanSupplier displayOwnNametag;

    public NeoClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        // region Debug options
        if (LoaderAgnostics.INSTANCE.isDevelopmentEnv()) {
            displayOwnNametag = builder.define("displayOwnNametag", false);
        } else {
            displayOwnNametag = () -> false;
        }
        // endregion

        ConfigValue<Boolean> firstTimeLoad = createConfigValue(builder, ClientConfigInstance.FIRST_TIME_LOAD, "firstTimeLoad", GenderConfigTranslations.CLIENT_FIRST_TIME_LOAD);
        ConfigValue<Boolean> showToast = createConfigValue(builder, ClientConfigInstance.SHOW_TOAST, "showToast", GenderConfigTranslations.CLIENT_SHOW_TOAST);

        ConfigValue<Boolean> armorStat = createConfigValue(builder, ClientConfigInstance.ARMOR_STAT, "armorStat", GenderConfigTranslations.CLIENT_ARMOR_STAT);
        ConfigValue<ShowPlayerListMode> playerListMode = createConfigValue(builder, ClientConfigInstance.PLAYER_LIST_MODE, "playerListMode", GenderConfigTranslations.CLIENT_PLAYER_LIST_MODE);
        ConfigValue<Boolean> hideOwnContributorNameTag = createConfigValue(builder, ClientConfigInstance.HIDE_OWN_CONTRIBUTOR_TAG, "hideOwnContributorNameTag", GenderConfigTranslations.CLIENT_HIDE_OWN_CONTRIBUTOR_TAG);

        applyToBuilder(GenderConfigTranslations.CLIENT_CLOUD_SYNC, builder).push("cloud_sync");
        CloudSyncConfig cloudSync = new CloudSyncConfig(
            createConfigValue(builder, CloudSyncConfig.CLOUD_SYNC_ENABLED, "enabled", GenderConfigTranslations.CLIENT_CLOUD_SYNC_ENABLED),
            createConfigValue(builder, CloudSyncConfig.AUTOMATIC_CLOUD_SYNC, "automatic", GenderConfigTranslations.CLIENT_CLOUD_SYNC_AUTOMATIC),
            createConfigValue(builder, CloudSyncConfig.CLOUD_SERVER, "server", GenderConfigTranslations.CLIENT_CLOUD_SYNC_SERVER),
            createConfigValue(builder, CloudSyncConfig.SYNC_VERBOSITY, "logVerbosity", GenderConfigTranslations.CLIENT_CLOUD_SYNC_LOG_VERBOSITY)
        );
        builder.pop();//End cloud_sync

        applyToBuilder(GenderConfigTranslations.CLIENT_OVERRIDE, builder).push("override");
        ConfigOverrides overrides = new ConfigOverrides(
            createConfigValue(builder, ConfigOverrides.ARMOR_PHYSICS_OVERRIDE, "armorPhysics", GenderConfigTranslations.CLIENT_OVERRIDE_ARMOR_PHYSICS),
            createConfigValue(builder, ConfigOverrides.DISABLE_RENDERING, "disableRendering", GenderConfigTranslations.CLIENT_OVERRIDE_DISABLE_RENDERING),
            createConfigValue(builder, ConfigOverrides.DISABLE_SOUND_REPLACEMENT, "disableSoundReplacement", GenderConfigTranslations.CLIENT_OVERRIDE_DISABLE_SOUND_REPLACEMENT)
        );
        builder.pop();//End override

        configInstance = new ClientConfigInstance(firstTimeLoad, showToast, armorStat, playerListMode, hideOwnContributorNameTag, cloudSync, overrides);
        configSpec = builder.build();
    }

    @SuppressWarnings({"unchecked", "rawtypes", "ConstantValue"})
    private static <TYPE> ConfigValue<TYPE> createConfigValue(ModConfigSpec.Builder builder, ConfigKey<TYPE> key, String name, GenderConfigTranslations translation) {
        applyToBuilder(translation, builder);
        TYPE defaultValue = key.defaultValue();
        ModConfigSpec.ConfigValue<TYPE> configValue = switch (defaultValue) {
            //Note: Booleans and Enums can make use of the actual default, as there is no need for them to be lazily created to ensure a fresh instance
            case Boolean val -> (ModConfigSpec.ConfigValue<TYPE>) builder.define(name, val.booleanValue());
            case Enum<?> val -> (ModConfigSpec.ConfigValue<TYPE>) builder.defineEnum(name, (Enum) val);
            default -> {
                Class<?> keyType = defaultValue.getClass();
                yield builder.define(name, key::defaultValue, o -> o != null && keyType.isAssignableFrom(o.getClass()) && key.validate((TYPE) keyType.cast(o)));
            }
        };
        return new NeoBackedConfigValue<>(key.validator(), configValue);
    }

    private static ModConfigSpec.Builder applyToBuilder(GenderConfigTranslations translation, ModConfigSpec.Builder builder) {
        return builder.translation(translation.getTranslationKey()).comment(translation.tooltip());
    }

    @Override
    public ClientConfigInstance current() {
        return configInstance;
    }

    @Override
    public void load(@Nullable Object data) {
        if (data instanceof ModContainer modContainer) {
            String fileName = Configuration.CONFIG_DIR + "/client.toml";
            modContainer.registerConfig(Type.CLIENT, configSpec, fileName);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
            //Check if a json file exists and if so try to migrate it
            /*Path baseConfigDir = LoaderAgnostics.INSTANCE.getConfigDir();
            File jsonFile = baseConfigDir.resolve(WildfireAPI.MODID + ".json").toFile();
            if (jsonFile.exists()) {
                File tomlFile = baseConfigDir.resolve(fileName).toFile();
                if (!tomlFile.exists()) {
                    //Note: We only try to migrate the json config to a toml file if the json one doesn't exist
                    //TODO - Neo: If there is a json config file, load from it/make the migration handle loading it?
                }
            }*/
        }
    }

    // region Debug options
    @Override
    public boolean displayOwnNameTag() {
        return displayOwnNametag.getAsBoolean();
    }
    // endregion

    @Override
    public void save() {
        EXECUTOR.submit(new ConfigSaver(configSpec));
    }

    private static class ConfigSaver implements Runnable {

        private final ModConfigSpec configSpec;
        private int retries = 0;

        private ConfigSaver(ModConfigSpec configSpec) {
            this.configSpec = configSpec;
        }

        @Override
        public void run() {
            try {
                configSpec.save();
            } catch (Exception e) {
                WildfireGender.LOGGER.error("Failed to save config", e);
                if (retries++ < 3) {
                    EXECUTOR.submit(this);
                } else {
                    WildfireGender.LOGGER.error("Giving up");
                }
            }
        }
    }
}

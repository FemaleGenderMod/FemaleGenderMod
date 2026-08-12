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

package com.wildfire.client.config;

import com.wildfire.common.WildfireGender;
import com.wildfire.common.config.Configuration;
import com.wildfire.common.config.GenderConfigTranslations;
import com.wildfire.common.config.enums.ShowPlayerListMode;
import com.wildfire.common.config.enums.SyncVerbosity;
import com.wildfire.common.config.value.ConfigKey;
import com.wildfire.common.config.value.ConfigValue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.util.TriState;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jspecify.annotations.Nullable;

public class NeoClientConfig implements ClientConfig {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final ModConfigSpec configSpec;
    private final ClientConfigInstance configInstance;

    public NeoClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ConfigValue<Boolean> firstTimeLoad = createConfigValue(builder, ClientConfigInstance.FIRST_TIME_LOAD, "firstTimeLoad", GenderConfigTranslations.CLIENT_FIRST_TIME_LOAD);
        ConfigValue<Boolean> showToast = createConfigValue(builder, ClientConfigInstance.SHOW_TOAST, "showToast", GenderConfigTranslations.CLIENT_SHOW_TOAST);

        ConfigValue<Boolean> armorStat = createConfigValue(builder, ClientConfigInstance.ARMOR_STAT, "armorStat", GenderConfigTranslations.CLIENT_ARMOR_STAT);
        ConfigValue<ShowPlayerListMode> playerListMode = createConfigValue(builder, ClientConfigInstance.PLAYER_LIST_MODE, "playerListMode", GenderConfigTranslations.CLIENT_PLAYER_LIST_MODE);
        ConfigValue<Boolean> hideOwnContributorNameTag = createConfigValue(builder, ClientConfigInstance.HIDE_OWN_CONTRIBUTOR_TAG, "hideOwnContributorNameTag", GenderConfigTranslations.CLIENT_HIDE_OWN_CONTRIBUTOR_TAG);


        applyToBuilder(GenderConfigTranslations.CLIENT_CLOUD_SYNC, builder).push("cloud_sync");
        ConfigValue<Boolean> cloudSync = createConfigValue(builder, ClientConfigInstance.CLOUD_SYNC_ENABLED, "enabled", GenderConfigTranslations.CLIENT_CLOUD_SYNC_ENABLED);
        ConfigValue<Boolean> automaticCloudSync = createConfigValue(builder, ClientConfigInstance.AUTOMATIC_CLOUD_SYNC, "automatic", GenderConfigTranslations.CLIENT_CLOUD_SYNC_AUTOMATIC);
        ConfigValue<String> cloudServer = createConfigValue(builder, ClientConfigInstance.CLOUD_SERVER, "server", GenderConfigTranslations.CLIENT_CLOUD_SYNC_SERVER);
        ConfigValue<SyncVerbosity> syncLogVerbosity = createConfigValue(builder, ClientConfigInstance.SYNC_VERBOSITY, "logVerbosity", GenderConfigTranslations.CLIENT_CLOUD_SYNC_LOG_VERBOSITY);
        builder.pop();//End cloud_sync

        applyToBuilder(GenderConfigTranslations.CLIENT_OVERRIDE, builder).push("override");
        ConfigValue<Boolean> armorPhysicsOverride = createConfigValue(builder, ClientConfigInstance.ARMOR_PHYSICS_OVERRIDE, "armorPhysics", GenderConfigTranslations.CLIENT_OVERRIDE_ARMOR_PHYSICS);

        ConfigValue<Boolean> disableRendering = createConfigValue(builder, ClientConfigInstance.DISABLE_RENDERING, "disableRendering", GenderConfigTranslations.CLIENT_OVERRIDE_DISABLE_RENDERING);
        ConfigValue<Boolean> disableSoundReplacement = createConfigValue(builder, ClientConfigInstance.DISABLE_SOUND_REPLACEMENT, "disableSoundReplacement", GenderConfigTranslations.CLIENT_OVERRIDE_DISABLE_SOUND_REPLACEMENT);
        builder.pop();//End override

        configInstance = new ClientConfigInstance(firstTimeLoad, showToast, armorStat, playerListMode, hideOwnContributorNameTag,
            cloudSync, automaticCloudSync, cloudServer, syncLogVerbosity,
            armorPhysicsOverride, disableRendering, disableSoundReplacement
        );
        configSpec = builder.build();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <TYPE> ConfigValue<TYPE> createConfigValue(ModConfigSpec.Builder builder, ConfigKey<TYPE> key, String name, GenderConfigTranslations translation) {
        applyToBuilder(translation, builder);
        //TODO: If we add cases where the default value uses the supplier instead of a constant, we need to change the below cases to support the supplier
        TYPE defaultValue = key.defaultValue();
        //TODO: If we ever need to validate things for some values, add variants that call the builder properly
        ModConfigSpec.ConfigValue<TYPE> configValue = switch (defaultValue) {
            case Boolean val -> (ModConfigSpec.ConfigValue<TYPE>) builder.define(name, val.booleanValue());
            case Enum<?> val -> (ModConfigSpec.ConfigValue<TYPE>) builder.defineEnum(name, (Enum) val);
            default -> builder.define(name, defaultValue);
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
        //TODO - Neo: If there is a json config file, load from it/make the migration handle loading it?
        if (data instanceof ModContainer modContainer) {
            modContainer.registerConfig(Type.CLIENT, configSpec, Configuration.CONFIG_DIR + "/client.toml");
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    @Override
    public void save() {
        //TODO - both: Implement/Re-evaluate if we can just call save on the ConfigValues and whether some guis are meant to only save on close
        // At which point we potentially should be rolling them back?
        EXECUTOR.submit(new ConfigSaver(configSpec));
    }

    @Override
    public TriState holidayCosmetics() {
        return ClientConfig.super.holidayCosmetics();
    }

    @Override
    public boolean displayOwnNameTag() {
        return ClientConfig.super.displayOwnNameTag();
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

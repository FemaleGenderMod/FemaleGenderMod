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

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.main.config.enums.ShowPlayerListMode;
import com.wildfire.main.config.enums.SyncVerbosity;
import com.wildfire.main.config.value.ConfigKey;
import com.wildfire.main.config.value.ConfigValue;
import net.minecraft.util.TriState;

public class ClientConfig {

    // note: this option is not intended to be saved in any persistent manner
    public static boolean RENDER_BREASTS = true;
    // region Debug options
    public static TriState HOLIDAY_COSMETICS = TriState.DEFAULT;
    public static boolean DISPLAY_OWN_NAMETAG = false;
    // endregion

    private static final ConfigKey<Boolean> ARMOR_PHYSICS_OVERRIDE = ConfigKey.create("armor_physics_override", false);

    private static final ConfigKey<Boolean> FIRST_TIME_LOAD = ConfigKey.create("firstTimeLoad", true);
    private static final ConfigKey<Boolean> SHOW_TOAST = ConfigKey.create("showToast", true);
    private static final ConfigKey<Boolean> CLOUD_SYNC_ENABLED = ConfigKey.create("cloud_sync", false);
    private static final ConfigKey<Boolean> AUTOMATIC_CLOUD_SYNC = ConfigKey.create("sync_player_data", false);
    /// @see com.wildfire.main.cloud.CloudSync#DEFAULT_CLOUD_URL for the actual default
    private static final ConfigKey<String> CLOUD_SERVER = ConfigKey.create("cloud_server", "", Codec.STRING);
    private static final ConfigKey<SyncVerbosity> SYNC_VERBOSITY = ConfigKey.create("sync_log_verbosity", SyncVerbosity.DEFAULT, SyncVerbosity.CODEC_OR_LEGACY);

    private static final ConfigKey<ShowPlayerListMode> PLAYER_LIST_MODE = ConfigKey.create("alwaysShowList", ShowPlayerListMode.MOD_UI_ONLY, ShowPlayerListMode.CODEC_OR_LEGACY);

    private static final ConfigKey<Boolean> ARMOR_STAT = ConfigKey.create("armor_stat", true);

    private static final ConfigKey<Boolean> HIDE_OWN_CONTRIBUTOR_TAG = ConfigKey.create("hide_own_contributor_nametag", false);

    public static final Codec<ClientConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ARMOR_PHYSICS_OVERRIDE.codecOrDefault().forGetter(config -> config.armorPhysicsOverride.get()),
        FIRST_TIME_LOAD.codecOrDefault().forGetter(config -> config.firstTimeLoad.get()),
        CLOUD_SYNC_ENABLED.codecOrDefault().forGetter(config -> config.cloudSyncEnabled.get()),
        AUTOMATIC_CLOUD_SYNC.codecOrDefault().forGetter(config -> config.automaticCloudSync.get()),
        CLOUD_SERVER.codecOrDefault().forGetter(config -> config.cloudServer.get()),
        SYNC_VERBOSITY.codecOrDefault().forGetter(config -> config.syncVerbosity.get()),
        PLAYER_LIST_MODE.codecOrDefault().forGetter(config -> config.playerListMode.get()),
        ARMOR_STAT.codecOrDefault().forGetter(config -> config.armorStat.get()),
        HIDE_OWN_CONTRIBUTOR_TAG.codecOrDefault().forGetter(config -> config.hideOwnContributorTag.get()),
        SHOW_TOAST.codecOrDefault().forGetter(config -> config.showToast.get())
    ).apply(instance, ClientConfig::new));

    private static final Configuration<ClientConfig> cfgFile = new Configuration<>(".", "female_gender_mod", CODEC);
    private static ClientConfig config = CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()).getOrThrow();
    static {
        if (!cfgFile.exists()) {
            save();
        }
    }

    public static ClientConfig config() {
        return config;
    }

    public static void load() {
        config = cfgFile.load();
    }

    public static void save() {
        cfgFile.save(config);
    }

    public final ConfigValue<Boolean> armorPhysicsOverride;
    public final ConfigValue<Boolean> firstTimeLoad;
    public final ConfigValue<Boolean> cloudSyncEnabled;
    public final ConfigValue<Boolean> automaticCloudSync;
    public final ConfigValue<String> cloudServer;
    public final ConfigValue<SyncVerbosity> syncVerbosity;
    public final ConfigValue<ShowPlayerListMode> playerListMode;
    public final ConfigValue<Boolean> armorStat;
    public final ConfigValue<Boolean> hideOwnContributorTag;
    public final ConfigValue<Boolean> showToast;

    private ClientConfig(boolean armorPhysicsOverride, boolean firstTimeLoad, boolean cloudSyncEnabled, boolean automaticCloudSync, String cloudServer,
        SyncVerbosity syncVerbosity, ShowPlayerListMode playerListMode, boolean armorStat, boolean hideOwnContributorTag, boolean showToast) {
        this.armorPhysicsOverride = ARMOR_PHYSICS_OVERRIDE.createValueHandler(armorPhysicsOverride);
        this.firstTimeLoad = FIRST_TIME_LOAD.createValueHandler(firstTimeLoad);
        this.cloudSyncEnabled = CLOUD_SYNC_ENABLED.createValueHandler(cloudSyncEnabled);
        this.automaticCloudSync = AUTOMATIC_CLOUD_SYNC.createValueHandler(automaticCloudSync);
        this.cloudServer = CLOUD_SERVER.createValueHandler(cloudServer);
        this.syncVerbosity = SYNC_VERBOSITY.createValueHandler(syncVerbosity);
        this.playerListMode = PLAYER_LIST_MODE.createValueHandler(playerListMode);
        this.armorStat = ARMOR_STAT.createValueHandler(armorStat);
        this.hideOwnContributorTag = HIDE_OWN_CONTRIBUTOR_TAG.createValueHandler(hideOwnContributorTag);
        this.showToast = SHOW_TOAST.createValueHandler(showToast);
    }
}

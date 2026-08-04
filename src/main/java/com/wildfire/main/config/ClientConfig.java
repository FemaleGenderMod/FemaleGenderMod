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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.main.config.enums.ShowPlayerListMode;
import com.wildfire.main.config.enums.SyncVerbosity;
import com.wildfire.main.config.types.ConfigKey;
import net.minecraft.util.TriState;

public class ClientConfig {

    // note: this option is not intended to be saved in any persistent manner
    public static boolean RENDER_BREASTS = true;
    // region Debug options
    public static TriState HOLIDAY_COSMETICS = TriState.DEFAULT;
    public static boolean DISPLAY_OWN_NAMETAG = false;
    // endregion

    public static final ConfigKey<Boolean> ARMOR_PHYSICS_OVERRIDE = ConfigKey.create("armor_physics_override", false);

    public static final ConfigKey<Boolean> FIRST_TIME_LOAD = ConfigKey.create("firstTimeLoad", true);
    public static final ConfigKey<Boolean> SHOW_TOAST = ConfigKey.create("showToast", true);
    public static final ConfigKey<Boolean> CLOUD_SYNC_ENABLED = ConfigKey.create("cloud_sync", false);
    public static final ConfigKey<Boolean> AUTOMATIC_CLOUD_SYNC = ConfigKey.create("sync_player_data", false);
    /// @see com.wildfire.main.cloud.CloudSync#DEFAULT_CLOUD_URL for the actual default
    public static final ConfigKey<String> CLOUD_SERVER = ConfigKey.create("cloud_server", "", Codec.STRING);
    public static final ConfigKey<SyncVerbosity> SYNC_VERBOSITY = ConfigKey.create("sync_log_verbosity", SyncVerbosity.DEFAULT, SyncVerbosity.CODEC_OR_LEGACY);

    public static final ConfigKey<ShowPlayerListMode> ALWAYS_SHOW_LIST = ConfigKey.create("alwaysShowList", ShowPlayerListMode.MOD_UI_ONLY, ShowPlayerListMode.CODEC_OR_LEGACY);

    public static final ConfigKey<Boolean> ARMOR_STAT = ConfigKey.create("armor_stat", true);

    public static final ConfigKey<Boolean> HIDE_OWN_CONTRIBUTOR_TAG = ConfigKey.create("hide_own_contributor_nametag", false);

    public static final Codec<ClientConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ARMOR_PHYSICS_OVERRIDE.codecOrDefault().forGetter(config -> config.armorPhysicsOverride),
        FIRST_TIME_LOAD.codecOrDefault().forGetter(config -> config.firstTimeLoad),
        CLOUD_SYNC_ENABLED.codecOrDefault().forGetter(config -> config.cloudSyncEnabled),
        AUTOMATIC_CLOUD_SYNC.codecOrDefault().forGetter(config -> config.automaticCloudSync),
        CLOUD_SERVER.codecOrDefault().forGetter(config -> config.cloudServer),
        SYNC_VERBOSITY.codecOrDefault().forGetter(config -> config.syncVerbosity),
        ALWAYS_SHOW_LIST.codecOrDefault().forGetter(config -> config.alwaysShowList),
        ARMOR_STAT.codecOrDefault().forGetter(config -> config.armorStat),
        HIDE_OWN_CONTRIBUTOR_TAG.codecOrDefault().forGetter(config -> config.hideOwnContributorTag),
        SHOW_TOAST.codecOrDefault().forGetter(config -> config.showToast)
    ).apply(instance, ClientConfig::new));

    //TODO: Do we need setters for any of these to validate their values?
    public boolean armorPhysicsOverride;
    public boolean firstTimeLoad;
    public boolean cloudSyncEnabled;
    public boolean automaticCloudSync;
    public String cloudServer;
    public SyncVerbosity syncVerbosity;
    public ShowPlayerListMode alwaysShowList;
    public boolean armorStat;
    public boolean hideOwnContributorTag;
    public boolean showToast;

    private ClientConfig(boolean armorPhysicsOverride, boolean firstTimeLoad, boolean cloudSyncEnabled, boolean automaticCloudSync, String cloudServer,
        SyncVerbosity syncVerbosity, ShowPlayerListMode alwaysShowList, boolean armorStat, boolean hideOwnContributorTag, boolean showToast) {
        this.armorPhysicsOverride = armorPhysicsOverride;
        this.firstTimeLoad = firstTimeLoad;
        this.cloudSyncEnabled = cloudSyncEnabled;
        this.automaticCloudSync = automaticCloudSync;
        this.cloudServer = cloudServer;
        this.syncVerbosity = syncVerbosity;
        this.alwaysShowList = alwaysShowList;
        this.armorStat = armorStat;
        this.hideOwnContributorTag = hideOwnContributorTag;
        this.showToast = showToast;
    }
}

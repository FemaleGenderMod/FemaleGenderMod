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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.client.cloud.CloudSync;
import com.wildfire.common.config.enums.ShowPlayerListMode;
import com.wildfire.common.config.enums.SyncVerbosity;
import com.wildfire.common.config.value.ConfigKey;
import com.wildfire.common.config.value.ConfigValue;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.TriState;

//TODO - both: Do we want to move some of the subsections into their own sub files, similar to how breasts are for entity configs
public record ClientConfigInstance(
    ConfigValue<Boolean> firstTimeLoad, ConfigValue<Boolean> showToast, ConfigValue<Boolean> armorStat, ConfigValue<ShowPlayerListMode> playerListMode,
    ConfigValue<Boolean> hideOwnContributorTag,
    //Cloud Sync
    ConfigValue<Boolean> cloudSyncEnabled, ConfigValue<Boolean> automaticCloudSync, ConfigValue<String> cloudServer, ConfigValue<SyncVerbosity> syncVerbosity,
    //Overrides
    ConfigValue<Boolean> overrideArmorPhysics, ConfigValue<Boolean> disableRendering, ConfigValue<Boolean> disableSoundReplacement
) {

    // region Debug options
    public static TriState HOLIDAY_COSMETICS = TriState.DEFAULT;
    public static boolean DISPLAY_OWN_NAMETAG = false;
    // endregion

    static final ConfigKey<Boolean> FIRST_TIME_LOAD = ConfigKey.DEFAULT_TRUE;
    static final ConfigKey<Boolean> SHOW_TOAST = ConfigKey.DEFAULT_TRUE;
    static final ConfigKey<ShowPlayerListMode> PLAYER_LIST_MODE = new ConfigKey<>(ShowPlayerListMode.MOD_UI_ONLY, ShowPlayerListMode.CODEC_OR_LEGACY, ShowPlayerListMode.STREAM_CODEC);
    static final ConfigKey<Boolean> ARMOR_STAT = ConfigKey.DEFAULT_TRUE;
    static final ConfigKey<Boolean> HIDE_OWN_CONTRIBUTOR_TAG = ConfigKey.DEFAULT_FALSE;

    // region Cloud settings
    static final ConfigKey<Boolean> CLOUD_SYNC_ENABLED = ConfigKey.DEFAULT_FALSE;
    static final ConfigKey<Boolean> AUTOMATIC_CLOUD_SYNC = ConfigKey.DEFAULT_FALSE;
    /// @see CloudSync#DEFAULT_CLOUD_URL for the actual default
    static final ConfigKey<String> CLOUD_SERVER = new ConfigKey<>("", Codec.STRING, ByteBufCodecs.STRING_UTF8);
    static final ConfigKey<SyncVerbosity> SYNC_VERBOSITY = new ConfigKey<>(SyncVerbosity.DEFAULT, SyncVerbosity.CODEC_OR_LEGACY, SyncVerbosity.STREAM_CODEC);
    // endregion

    // region Overrides
    static final ConfigKey<Boolean> ARMOR_PHYSICS_OVERRIDE = ConfigKey.DEFAULT_FALSE;
    static final ConfigKey<Boolean> DISABLE_RENDERING = ConfigKey.DEFAULT_FALSE;
    static final ConfigKey<Boolean> DISABLE_SOUND_REPLACEMENT = ConfigKey.DEFAULT_FALSE;
    // endregion

    static final Codec<ClientConfigInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        FIRST_TIME_LOAD.codecOrDefault("firstTimeLoad").forGetter(config -> config.firstTimeLoad.get()),
        SHOW_TOAST.codecOrDefault("showToast").forGetter(config -> config.showToast.get()),

        ARMOR_STAT.codecOrDefault("armor_stat").forGetter(config -> config.armorStat.get()),
        PLAYER_LIST_MODE.codecOrDefault("alwaysShowList").forGetter(config -> config.playerListMode.get()),
        HIDE_OWN_CONTRIBUTOR_TAG.codecOrDefault("hide_own_contributor_nametag").forGetter(config -> config.hideOwnContributorTag.get()),

        //Cloud
        CLOUD_SYNC_ENABLED.codecOrDefault("cloud_sync").forGetter(config -> config.cloudSyncEnabled.get()),
        AUTOMATIC_CLOUD_SYNC.codecOrDefault("sync_player_data").forGetter(config -> config.automaticCloudSync.get()),
        CLOUD_SERVER.codecOrDefault("cloud_server").forGetter(config -> config.cloudServer.get()),
        SYNC_VERBOSITY.codecOrDefault("sync_log_verbosity").forGetter(config -> config.syncVerbosity.get()),
        //Overrides
        ARMOR_PHYSICS_OVERRIDE.codecOrDefault("armor_physics_override").forGetter(config -> config.overrideArmorPhysics.get()),
        DISABLE_RENDERING.codecOrDefault("armor_physics_override").forGetter(config -> config.disableRendering.get()),
        DISABLE_SOUND_REPLACEMENT.codecOrDefault("armor_physics_override").forGetter(config -> config.disableSoundReplacement.get())
    ).apply(instance, ClientConfigInstance::new));

    //TODO - both: Reorder this
    private ClientConfigInstance(boolean firstTimeLoad, boolean showToast, boolean armorStat, ShowPlayerListMode playerListMode, boolean hideOwnContributorTag,
        //Cloud sync
        boolean cloudSyncEnabled, boolean automaticCloudSync, String cloudServer, SyncVerbosity syncVerbosity,
        //Overrides
        boolean armorPhysicsOverride, boolean disableRendering, boolean disableSoundReplacement) {
        this(FIRST_TIME_LOAD.createValueHandler(firstTimeLoad),
            SHOW_TOAST.createValueHandler(showToast),
            ARMOR_STAT.createValueHandler(armorStat),
            PLAYER_LIST_MODE.createValueHandler(playerListMode),
            HIDE_OWN_CONTRIBUTOR_TAG.createValueHandler(hideOwnContributorTag),
            //Cloud
            CLOUD_SYNC_ENABLED.createValueHandler(cloudSyncEnabled),
            AUTOMATIC_CLOUD_SYNC.createValueHandler(automaticCloudSync),
            CLOUD_SERVER.createValueHandler(cloudServer),
            SYNC_VERBOSITY.createValueHandler(syncVerbosity),
            //Overrides
            ARMOR_PHYSICS_OVERRIDE.createValueHandler(armorPhysicsOverride),
            DISABLE_RENDERING.createValueHandler(disableRendering),
            DISABLE_SOUND_REPLACEMENT.createValueHandler(disableSoundReplacement)
        );
    }
}

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
import com.wildfire.common.config.enums.ShowPlayerListMode;
import com.wildfire.common.config.value.ConfigKey;
import com.wildfire.common.config.value.ConfigValue;
import net.minecraft.util.TriState;

public record ClientConfigInstance(
    ConfigValue<Boolean> firstTimeLoad, ConfigValue<Boolean> showToast, ConfigValue<Boolean> armorStat, ConfigValue<ShowPlayerListMode> playerListMode,
    ConfigValue<Boolean> hideOwnContributorTag, CloudSyncConfig cloudSync, ConfigOverrides overrides
) {

    // region Debug options
    public static TriState HOLIDAY_COSMETICS = TriState.DEFAULT;
    public static boolean DISPLAY_OWN_NAMETAG = false;
    // endregion

    public static final ConfigKey<Boolean> FIRST_TIME_LOAD = ConfigKey.DEFAULT_TRUE;
    public static final ConfigKey<Boolean> SHOW_TOAST = ConfigKey.DEFAULT_TRUE;
    public static final ConfigKey<ShowPlayerListMode> PLAYER_LIST_MODE = new ConfigKey<>(ShowPlayerListMode.MOD_UI_ONLY, ShowPlayerListMode.CODEC_OR_LEGACY, ShowPlayerListMode.STREAM_CODEC);
    public static final ConfigKey<Boolean> ARMOR_STAT = ConfigKey.DEFAULT_TRUE;
    public static final ConfigKey<Boolean> HIDE_OWN_CONTRIBUTOR_TAG = ConfigKey.DEFAULT_FALSE;

    static final Codec<ClientConfigInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        FIRST_TIME_LOAD.codecOrDefault("firstTimeLoad").forGetter(config -> config.firstTimeLoad.get()),
        SHOW_TOAST.codecOrDefault("showToast").forGetter(config -> config.showToast.get()),

        ARMOR_STAT.codecOrDefault("armor_stat").forGetter(config -> config.armorStat.get()),
        PLAYER_LIST_MODE.codecOrDefault("alwaysShowList").forGetter(config -> config.playerListMode.get()),
        HIDE_OWN_CONTRIBUTOR_TAG.codecOrDefault("hide_own_contributor_nametag").forGetter(config -> config.hideOwnContributorTag.get()),
        CloudSyncConfig.CODEC.forGetter(ClientConfigInstance::cloudSync),
        ConfigOverrides.CODEC.forGetter(ClientConfigInstance::overrides)
    ).apply(instance, ClientConfigInstance::new));

    private ClientConfigInstance(boolean firstTimeLoad, boolean showToast, boolean armorStat, ShowPlayerListMode playerListMode, boolean hideOwnContributorTag,
        CloudSyncConfig cloudSync, ConfigOverrides overrides) {
        this(FIRST_TIME_LOAD.createValueHandler(firstTimeLoad),
            SHOW_TOAST.createValueHandler(showToast),
            ARMOR_STAT.createValueHandler(armorStat),
            PLAYER_LIST_MODE.createValueHandler(playerListMode),
            HIDE_OWN_CONTRIBUTOR_TAG.createValueHandler(hideOwnContributorTag),
            cloudSync,
            overrides
        );
    }
}

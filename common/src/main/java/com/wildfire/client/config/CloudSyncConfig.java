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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.client.cloud.CloudSync;
import com.wildfire.common.config.enums.SyncVerbosity;
import com.wildfire.common.config.value.ConfigKey;
import com.wildfire.common.config.value.ConfigValue;
import net.minecraft.network.codec.ByteBufCodecs;

public record CloudSyncConfig(ConfigValue<Boolean> enabled, ConfigValue<Boolean> automatic, ConfigValue<String> server, ConfigValue<SyncVerbosity> logVerbosity) {

    static final ConfigKey<Boolean> CLOUD_SYNC_ENABLED = ConfigKey.DEFAULT_FALSE;
    static final ConfigKey<Boolean> AUTOMATIC_CLOUD_SYNC = ConfigKey.DEFAULT_FALSE;
    /// @see CloudSync#DEFAULT_CLOUD_URL for the actual default
    static final ConfigKey<String> CLOUD_SERVER = new ConfigKey<>("", Codec.STRING, ByteBufCodecs.STRING_UTF8);
    static final ConfigKey<SyncVerbosity> SYNC_VERBOSITY = new ConfigKey<>(SyncVerbosity.DEFAULT, SyncVerbosity.CODEC_OR_LEGACY, SyncVerbosity.STREAM_CODEC);

    public static final MapCodec<CloudSyncConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CLOUD_SYNC_ENABLED.codecOrDefault("cloud_sync").forGetter(config -> config.enabled.get()),
        AUTOMATIC_CLOUD_SYNC.codecOrDefault("sync_player_data").forGetter(config -> config.automatic.get()),
        CLOUD_SERVER.codecOrDefault("cloud_server").forGetter(config -> config.server.get()),
        SYNC_VERBOSITY.codecOrDefault("sync_log_verbosity").forGetter(config -> config.logVerbosity.get())
    ).apply(instance, CloudSyncConfig::new));

    public CloudSyncConfig(boolean enabled, boolean automatic, String server, SyncVerbosity logVerbosity) {
        this(CLOUD_SYNC_ENABLED.createValueHandler(enabled),
            AUTOMATIC_CLOUD_SYNC.createValueHandler(automatic),
            CLOUD_SERVER.createValueHandler(server),
            SYNC_VERBOSITY.createValueHandler(logVerbosity));
    }
}

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

package com.wildfire.common.entities.players;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.wildfire.client.cloud.CloudSync;
import com.wildfire.client.cloud.SyncLog;
import com.wildfire.client.config.ClientConfig;
import com.wildfire.client.gui.screen.BaseWildfireScreen;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.WildfireLang;
import com.wildfire.common.config.Configuration;
import com.wildfire.common.config.value.ConfigKey;
import com.wildfire.common.entities.avatars.AbstractAvatarConfigHolder;
import com.wildfire.common.entities.avatars.AvatarConfig;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;

public class PlayerConfigHolder extends AbstractAvatarConfigHolder {

    /// `true` if this config should be synced to the connected server on the next attempt
    ///
    /// This only has an effect for the client player.
    public volatile boolean needsSync;

    /// `true` if this config should be synced to the [`cloud sync server`][CloudSync] on the next attempt
    ///
    /// This only has an effect for the client player.
    public volatile boolean needsCloudSync;

    /// The current sync status of this player config
    ///
    /// @see #needsSync
    /// @see SyncStatus
    public volatile SyncStatus syncStatus = SyncStatus.UNKNOWN;

    private final Configuration<AvatarConfig> cfgFile;

    public PlayerConfigHolder(UUID uuid) {
        cfgFile = new Configuration<>(uuid.toString(), AvatarConfig.CODEC);
        super(uuid, AvatarConfig.createDefault());
    }

    public SyncStatus getSyncStatus() {
        return this.syncStatus;
    }

    /// @apiNote Only use this on the client side
    @ApiStatus.Internal
    public void attemptCloudSync() {
        var client = Minecraft.getInstance();
        if(client.player == null || !this.uuid.equals(client.player.getUUID())) return;
        if(!needsCloudSync) return;
        if(client.gui.screen() instanceof BaseWildfireScreen) return;
        if(!ClientConfig.config().cloudSync().automatic().get()) return;
        if(CloudSync.syncOnCooldown()) return;

        CompletableFuture.runAsync(() -> {
            try {
                CloudSync.sync(this).join();
                WildfireGender.LOGGER.info("Synced player data to the cloud");
            } catch(Exception e) {
                WildfireGender.LOGGER.error("Failed to sync player data", e);
                SyncLog.add(WildfireLang.SYNC_LOG_FAILED);
            }
        });
        needsCloudSync = false;
    }

    /// @return `true` if the current player [`has a local config file`][Configuration#exists()]
    public boolean hasLocalConfig() {
        return cfgFile.exists();
    }

    /// Loads the current player's settings from a file on disk
    ///
    /// @param markForSync `true` if [#needsSync] should be set to true
    public void loadFromDisk(boolean markForSync) {
        this.syncStatus = SyncStatus.CACHED;
        config = cfgFile.load();
        if (markForSync) {
            this.needsSync = true;
        }
    }

    /// Saves the settings stored in this [AvatarConfig] to the underlying [Configuration],
    /// and then attempts to [`save to disk`][Configuration#save].
    public void save() {
        cfgFile.save(config);
        needsSync = true;
        needsCloudSync = true;
    }

    /// Returns a copy of the player's current configuration; the stored values are guaranteed to be valid for
    /// the associated [ConfigKey], and does not include any unrecognized keys.
    ///
    /// @return A new copy of the player's [`saved config values`][JsonObject]
    public JsonElement toJson() {
        return AvatarConfig.CODEC.encodeStart(JsonOps.INSTANCE, config).resultOrPartial().orElseGet(JsonObject::new);
    }

    /// Update player data from the provided [JsonObject]
    ///
    /// @apiNote This method will set the player's [`sync status`][#getSyncStatus()] to [SyncStatus#SYNCED],
    ///          as it's expected that this method is only used in such cases where this would be applicable.
    ///
    /// @param serialized The [JsonObject] to merge with the existing config for this player
    public void updateFromJson(JsonElement serialized) {
        super.updateFromJson(serialized);
        this.syncStatus = SyncStatus.SYNCED;
    }

    @Override
    public void updateFromPacket(final AvatarConfig config) {
        super.updateFromPacket(config);
        this.syncStatus = SyncStatus.SYNCED;
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> lines = super.getDebugInfo();
        lines.add(1, "Sync status: " + getSyncStatus());
        return lines;
    }
}

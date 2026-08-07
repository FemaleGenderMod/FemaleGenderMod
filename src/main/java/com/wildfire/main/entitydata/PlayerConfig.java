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

package com.wildfire.main.entitydata;

import com.google.gson.JsonObject;
import com.wildfire.gui.screen.BaseWildfireScreen;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLang;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.cloud.SyncLog;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.config.types.ConfigKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/// A version of [EntityConfig] backed by a [Configuration] for use with players
public class PlayerConfig extends EntityConfig {

    /// `true` if this config should be synced to the connected server on the next attempt
    ///
    /// This only has an effect for the client player.
    public boolean needsSync;

    /// `true` if this config should be synced to the [`cloud sync server`][CloudSync] on the next attempt
    ///
    /// This only has an effect for the client player.
    public boolean needsCloudSync;

    /// The current sync status of this player config
    ///
    /// @see #needsSync
    /// @see SyncStatus
    public SyncStatus syncStatus = SyncStatus.UNKNOWN;

    private final Configuration cfg;
    protected boolean hurtSounds = Configuration.HURT_SOUNDS.getDefault();
    protected boolean holidayThemes = Configuration.HOLIDAY_THEMES.getDefault();
    protected boolean showBreastsInArmor = Configuration.SHOW_IN_ARMOR.getDefault();

    /// @deprecated Use [#updateGender(Gender)] instead
    @Deprecated
    public PlayerConfig(UUID uuid, Gender gender) {
        this(uuid);
        updateGender(gender);
    }

    public PlayerConfig(UUID uuid) {
        super(uuid);
        cfg = new Configuration(uuid.toString());
        cfg.setDefaults();

        // Real players always have a UUID of version 4; if this isn't the case, then this is undeniably
        // an NPC player entity.
        if(uuid.version() != 4) holidayThemes = false;
    }

    // these shouldn't ever be called on players, but just to be safe, override with a noop.
    @Override
    public void readFromStack(ItemStack chestplate) {
    }

    public Configuration getConfig() {
        return cfg;
    }

    public boolean updateGender(Gender value) {
        return updateValue(Configuration.GENDER, value, v -> this.gender = v);
    }

    public boolean updateBustSize(float value) {
        return updateValue(Configuration.BUST_SIZE, value, v -> this.pBustSize = v);
    }


    public boolean hasHolidayThemes() {
        return holidayThemes;
    }

    public boolean updateHolidayThemes(boolean value) {
        return updateValue(Configuration.HOLIDAY_THEMES, value, v -> this.holidayThemes = v);
    }


    public boolean hasHurtSounds() {
        return hurtSounds;
    }

    public boolean updateVoicePitch(float value) {
        return updateValue(Configuration.VOICE_PITCH, value, v -> this.voicePitch = v);
    }

    public boolean updateHurtSounds(boolean value) {
        return updateValue(Configuration.HURT_SOUNDS, value, v -> this.hurtSounds = v);
    }

    public boolean updateBreastPhysics(boolean value) {
        return updateValue(Configuration.BREAST_PHYSICS, value, v -> this.breastPhysics = v);
    }

    /// @apiNote The value this method returns has been moved to [ClientConfig], and this method is only
    /// 			retained for compatibility with mods that use this as a mixin target.
    @Override
    @ApiStatus.Obsolete
    @Environment(EnvType.CLIENT)
    public boolean getArmorPhysicsOverride() {
        return ClientConfig.INSTANCE.get(ClientConfig.ARMOR_PHYSICS_OVERRIDE);
    }

    @Override
    public boolean showBreastsInArmor() {
        return showBreastsInArmor;
    }

    public boolean updateShowBreastsInArmor(boolean value) {
        return updateValue(Configuration.SHOW_IN_ARMOR, value, v -> this.showBreastsInArmor = v);
    }

    public boolean updateBounceMultiplier(float value) {
        return updateValue(Configuration.BOUNCE_MULTIPLIER, value, v -> this.bounceMultiplier = v);
    }

    public boolean updateFloppiness(float value) {
        return updateValue(Configuration.FLOPPY_MULTIPLIER, value, v -> this.floppyMultiplier = v);
    }

    public SyncStatus getSyncStatus() {
        return this.syncStatus;
    }

    /// Returns a copy of the player's current configuration; the stored values are guaranteed to be valid for
    /// the associated [ConfigKey], and does not include any unrecognized keys.
    ///
    /// @return A new copy of the player's [`saved config values`][JsonObject]
    public JsonObject toJson() {
        var json = new JsonObject();
        Configuration.KEYS.forEach(key -> key.dump(this, json));
        return json;
    }

    /// @return `true` if the current player [`has a local config file`][Configuration#exists()]
    public boolean hasLocalConfig() {
        return cfg.exists();
    }

    /// Loads the current player's settings from a file on disk
    ///
    /// @param markForSync`true` if [#needsSync] should be set to true
    public void loadFromDisk(boolean markForSync) {
        this.syncStatus = SyncStatus.CACHED;
        cfg.load();
        loadFromConfig(markForSync);
    }

    /// Loads the current player's settings from the local [Configuration]
    ///
    /// @param markForSync`true` if [#needsSync] should be set to true
    public void loadFromConfig(boolean markForSync) {
        Configuration.KEYS.forEach(key -> key.writeToPlayer(this));
        if(markForSync) {
            this.needsSync = true;
        }
    }

    /// Write all known [ConfigKey]s from this [PlayerConfig] to the underlying [Configuration]
    public void writeToConfig() {
        Configuration.KEYS.forEach(key -> key.writeToConfig(this));
    }

    /// Saves the settings stored in this [PlayerConfig] to the underlying [Configuration],
    /// and then attempts to [`save to disk`][Configuration#save()].
    public void save() {
        writeToConfig();
        getConfig().save();
        needsSync = true;
        needsCloudSync = true;
    }

    /// @deprecated Use `plr.save()` instead
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "First release of 26.1")
    public static void saveGenderInfo(PlayerConfig plr) {
        plr.save();
    }

    @Override
    public boolean hasJacketLayer() {
        throw new UnsupportedOperationException("PlayerConfig does not support #hasJacketLayer(); use Player#isModelPartShown instead");
    }

    @ApiStatus.Internal
    public void attemptCloudSync() {
        var client = Minecraft.getInstance();
        if(client.player == null || !this.uuid.equals(client.player.getUUID())) return;
        if(!needsCloudSync) return;
        //~ if >=26.2 'client.screen' -> 'client.gui.screen()'
        if(client.gui.screen() instanceof BaseWildfireScreen) return;
        if(!ClientConfig.INSTANCE.get(ClientConfig.AUTOMATIC_CLOUD_SYNC)) return;
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

    /// Update player data from the provided [JsonObject]
    ///
    /// @apiNote This method will set the player's [`sync status`][#getSyncStatus()] to [SyncStatus#SYNCED],
    ///          as it's expected that this method is only used in such cases where this would be applicable.
    ///
    /// @param json The [JsonObject] to merge with the existing config for this player
    public void updateFromJson(JsonObject json) {
        json.asMap().forEach(this.cfg::set);
        loadFromConfig(false);
        this.syncStatus = SyncStatus.SYNCED;
    }

    @Override
    public List<String> getDebugInfo() {
        var lines = super.getDebugInfo();
        lines.add(1, "Sync status: " + getSyncStatus());
        lines.add("Female hurt sounds: " + hasHurtSounds());
        lines.add("Show in armor: " + showBreastsInArmor());
        return lines;
    }

    public enum SyncStatus {
        /// Indicates that the relevant configuration has had its data loaded from a file on disk.
        ///
        /// This is only applicable on a client, as dedicated servers do not read player data from
        /// configuration files.
        CACHED,

        /// Indicates that the relevant configuration has had its data loaded from a sync packet,
        /// or from a profile retrieved from [`the cloud sync server`][CloudSync].
        ///
        /// This is currently only set on the client.
        // TODO this should be set on dedicated servers if/when the player config cache is split
        //		into separate server-sided & client-sided caches
        SYNCED,

        /// Indicates that this configuration has an unknown sync state.
        ///
        /// This is the default sync state for new configuration instances, and on dedicated servers is
        /// the only sync state.
        UNKNOWN,
    }
}

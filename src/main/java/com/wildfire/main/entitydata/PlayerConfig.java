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
import com.wildfire.client.gui.screen.BaseWildfireScreen;
import com.wildfire.main.Gender;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLocalization;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.cloud.SyncLog;
import com.wildfire.main.config.ClientConfiguration;
import com.wildfire.main.config.ConfigKey;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.GeneralClientConfig;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A version of {@link EntityConfig} backed by a {@link Configuration} for use with players
 */
@SuppressWarnings("UnusedReturnValue")
public class PlayerConfig extends EntityConfig {

    private final ClientConfiguration cfg;
    public SyncStatus syncStatus = SyncStatus.UNKNOWN;
    public boolean needsSync;
    public boolean needsCloudSync;

    private boolean hurtSounds = ClientConfiguration.HURT_SOUNDS.getDefault();
    protected boolean holidayThemes = ClientConfiguration.HOLIDAY_THEMES.getDefault();
    protected boolean showBreastsInArmor = ClientConfiguration.SHOW_IN_ARMOR.getDefault();
    private boolean armorPhysOverride = ClientConfiguration.ARMOR_PHYSICS_OVERRIDE.getDefault();

    public PlayerConfig(UUID uuid) {
        this(uuid, ClientConfiguration.GENDER.getDefault());
    }

    public PlayerConfig(UUID uuid, Gender gender) {
        super(uuid);
        this.gender = gender;
        this.cfg = new ClientConfiguration(this.uuid.toString());
        this.cfg.set(ClientConfiguration.USERNAME, this.uuid);
        this.cfg.setDefaults(
              ClientConfiguration.GENDER,
              ClientConfiguration.BUST_SIZE,
              ClientConfiguration.HURT_SOUNDS,

              ClientConfiguration.BREASTS_OFFSET_X,
              ClientConfiguration.BREASTS_OFFSET_Y,
              ClientConfiguration.BREASTS_OFFSET_Z,
              ClientConfiguration.BREASTS_UNIBOOB,
              ClientConfiguration.BREASTS_CLEAVAGE,

              ClientConfiguration.BREAST_PHYSICS,
              ClientConfiguration.ARMOR_PHYSICS_OVERRIDE,
              ClientConfiguration.SHOW_IN_ARMOR,
              ClientConfiguration.BOUNCE_MULTIPLIER,
              ClientConfiguration.FLOPPY_MULTIPLIER,

              ClientConfiguration.VOICE_PITCH,
              ClientConfiguration.HOLIDAY_THEMES
        );

        // Real players always have a UUID of version 4; if this isn't the case, then this is undeniably
        // an NPC player entity.
        if (uuid.version() != 4) {
            this.holidayThemes = false;
        }
    }

    // this shouldn't ever be called on players, but just to be safe, override with a noop.
    @Override
    public void readFromStack(@NotNull ItemStack chest) {

    }

    public ClientConfiguration getConfig() {
        return cfg;
    }

    private <VALUE> boolean updateValue(ConfigKey<VALUE> key, VALUE value, Consumer<VALUE> setter) {
        if (key.validate(value)) {
            setter.accept(value);
            return true;
        }
        return false;
    }

    public <VALUE> boolean updateFrom(ConfigKey<VALUE> key, Configuration copyFrom, Consumer<VALUE> setter) {
        VALUE value = copyFrom.get(key);
        if (value == null) {
            return false;
        }
        return updateValue(key, value, setter);
    }

    public boolean updateGender(Gender value) {
        return updateValue(ClientConfiguration.GENDER, value, v -> this.gender = v);
    }

    public boolean updateBustSize(float value) {
        return updateValue(ClientConfiguration.BUST_SIZE, value, v -> this.pBustSize = v);
    }

    public boolean updateBustSize(Configuration copyFrom) {
        return updateFrom(ClientConfiguration.BUST_SIZE, copyFrom, v -> this.pBustSize = v);
    }

    public boolean hasHolidayThemes() {
        return holidayThemes;
    }

    public boolean updateHolidayThemes(boolean value) {
        return updateValue(ClientConfiguration.HOLIDAY_THEMES, value, v -> this.holidayThemes = v);
    }

    public boolean updateVoicePitch(float value) {
        return updateValue(ClientConfiguration.VOICE_PITCH, value, v -> this.voicePitch = v);
    }

    public boolean hasHurtSounds() {
        return hurtSounds;
    }

    public boolean updateHurtSounds(boolean value) {
        return updateValue(ClientConfiguration.HURT_SOUNDS, value, v -> this.hurtSounds = v);
    }

    public boolean updateBreastPhysics(boolean value) {
        return updateValue(ClientConfiguration.BREAST_PHYSICS, value, v -> this.breastPhysics = v);
    }

    @Override
    public boolean getArmorPhysicsOverride() {
        return armorPhysOverride;
    }

    @Override
    public boolean canBreathe() {
        return true;
    }

    public boolean updateArmorPhysicsOverride(boolean value) {
        return updateValue(ClientConfiguration.ARMOR_PHYSICS_OVERRIDE, value, v -> this.armorPhysOverride = v);
    }

    @Override
    public boolean showBreastsInArmor() {
        return showBreastsInArmor;
    }

    public boolean updateShowBreastsInArmor(boolean value) {
        return updateValue(ClientConfiguration.SHOW_IN_ARMOR, value, v -> this.showBreastsInArmor = v);
    }

    public boolean updateBounceMultiplier(float value) {
        return updateValue(ClientConfiguration.BOUNCE_MULTIPLIER, value, v -> this.bounceMultiplier = v);
    }

    public boolean updateFloppiness(float value) {
        return updateValue(ClientConfiguration.FLOPPY_MULTIPLIER, value, v -> this.floppyMultiplier = v);
    }

    public SyncStatus getSyncStatus() {
        return this.syncStatus;
    }

    /**
     * Returns a copy of the player's current configuration. Note that there are no guarantees of any values being valid (either type or number ranges), as this taken
     * directly from the loaded JSON file, which may have been modified by the user.
     *
     * @return A new copy of the player's {@link JsonObject saved config values}
     */
    public JsonObject toJson() {
        return cfg.SAVE_VALUES.deepCopy();
    }

    /**
     * @return {@code true} if the current player {@link Configuration#exists() has a local config file}
     */
    public boolean hasLocalConfig() {
        return cfg.exists();
    }

    /**
     * Loads the current player's settings from a file on disk
     *
     * @param markForSync {@code true} if {@link #needsSync} should be set to true
     */
    public void loadFromDisk(boolean markForSync) {
        this.syncStatus = SyncStatus.CACHED;
        cfg.load();
        loadFromConfig(markForSync);
    }

    /**
     * Loads the current player's settings from the local {@link Configuration}
     *
     * @param markForSync {@code true} if {@link #needsSync} should be set to true
     */
    public void loadFromConfig(boolean markForSync) {
        updateGender(cfg.get(ClientConfiguration.GENDER));
        updateBustSize(cfg.get(ClientConfiguration.BUST_SIZE));
        updateHurtSounds(cfg.get(ClientConfiguration.HURT_SOUNDS));
        updateVoicePitch(cfg.get(ClientConfiguration.VOICE_PITCH));
        updateHolidayThemes(cfg.get(ClientConfiguration.HOLIDAY_THEMES));

        //physics
        updateBreastPhysics(cfg.get(ClientConfiguration.BREAST_PHYSICS));
        updateShowBreastsInArmor(cfg.get(ClientConfiguration.SHOW_IN_ARMOR));
        updateArmorPhysicsOverride(cfg.get(ClientConfiguration.ARMOR_PHYSICS_OVERRIDE));
        updateBounceMultiplier(cfg.get(ClientConfiguration.BOUNCE_MULTIPLIER));
        updateFloppiness(cfg.get(ClientConfiguration.FLOPPY_MULTIPLIER));

        getBreasts().copyFrom(cfg);

        if (markForSync) {
            this.needsSync = true;
        }
    }

    /**
     * @deprecated Use {@link #loadFromDisk(boolean)} instead
     */
    @Deprecated
    public static PlayerConfig loadCachedPlayer(UUID uuid, boolean markForSync) {
        PlayerConfig plr = WildfireGender.getPlayerById(uuid);
        if (plr != null && plr.hasLocalConfig()) {
            plr.loadFromDisk(markForSync);
        }
        return plr;
    }

    /**
     * Save the settings stored in the provided {@link PlayerConfig} to the underlying {@link Configuration}, and then
     * {@link Configuration#save() attempt to save it to disk}.
     *
     * @param plr The {@link PlayerConfig} to save
     */
    public static void saveGenderInfo(PlayerConfig plr) {
        ClientConfiguration config = plr.getConfig();
        config.set(ClientConfiguration.USERNAME, plr.uuid);
        config.set(ClientConfiguration.GENDER, plr.getGender());
        config.set(ClientConfiguration.BUST_SIZE, plr.getBustSize());
        config.set(ClientConfiguration.HURT_SOUNDS, plr.hasHurtSounds());
        config.set(ClientConfiguration.VOICE_PITCH, plr.getVoicePitch());
        config.set(ClientConfiguration.HOLIDAY_THEMES, plr.hasHolidayThemes());

        //physics
        config.set(ClientConfiguration.BREAST_PHYSICS, plr.hasBreastPhysics());
        config.set(ClientConfiguration.SHOW_IN_ARMOR, plr.showBreastsInArmor());
        config.set(ClientConfiguration.ARMOR_PHYSICS_OVERRIDE, plr.getArmorPhysicsOverride());
        config.set(ClientConfiguration.BOUNCE_MULTIPLIER, plr.getBounceMultiplier());
        config.set(ClientConfiguration.FLOPPY_MULTIPLIER, plr.getFloppiness());

        config.set(ClientConfiguration.BREASTS_OFFSET_X, plr.getBreasts().getXOffset());
        config.set(ClientConfiguration.BREASTS_OFFSET_Y, plr.getBreasts().getYOffset());
        config.set(ClientConfiguration.BREASTS_OFFSET_Z, plr.getBreasts().getZOffset());
        config.set(ClientConfiguration.BREASTS_UNIBOOB, plr.getBreasts().isUniboob());
        config.set(ClientConfiguration.BREASTS_CLEAVAGE, plr.getBreasts().getCleavage());

        config.save();
        plr.needsSync = true;
        plr.needsCloudSync = true;
    }

    @Override
    public boolean hasJacketLayer() {
        throw new UnsupportedOperationException("PlayerConfig does not support #hasJacketLayer(); use Player#isModelPartShown instead");
    }

    @ApiStatus.Internal
    public void attemptCloudSync() {
        Minecraft client = Minecraft.getInstance();
        if (!GeneralClientConfig.INSTANCE.syncPlayerData.get()) {
            return;
        } else if (client.player == null || !this.uuid.equals(client.player.getUUID()) || !needsCloudSync || client.screen instanceof BaseWildfireScreen || CloudSync.syncOnCooldown()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                CloudSync.sync(this).join();
                WildfireGender.LOGGER.info("Synced player data to the cloud");
                SyncLog.add(WildfireLocalization.SYNC_LOG_SYNC_TO_CLOUD);
            } catch (Exception e) {
                WildfireGender.LOGGER.error("Failed to sync player data", e);
                SyncLog.add(WildfireLocalization.SYNC_LOG_FAILED_TO_SYNC_DATA);
            }
        });
        needsCloudSync = false;
    }

    /**
     * Update player data from the provided {@link JsonObject}
     *
     * @param json The {@link JsonObject} to merge with the existing config for this player
     *
     * @apiNote This method will set the player's {@link #getSyncStatus() sync status} to {@link SyncStatus#SYNCED}, as it's expected that this method is only used in
     * such cases where this would be applicable.
     */
    public void updateFromJson(@NotNull JsonObject json) {
        json.asMap().forEach(this.cfg.SAVE_VALUES::add);
        loadFromConfig(false);
        this.syncStatus = SyncStatus.SYNCED;
    }

    public enum SyncStatus {
        CACHED,
        SYNCED,
        UNKNOWN
    }
}

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
import com.wildfire.main.WildfireLocalization;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.cloud.SyncLog;
import com.wildfire.main.config.ConfigKey;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.Gender;
import com.wildfire.main.config.GlobalConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A version of {@link EntityConfig} backed by a {@link Configuration} for use with players
 */
public class PlayerConfig extends EntityConfig {

	public boolean needsSync;
	public boolean needsCloudSync;
	public SyncStatus syncStatus = SyncStatus.UNKNOWN;

	private final Configuration cfg;
	protected boolean hurtSounds = Configuration.HURT_SOUNDS.getDefault();
	protected boolean holidayThemes = Configuration.HOLIDAY_THEMES.getDefault();
	protected boolean armorPhysOverride = Configuration.ARMOR_PHYSICS_OVERRIDE.getDefault();
	protected boolean showBreastsInArmor = Configuration.SHOW_IN_ARMOR.getDefault();

	/**
	 * @deprecated Use {@link #updateGender(Gender)} instead
	 */
	@Deprecated
	public PlayerConfig(UUID uuid, Gender gender) {
		this(uuid);
		updateGender(gender);
	}

	public PlayerConfig(UUID uuid) {
		super(uuid);
		this.cfg = new Configuration(this.uuid.toString());
		this.cfg.set(Configuration.USERNAME, this.uuid);
		this.cfg.setDefault(Configuration.GENDER);
		this.cfg.setDefault(Configuration.BUST_SIZE);
		this.cfg.setDefault(Configuration.HURT_SOUNDS);

		this.cfg.setDefault(Configuration.BREASTS_OFFSET_X);
		this.cfg.setDefault(Configuration.BREASTS_OFFSET_Y);
		this.cfg.setDefault(Configuration.BREASTS_OFFSET_Z);
		this.cfg.setDefault(Configuration.BREASTS_UNIBOOB);
		this.cfg.setDefault(Configuration.BREASTS_CLEAVAGE);

		this.cfg.setDefault(Configuration.BREAST_PHYSICS);
		this.cfg.setDefault(Configuration.ARMOR_PHYSICS_OVERRIDE);
		this.cfg.setDefault(Configuration.SHOW_IN_ARMOR);
		this.cfg.setDefault(Configuration.BOUNCE_MULTIPLIER);
		this.cfg.setDefault(Configuration.FLOPPY_MULTIPLIER);
		this.cfg.setDefault(Configuration.VOICE_PITCH);

		this.cfg.setDefault(Configuration.HOLIDAY_THEMES);

		// Real players always have a UUID of version 4; if this isn't the case, then this is undeniably
		// an NPC player entity.
		if(uuid.version() != 4) this.holidayThemes = false;
	}

	// this shouldn't ever be called on players, but just to be safe, override with a noop.
	@Override
	public void readFromStack(@NotNull ItemStack chestplate) {}

	public Configuration getConfig() {
		return cfg;
	}

	private <VALUE> boolean updateValue(ConfigKey<VALUE> key, VALUE value, Consumer<VALUE> setter) {
		if (key.validate(value)) {
			setter.accept(value);
			return true;
		}
		return false;
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

	public boolean getArmorPhysicsOverride() {
		return armorPhysOverride;
	}

	public boolean updateArmorPhysicsOverride(boolean value) {
		return updateValue(Configuration.ARMOR_PHYSICS_OVERRIDE, value, v -> this.armorPhysOverride = v);
	}

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

	/**
	 * @deprecated Use {@link #toJson()} instead
	 */
	@Deprecated
	public static JsonObject toJsonObject(PlayerConfig plr) {
		return plr.toJson();
	}

	/**
	 * Returns a copy of the player's current configuration. Note that there are no guarantees of any values being valid
	 * (either type or number ranges), as this taken directly from the loaded JSON file, which may have been modified
	 * by the user.
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
		updateGender(cfg.get(Configuration.GENDER));
		updateBustSize(cfg.get(Configuration.BUST_SIZE));
		updateHurtSounds(cfg.get(Configuration.HURT_SOUNDS));
		updateVoicePitch(cfg.get(Configuration.VOICE_PITCH));
		updateHolidayThemes(cfg.get(Configuration.HOLIDAY_THEMES));

		//physics
		updateBreastPhysics(cfg.get(Configuration.BREAST_PHYSICS));
		updateShowBreastsInArmor(cfg.get(Configuration.SHOW_IN_ARMOR));
		updateArmorPhysicsOverride(cfg.get(Configuration.ARMOR_PHYSICS_OVERRIDE));
		updateBounceMultiplier(cfg.get(Configuration.BOUNCE_MULTIPLIER));
		updateFloppiness(cfg.get(Configuration.FLOPPY_MULTIPLIER));

		breasts.updateXOffset(cfg.get(Configuration.BREASTS_OFFSET_X));
		breasts.updateYOffset(cfg.get(Configuration.BREASTS_OFFSET_Y));
		breasts.updateZOffset(cfg.get(Configuration.BREASTS_OFFSET_Z));
		breasts.updateUniboob(cfg.get(Configuration.BREASTS_UNIBOOB));
		breasts.updateCleavage(cfg.get(Configuration.BREASTS_CLEAVAGE));

		if(markForSync) {
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
	 * Save the settings stored in the provided {@link PlayerConfig} to the underlying {@link Configuration},
	 * and then {@link Configuration#save() attempt to save it to disk}.
	 *
	 * @param plr The {@link PlayerConfig} to save
	 */
	public static void saveGenderInfo(PlayerConfig plr) {
		Configuration config = plr.getConfig();
		config.set(Configuration.USERNAME, plr.uuid);
		config.set(Configuration.GENDER, plr.getGender());
		config.set(Configuration.BUST_SIZE, plr.getBustSize());
		config.set(Configuration.HURT_SOUNDS, plr.hasHurtSounds());
		config.set(Configuration.VOICE_PITCH, plr.getVoicePitch());
		config.set(Configuration.HOLIDAY_THEMES, plr.hasHolidayThemes());

		//physics
		config.set(Configuration.BREAST_PHYSICS, plr.hasBreastPhysics());
		config.set(Configuration.SHOW_IN_ARMOR, plr.showBreastsInArmor());
		config.set(Configuration.ARMOR_PHYSICS_OVERRIDE, plr.getArmorPhysicsOverride());
		config.set(Configuration.BOUNCE_MULTIPLIER, plr.getBounceMultiplier());
		config.set(Configuration.FLOPPY_MULTIPLIER, plr.getFloppiness());

		config.set(Configuration.BREASTS_OFFSET_X, plr.getBreasts().getXOffset());
		config.set(Configuration.BREASTS_OFFSET_Y, plr.getBreasts().getYOffset());
		config.set(Configuration.BREASTS_OFFSET_Z, plr.getBreasts().getZOffset());
		config.set(Configuration.BREASTS_UNIBOOB, plr.getBreasts().isUniboob());
		config.set(Configuration.BREASTS_CLEAVAGE, plr.getBreasts().getCleavage());

		config.save();
		plr.needsSync = true;
		plr.needsCloudSync = true;
	}

	@Override
	public boolean hasJacketLayer() {
		throw new UnsupportedOperationException("PlayerConfig does not support #hasJacketLayer(); use PlayerEntity#isPartVisible instead");
	}

	@ApiStatus.Internal
	public void attemptCloudSync() {
		var client = MinecraftClient.getInstance();
		if(client.player == null || !this.uuid.equals(client.player.getUuid())) return;
		if(!needsCloudSync) return;
		if(client.currentScreen instanceof BaseWildfireScreen) return;
		if(!GlobalConfig.INSTANCE.get(GlobalConfig.AUTOMATIC_CLOUD_SYNC)) return;
		if(CloudSync.syncOnCooldown()) return;

		CompletableFuture.runAsync(() -> {
			try {
				CloudSync.sync(this).join();
				WildfireGender.LOGGER.info("Synced player data to the cloud");
				SyncLog.add(WildfireLocalization.SYNC_LOG_SYNC_TO_CLOUD);
			} catch(Exception e) {
				WildfireGender.LOGGER.error("Failed to sync player data", e);
				SyncLog.add(WildfireLocalization.SYNC_LOG_FAILED_TO_SYNC_DATA);
			}
		});
		needsCloudSync = false;
	}

	/**
	 * Update player data from the provided {@link JsonObject}
	 *
	 * @apiNote This method will set the player's {@link #getSyncStatus() sync status} to {@link SyncStatus#SYNCED},
	 *          as it's expected that this method is only used in such cases where this would be applicable.
	 *
	 * @param json The {@link JsonObject} to merge with the existing config for this player
	 */
	public void updateFromJson(@NotNull JsonObject json) {
		json.asMap().forEach(this.cfg.SAVE_VALUES::add);
		loadFromConfig(false);
		this.syncStatus = SyncStatus.SYNCED;
	}

	public enum SyncStatus {
		CACHED, SYNCED, UNKNOWN
	}
}

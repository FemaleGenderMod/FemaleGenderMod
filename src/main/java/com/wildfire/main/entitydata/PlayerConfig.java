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
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.config.types.ConfigKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
		cfg = new Configuration(uuid.toString());
		cfg.setDefaults();

		// Real players always have a UUID of version 4; if this isn't the case, then this is undeniably
		// an NPC player entity.
		if(uuid.version() != 4) holidayThemes = false;
	}

	// these shouldn't ever be called on players, but just to be safe, override with a noop.
	@Override
	public void readFromStack(@NotNull ItemStack chestplate) {
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

	/**
	 * @apiNote The value this method returns has been moved to {@link ClientConfig}, and this method is only
	 * 			retained for compatibility with mods that use this as a mixin target.
	 */
	@ApiStatus.Obsolete
	@Environment(EnvType.CLIENT)
	public boolean getArmorPhysicsOverride() {
		return ClientConfig.INSTANCE.get(ClientConfig.ARMOR_PHYSICS_OVERRIDE);
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
	 * Returns a copy of the player's current configuration; the stored values are guaranteed to be valid for
	 * the associated {@link ConfigKey}, and does not include any unrecognized keys.
	 *
	 * @return A new copy of the player's {@link JsonObject saved config values}
	 */
	public JsonObject toJson() {
		var json = new JsonObject();
		Configuration.KEYS.forEach(key -> key.dump(this, json));
		return json;
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
		Configuration.KEYS.forEach(key -> key.writeToPlayer(this));
		if(markForSync) {
			this.needsSync = true;
		}
	}

	/**
	 * Write all known {@link ConfigKey}s from this {@link PlayerConfig} to the underlying {@link Configuration}
	 */
	public void writeToConfig() {
		Configuration.KEYS.forEach(key -> key.writeToConfig(this));
	}

	/**
	 * Saves the settings stored in this {@link PlayerConfig} to the underlying {@link Configuration},
	 * and then attempts to {@link Configuration#save() save to disk}.
	 */
	public void save() {
		writeToConfig();
		getConfig().save();
		needsSync = true;
		needsCloudSync = true;
	}

	/**
	 * @deprecated Use {@code plr.save()} instead
	 */
	@Deprecated(forRemoval = true)
	public static void saveGenderInfo(PlayerConfig plr) {
		plr.save();
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
		if(!ClientConfig.INSTANCE.get(ClientConfig.AUTOMATIC_CLOUD_SYNC)) return;
		if(CloudSync.syncOnCooldown()) return;

		CompletableFuture.runAsync(() -> {
			try {
				CloudSync.sync(this).join();
				WildfireGender.LOGGER.info("Synced player data to the cloud");
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
		CACHED, SYNCED, UNKNOWN
	}
}

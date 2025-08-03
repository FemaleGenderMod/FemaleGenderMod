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

package com.wildfire.main;

import com.google.gson.JsonObject;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.cloud.ContributorNametag;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.WildfireSync;
import com.wildfire.resources.GenderArmorResourceManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Environment(EnvType.CLIENT)
public class WildfireGenderClient implements ClientModInitializer {
	private static final Executor LOAD_EXECUTOR = Util.getIoWorkerExecutor().named("wildfire_gender$loadPlayerData");
	// TODO merge WildfireGender.CONTRIBUTOR_UUIDS into this?
	public static final CompletableFuture<Map<UUID, ContributorNametag>> CONTRIBUTOR_NAMETAGS = CloudSync.getContributors();

	@Override
	public void onInitializeClient() {
		WildfireSounds.register();
		WildfireSync.registerClient();
		WildfireEventHandler.registerClientEvents();
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(GenderArmorResourceManager.INSTANCE);
	}

	public static CompletableFuture<@Nullable PlayerConfig> loadGenderInfo(UUID uuid, boolean markForSync, boolean bypassQueue) {
		var cache = WildfireGender.getPlayerById(uuid);
		if(cache == null) {
			return CompletableFuture.completedFuture(null);
		}
		return loadGenderInfo(cache, markForSync, bypassQueue);
	}

	public static CompletableFuture<@NotNull PlayerConfig> loadGenderInfo(PlayerConfig player, boolean markForSync, boolean bypassQueue) {
		return CompletableFuture.supplyAsync(() -> {
			var uuid = player.uuid;
			if(player.hasLocalConfig()) {
				player.loadFromDisk(markForSync);
			} else if(player.syncStatus == PlayerConfig.SyncStatus.UNKNOWN) {
				JsonObject data;
				try {
					var future = bypassQueue ? CloudSync.getProfile(uuid) : CloudSync.queueFetch(uuid);
					data = future.join();
				} catch(Exception e) {
					WildfireGender.LOGGER.error("Failed to fetch profile from sync server", e);
					throw e;
				}
				// make sure the server we're connected to hasn't provided player data while we were fetching data from
				// the sync server
				if(data != null && player.syncStatus == PlayerConfig.SyncStatus.UNKNOWN) {
					player.updateFromJson(data);
					if(markForSync) {
						player.needsSync = true;
					}
				}
			}
			return player;
		}, LOAD_EXECUTOR);
	}

	public static @Nullable Text getNametag(UUID uuid) {
		var clientPlayer = MinecraftClient.getInstance().player;
		if(ClientConfig.INSTANCE.get(ClientConfig.HIDE_OWN_CONTRIBUTOR_TAG) && clientPlayer != null && uuid.equals(clientPlayer.getUuid())) {
			return null;
		}

		ContributorNametag custom;
		try {
			custom = WildfireGenderClient.CONTRIBUTOR_NAMETAGS.getNow(Map.of()).get(uuid);
		} catch(Exception e) {
			custom = null;
		}

		if(custom != null) {
			return custom.asText();
		} else if(WildfireGender.CREATOR_UUID.equals(uuid)) {
			return Text.translatable("wildfire_gender.nametag.creator").formatted(Formatting.LIGHT_PURPLE);
		} else if(WildfireGender.CONTRIBUTOR_UUIDS.contains(uuid)) {
			return Text.translatable("wildfire_gender.nametag.contributor").formatted(Formatting.GOLD);
		}

		return null;
	}
}

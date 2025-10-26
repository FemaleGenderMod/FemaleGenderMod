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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.WildfireSync;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.UUID;

public class WildfireGender implements ModInitializer {
	public static final String MODID = "wildfire_gender";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final LoadingCache<UUID, PlayerConfig> CACHE;

	static {
		var builder = CacheBuilder.newBuilder();
		// Only automatically expire cache entries on the client; a server may go a decent while without accessing
		// the player cache, and we can't easily re-cache a player's settings on a server, while a client
		// will typically either receive settings from the server in a sync, or simply re-fetch from
		// a local config file or from the cloud.
		// Note that servers will manually invalidate cache entries upon a player disconnecting
		// (see WildfireEventHandler#playerDisconnected).
		if(WildfireHelper.onClient()) {
			builder.expireAfterAccess(Duration.ofMinutes(15));
		}
		CACHE = builder.build(CacheLoader.from(key -> {
			var config = new PlayerConfig(key);
			// only attempt to load player data on the client
			if(WildfireHelper.onClient()) {
				// markForSync being true will only ever do anything for the client player
				WildfireGenderClient.loadGenderInfo(config, true, false);
			}
			return config;
		}));
	}

	@Override
	public void onInitialize() {
		WildfireSync.register();
		WildfireEventHandler.registerCommonEvents();
	}

	public static @Nullable PlayerConfig getPlayerById(UUID id) {
		return CACHE.getIfPresent(id);
	}

	public static @NotNull PlayerConfig getOrAddPlayerById(UUID id) {
		return CACHE.getUnchecked(id);
	}

	public static Identifier id(String path) {
		return Identifier.of(MODID, path);
	}
}

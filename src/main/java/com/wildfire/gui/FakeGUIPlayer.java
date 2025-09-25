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

package com.wildfire.gui;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.mixins.accessors.ClientMannequinEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientMannequinEntity;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class FakeGUIPlayer {

	private final String name;
	private final UUID uuid;
	private final Supplier<GUIMannequin> entity;

	private final String devTitle;
	private final String description;

	public FakeGUIPlayer(final String name, final UUID uuid, final String devTitle, final String description, @Nullable final JsonObject genderData) {
		this.name = name;
		this.uuid = uuid;
		this.entity = createPlayerSupplier(uuid, genderData);
		this.devTitle = devTitle;
		this.description = description;
	}

	public ClientMannequinEntity getEntity() {
		return entity.get();
	}

	public UUID getUUID() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public String getDevTitle() {
		return devTitle;
	}

	public String getDescription() {
		return description;
	}

	public void tick() {
		entity.get().applyLoadedSkin();
		entity.get().age++; // This allows for playing the breathing animation
		EntityConfig.getEntity(getEntity()).tickBreastPhysics(getEntity());
	}

	private static Supplier<GUIMannequin> createPlayerSupplier(final UUID uuid, final JsonObject genderData) {
		return Suppliers.memoize(() -> {
			var client = MinecraftClient.getInstance();
			assert client.world != null;

			var entity = new GUIMannequin(client.world, client.getPlayerSkinCache(), ProfileComponent.ofDynamic(uuid));

			try {
				// while we don't have proper support for mannequins right now, we can most certainly fake it
				PlayerConfig config = (PlayerConfig) EntityConfig.CACHE.get(entity.getUuid(), () -> new PlayerConfig(entity.getUuid()));
				config.forceSimplifiedPhysics = true;
				CloudSync.getProfile(uuid, true).thenAccept(json -> {
					if(json != null) {
						config.updateFromJson(json);
					} else {
						//fallback,
						config.updateFromJson(genderData);
					}
				});
			} catch(ExecutionException | ClassCastException ignored) {
			}

			return entity;
		});
	}

	private static class GUIMannequin extends ClientMannequinEntity {
		private final ProfileComponent copySkinFrom;

		public GUIMannequin(World world, PlayerSkinCache skinCache, ProfileComponent copySkinFrom) {
			super(world, skinCache);
			this.copySkinFrom = copySkinFrom;
			// this is being done as opposed to using data tracker to force a refresh to avoid interfering
			// with other mods that might be injecting into the data tracker update methods to know
			// when real entities in the world are updated
			((ClientMannequinEntityAccessor) this).invokeRefreshSkin();
		}

		public void applyLoadedSkin() {
			var accessor = (ClientMannequinEntityAccessor) this;
			var skinLookup = accessor.getSkinLookup();
			if(skinLookup != null && skinLookup.isDone()) {
				try {
					skinLookup.get().ifPresent(accessor::invokeSetSkin);
					accessor.setSkinLookup(null);
				} catch(Exception ignored) {
				}
			}
		}

		@Override
		protected ProfileComponent getMannequinProfile() {
			return copySkinFrom;
		}
	}
}

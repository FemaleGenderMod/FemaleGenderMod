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
import com.wildfire.main.WildfireGender;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.contributors.Contributor;
import com.wildfire.main.contributors.Contributors;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.mixins.accessors.ClientMannequinAccessor;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FakeGUIPlayer {

    private final String name;
    private final UUID uuid;
    private final Supplier<GUIMannequin> entity;
    private final @Nullable String description;

    public FakeGUIPlayer(String name, UUID uuid, @Nullable String description, @Nullable JsonObject defaultGenderSettings) {
        this.name = name;
        this.uuid = uuid;
        this.entity = createPlayerSupplier(uuid, defaultGenderSettings);
        this.description = description;
    }

    public FakeGUIPlayer(String name, UUID uuid, @Nullable JsonObject defaultGenderSettings) {
        this(name, uuid, null, defaultGenderSettings);
    }

    public ClientMannequin getEntity() {
        return entity.get();
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public @Nullable Contributor.Role getRole() {
        return Contributors.getRole(uuid);
    }

    public Contributor.Role getRoleOrGeneric() {
        var role = getRole();
        return role == null ? Contributor.Role.GENERIC : role;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void tick() {
        entity.get().applyLoadedSkin();
        entity.get().tickCount++; // This allows for playing the breathing animation
        EntityConfig.getEntity(getEntity()).tickBreastPhysics(getEntity());
    }

    private static Supplier<GUIMannequin> createPlayerSupplier(final UUID uuid, final @Nullable JsonObject defaultGenderData) {
        return Suppliers.memoize(() -> {
            var client = Minecraft.getInstance();
            assert client.level != null;

            var entity = new GUIMannequin(client.level, client.playerSkinRenderCache(), ResolvableProfile.createUnresolved(uuid));

            PlayerConfig config;
            try {
                // while we don't have proper support for mannequins right now, we can most certainly fake it
                config = (PlayerConfig) EntityConfig.CACHE.get(entity.getUUID(), () -> new PlayerConfig(entity.getUUID()));
            } catch(ExecutionException | ClassCastException _) {
                return entity;
            }

            config.forceSimplifiedPhysics = true;

            var cached = WildfireGender.getPlayerById(uuid);
            if(cached == null) {
                CloudSync.getProfile(uuid, true).thenAccept(json -> {
                    if(json != null) {
                        config.updateFromJson(json);
                    } else if(defaultGenderData != null) {
                        config.updateFromJson(defaultGenderData);
                    }
                });
            } else {
                config.updateFromJson(cached.toJson());
            }

            return entity;
        });
    }

    private static class GUIMannequin extends ClientMannequin {
        private final ResolvableProfile copySkinFrom;

        public GUIMannequin(Level world, PlayerSkinRenderCache skinCache, ResolvableProfile copySkinFrom) {
            super(world, skinCache);
            this.copySkinFrom = copySkinFrom;
            // this is being done as opposed to using data tracker to force a refresh to avoid interfering
            // with other mods that might be injecting into the data tracker update methods to know
            // when real entities in the world are updated
            ((ClientMannequinAccessor) this).invokeUpdateSkin();
            // workaround for #getId() throwing an error if an id isn't set on 26.2+, which results in the game crashing
            // when attempting to extract the render state for one of these mannequins.
            // the id here doesn't matter given this entity is never spawned in the world, so just set some arbitrary id.
            // the proper fix would be to extract the render state ourselves, but doing so would make keeping up with
            // updates more complex when we could just take the quick and easy way out.
            this.setId(1);
        }

        public void applyLoadedSkin() {
            var accessor = (ClientMannequinAccessor) this;
            var skinLookup = accessor.getSkinLookup();
            if(skinLookup != null && skinLookup.isDone()) {
                try {
                    skinLookup.get().ifPresent(accessor::invokeSetSkin);
                    accessor.setSkinLookup(null);
                } catch(Exception _) {
                }
            }
        }

        @Override
        public ResolvableProfile getProfile() {
            return copySkinFrom;
        }
    }
}

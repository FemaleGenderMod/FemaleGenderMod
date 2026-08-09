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
import com.wildfire.main.WildfireGender;
import com.wildfire.main.cloud.CloudSync;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.contributors.Contributor;
import com.wildfire.main.contributors.Contributor.Role;
import com.wildfire.main.contributors.Contributors;
import com.wildfire.main.entitydata.EntityConfigHolder;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.entitydata.PlayerConfigHolder;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class FakeGUIPlayer {

    public static final Consumer<PlayerConfig> FEMALE_CHANGES = config -> {
        //The settings that need to be changed away from default
        config.gender.update(Gender.FEMALE);
        config.breasts.yOffset().update(-0.2F);
        config.breasts.physics().uniboob().update(false);
        config.breasts.cleavage().update(0.05F);
        config.holidayThemes.update(false);
    };

    private final String name;
    private final UUID uuid;
    private final Supplier<GUIMannequin> entity;
    private final @Nullable Component description;

    public FakeGUIPlayer(String name, UUID uuid, @Nullable Component description, @Nullable Consumer<PlayerConfig> defaultGenderSettings) {
        this.name = name;
        this.uuid = uuid;
        this.entity = createPlayerSupplier(this.uuid, this.name, defaultGenderSettings);
        this.description = description;
    }

    public FakeGUIPlayer(String name, UUID uuid, @Nullable Consumer<PlayerConfig> defaultGenderSettings) {
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

    public Contributor.@Nullable Role getRole() {
        return Contributors.getRole(uuid);
    }

    public Contributor.Role getRoleOrGeneric() {
        Role role = getRole();
        return role == null ? Contributor.Role.GENERIC : role;
    }

    public @Nullable Component getDescription() {
        return description;
    }

    public void tick() {
        entity.get().applyLoadedSkin();
        entity.get().tickCount++; // This allows for playing the breathing animation
        EntityConfigHolder.getEntity(getEntity()).tickBreastPhysics(getEntity());
    }

    @SuppressWarnings("NullableProblems")
    private static Supplier<GUIMannequin> createPlayerSupplier(final UUID uuid, final String name, final @Nullable Consumer<PlayerConfig> defaultGenderData) {
        return Suppliers.memoize(() -> {
            var client = Minecraft.getInstance();
            assert client.level != null;

            var entity = new GUIMannequin(client.level, client.playerSkinRenderCache(), ResolvableProfile.createUnresolved(uuid));
            //Set the custom name in case it is relevant for player rendering (for example Dinnerbone or Grumm).
            // As it is possible a mod adds other names to render upside down, so we might be as compatible as possible
            entity.setCustomName(Component.literal(name));

            PlayerConfigHolder config;
            try {
                // while we don't have proper support for mannequins right now, we can most certainly fake it
                config = (PlayerConfigHolder) EntityConfigHolder.CACHE.get(entity.getUUID(), () -> new PlayerConfigHolder(entity.getUUID()));
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
                        //Apply changes compared to the default configs
                        defaultGenderData.accept(config.config());
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
            updateSkin();
            // workaround for #getId() throwing an error if an id isn't set on 26.2+, which results in the game crashing
            // when attempting to extract the render state for one of these mannequins.
            // the id here doesn't matter given this entity is never spawned in the world, so just set some arbitrary id.
            // the proper fix would be to extract the render state ourselves, but doing so would make keeping up with
            // updates more complex when we could just take the quick and easy way out.
            this.setId(1);
        }

        public void applyLoadedSkin() {
            //From super.tick, except without the rest of the side effects of tick, and without logging when it failed to look up the skin
            if (this.skinLookup != null && this.skinLookup.isDone()) {
                try {
                    this.skinLookup.get().ifPresent(this::setSkin);
                    this.skinLookup = null;
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

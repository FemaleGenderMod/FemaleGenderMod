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

package com.wildfire.client.gui;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.wildfire.api.Gender;
import com.wildfire.api.client.WildfireClientAPI;
import com.wildfire.client.cloud.CloudSync;
import com.wildfire.client.contributors.Contributor.Role;
import com.wildfire.client.contributors.Contributors;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.entities.avatars.AvatarConfig;
import com.wildfire.common.entities.players.PlayerConfigHolder;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class FakeGUIPlayer {

    public static final Consumer<AvatarConfig> FEMALE_CHANGES = config -> {
        //The settings that need to be changed away from default
        config.gender.update(Gender.FEMALE);
        config.breasts.yOffset().update(-0.2F);
        config.breasts.physics().uniboob().update(false);
        config.breasts.cleavage().update(0.05F);
    };

    /// @return `true` if the given entity is one of the fake players rendered inside GUIs
    public static boolean isFakePlayer(net.minecraft.world.entity.Entity entity) {
        return entity instanceof GUIPlayer;
    }

    /// @return The config belonging to the given fake player, or `null` if the entity is not one
    public static @Nullable PlayerConfigHolder getConfigOf(net.minecraft.world.entity.Entity entity) {
        return entity instanceof GUIPlayer gui ? gui.wildfireConfig : null;
    }

    private final String name;
    private final UUID uuid;
    private final Supplier<GUIPlayer> entity;
    private final @Nullable Component description;

    public FakeGUIPlayer(String name, UUID uuid, @Nullable Component description, @Nullable Consumer<AvatarConfig> defaultGenderSettings) {
        this.name = name;
        this.uuid = uuid;
        this.entity = createPlayerSupplier(this.uuid, this.name, defaultGenderSettings);
        this.description = description;
    }

    public FakeGUIPlayer(String name, UUID uuid, @Nullable Consumer<AvatarConfig> defaultGenderSettings) {
        this(name, uuid, null, defaultGenderSettings);
    }

    public RemotePlayer getEntity() {
        return entity.get();
    }

    /// The config associated with this fake player; unlike real players, this is not part of any shared cache
    public PlayerConfigHolder getConfig() {
        return entity.get().wildfireConfig;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public @Nullable Role getRole() {
        return Contributors.getRole(uuid);
    }

    public Role getRoleOrGeneric() {
        Role role = getRole();
        return role == null ? Role.GENERIC : role;
    }

    public @Nullable Component getDescription() {
        return description;
    }

    public void tick() {
        GUIPlayer entity = this.entity.get();
        entity.tickCount++; // This allows for playing the breathing animation
        var cfg = getConfig();
        cfg.breastPhysics().tick(entity);
    }

    @SuppressWarnings("NullableProblems")
    private static Supplier<GUIPlayer> createPlayerSupplier(final UUID uuid, final String name, final @Nullable Consumer<AvatarConfig> defaultGenderData) {
        return Suppliers.memoize(() -> {
            var client = Minecraft.getInstance();
            assert client.level != null;

            var entity = new GUIPlayer(client.level, new GameProfile(uuid, name));
            //Set the custom name in case it is relevant for player rendering (for example Dinnerbone or Grumm).
            // As it is possible a mod adds other names to render upside down, so we might be as compatible as possible
            entity.setCustomName(Component.literal(name));

            PlayerConfigHolder config = entity.wildfireConfig;
            config.forceSimplifiedPhysics = true;

            var cached = WildfireClientAPI.players().get(uuid);
            if(cached == null) {
                CompletableFuture.runAsync(() -> loadFromCloud(uuid, config, defaultGenderData));
            } else {
                config.updateFromJson(cached.toJson());
            }

            return entity;
        });
    }

    private static void loadFromCloud(final UUID uuid, final PlayerConfigHolder holder, final @Nullable Consumer<AvatarConfig> defaults) {
        JsonObject profile;
        try {
            profile = CloudSync.getProfile(uuid, true).join();
        } catch(Exception e) {
            WildfireGender.LOGGER.warn("Failed to load contributor data from cloud sync", e);
            profile = null;
        }

        if(profile != null) {
            holder.updateFromJson(profile);
        } else if(defaults != null) {
            //Apply changes compared to the default configs
            defaults.accept(holder.config());
        }
    }

    private static class GUIPlayer extends RemotePlayer {
        private final PlayerConfigHolder wildfireConfig;
        // 26.1 的 PlayerSkinRenderCache 等效：离线 profile 没有 textures 属性，
        // getInsecureSkin 永远拿默认皮肤，必须主动向 session 服务器 fetch 完整 profile
        private volatile PlayerSkin resolvedSkin;

        public GUIPlayer(ClientLevel level, GameProfile profile) {
            super(level, profile);
            this.wildfireConfig = new PlayerConfigHolder(profile.getId());
            //The model part overlay flags (hat/jacket/sleeves/pants) default to zero, and are normally
            // set from the client information sent by the server - fake players need to enable them manually
            this.entityData.set(Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte)0x7F);
            CompletableFuture.supplyAsync(() ->
                    Minecraft.getInstance().getMinecraftSessionService().fetchProfile(profile.getId(), true), Util.ioPool())
                .thenCompose(result -> {
                    if (result == null || result.profile() == null) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return Minecraft.getInstance().getSkinManager().getOrLoad(result.profile());
                })
                .thenAccept(skin -> {
                    if (skin != null) {
                        resolvedSkin = skin;
                    }
                })
                .exceptionally(e -> {
                    WildfireGender.LOGGER.warn("Failed to fetch skin for fake player {}", profile.getName(), e);
                    return null;
                });
        }

        @Override
        public PlayerSkin getSkin() {
            PlayerSkin skin = resolvedSkin;
            if (skin != null) {
                return skin;
            }
            //Note: We can't rely on the default implementation as there is no PlayerInfo entry for this fake player,
            // so we instead directly resolve the skin out of the skin manager's insecure cache
            return Minecraft.getInstance().getSkinManager().getInsecureSkin(getGameProfile());
        }
    }
}

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

package com.wildfire.common.entitydata;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.serialization.JsonOps;
import com.wildfire.client.cloud.CloudSync;
import com.wildfire.common.WildfireGender;
import com.wildfire.common.config.enums.Gender;
import com.wildfire.common.config.value.ConfigValue;
import com.wildfire.common.config.uvs.UVs;
import com.wildfire.client.physics.BothBreastsPhysics;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public class EntityConfigHolder<CONFIG extends EntityConfig> {

    public static final LoadingCache<UUID, EntityConfigHolder<? extends EntityConfig>> CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(5))
        //Note: Theoretically this can never fail so it is safe to use getOrThrow as everything in the codec has orElse(default)
        .build(CacheLoader.from(uuid -> new EntityConfigHolder<>(uuid, EntityConfig.CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()).getOrThrow())));

    /// Get the configuration for a given entity
    ///
    /// @apiNote Configuration settings for [PlayerConfig]s may not be immediately available upon being
    ///          returned, and may take several seconds to be populated if loaded from the
    ///          [`cloud sync server`][CloudSync].
    ///
    /// @return The relevant [EntityConfig], or [PlayerConfig] if given a [`player`][Player]
    public static EntityConfigHolder<? extends EntityConfig> getEntity(LivingEntity entity) {
        if(entity instanceof Player) {
            return WildfireGender.getOrAddPlayerById(entity.getUUID());
        }
        return CACHE.getUnchecked(entity.getUUID());
    }

    public final UUID uuid;
    // TODO ideally this physics object would be made entirely client-sided, but this class is
    //      used on both the client and server (primarily through PlayerConfigHolder), making it very
    //      difficult to do so without some major changes to split this up further into a common class
    //      with a client extension class (e.g. the PlayerEntity & AbstractClientPlayerEntity classes)
    protected final BothBreastsPhysics breastPhysics;

    protected boolean jacketLayer = true;
    protected @Nullable BreastDataComponent fromComponent;

    @ApiStatus.Internal
    public boolean forceSimplifiedPhysics = false;

    protected CONFIG config;

    protected EntityConfigHolder(UUID uuid, CONFIG config) {
        this.uuid = uuid;
        this.breastPhysics = new BothBreastsPhysics(this);
        this.config = config;
    }

    public CONFIG config() {
        return config;
    }

    /// Copy gender settings included in the given [`item NBT`][ItemStack] to the current entity
    ///
    /// @see BreastDataComponent
    public void readFromStack(ItemStack chestplate) {
        CustomData component = chestplate.get(DataComponents.CUSTOM_DATA);
        if (chestplate.isEmpty() || component == null) {
            this.fromComponent = null;
            gender().update(Gender.MALE);
            return;
        } else if(fromComponent != null && Objects.equals(component, fromComponent.nbtComponent())) {
            // nothing's changed since the last time we checked, so there's no need to read from the
            // underlying nbt tag again
            return;
        }

        fromComponent = BreastDataComponent.fromComponent(component);
        if (fromComponent == null) {
            gender().update(Gender.MALE);
            return;
        }

        breasts().updateFromComponent(fromComponent);
        gender().update(breasts().bustSize().get() >= 0.02f ? Gender.FEMALE : Gender.MALE);
        this.jacketLayer = fromComponent.jacket();
    }

    /// Only used in the case of [`armor stands`][ArmorStand]; returns `true` if the player who equipped
    /// the armor stand's chestplate has their jacket layer visible.
    public boolean hasJacketLayer() {
        return jacketLayer;
    }

    public BothBreastsPhysics breastPhysics() {
        return breastPhysics;
    }

    public List<String> getDebugInfo() {
        List<String> info = breasts().getDebugInfo();
        info.addFirst("Gender: " + switch(gender().get()) {
            case FEMALE -> ChatFormatting.LIGHT_PURPLE + "Female";
            case MALE -> ChatFormatting.BLUE + "Male";
            case OTHER -> ChatFormatting.GREEN + "Other";
        });
        return info;
    }

    @Override
    public String toString() {
        return "%s(uuid=%s, gender=%s)".formatted(getClass().getCanonicalName(), uuid, gender());
    }

    // Bouncer methods for config values that act upon the current config instance

    public final ConfigValue<Gender> gender() {
        return config.gender;
    }
    public Breasts breasts() {
        return config.breasts;
    }
    public final UVs uvs() {
        return config.uvs;
    }
}

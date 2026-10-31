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

package com.wildfire.api.impl;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.wildfire.api.EntityCache;
import com.wildfire.common.entities.EntityConfig;
import com.wildfire.common.entities.EntityConfigHolder;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public class EntityCacheImpl<TYPE extends EntityConfigHolder<? extends EntityConfig>, ENTITY extends LivingEntity> implements EntityCache<TYPE, ENTITY> {
    private final LoadingCache<UUID, TYPE> cache;

    public EntityCacheImpl(final CacheLoader<UUID, TYPE> loader, final @Nullable Duration expiryTime) {
        this.cache = Util.make(CacheBuilder.newBuilder(), builder -> {
            if(expiryTime != null) {
                builder.expireAfterAccess(expiryTime);
            }
        }).build(loader);
    }

    public EntityCacheImpl(final Function<UUID, TYPE> constructor, final @Nullable Duration expiryTime) {
        this(CacheLoader.from(constructor), expiryTime);
    }

    public EntityCacheImpl(final Function<UUID, TYPE> constructor) {
        this(constructor, null);
    }

    @Override
    public @Nullable TYPE get(final UUID uuid) {
        return cache.getIfPresent(uuid);
    }

    @Override
    public TYPE getOrCreate(final UUID uuid) {
        return cache.getUnchecked(uuid);
    }

    @Override
    public void invalidate(final UUID uuid) {
        cache.invalidate(uuid);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    @Override
    public void invalidateIf(final Predicate<TYPE> predicate) {
        cache.asMap().values().removeIf(predicate);
    }

    @ApiStatus.Internal
    public LoadingCache<UUID, TYPE> cache() {
        return cache;
    }
}

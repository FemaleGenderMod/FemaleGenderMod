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

package com.wildfire.api;

import com.wildfire.common.entities.EntityConfig;
import com.wildfire.common.entities.EntityConfigHolder;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public interface EntityCache<TYPE extends EntityConfigHolder<? extends EntityConfig>, ENTITY extends LivingEntity> {
    default @Nullable TYPE get(final ENTITY entity) {
        return get(entity.getUUID());
    }

    default TYPE getOrCreate(final ENTITY entity) {
        return getOrCreate(entity.getUUID());
    }

    @Nullable TYPE get(UUID uuid);
    TYPE getOrCreate(UUID uuid);

    @ApiStatus.Internal
    default void invalidate(final ENTITY entity) {
        invalidate(entity.getUUID());
    }

    @ApiStatus.Internal
    void invalidate(UUID uuid);

    @ApiStatus.Internal
    void invalidateAll();

    @ApiStatus.Internal
    void invalidateIf(Predicate<TYPE> predicate);
}

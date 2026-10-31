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

package com.wildfire.common.entities;

import com.wildfire.api.Gender;
import com.wildfire.common.config.value.ConfigValue;
import com.wildfire.common.config.UVs;
import com.wildfire.client.physics.BothBreastsPhysics;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;
import net.minecraft.ChatFormatting;

/// Holds and provides an entity's [EntityConfig]
///
/// The [`provided configuration`][#config()] should not be assumed to be persistent, as the instance
/// stored on any of these holder objects may be replaced at any point (such as from a sync packet).
///
/// @param <CONFIG> The [EntityConfig] type supplied by this holder
public abstract class EntityConfigHolder<CONFIG extends EntityConfig> {
    public final UUID uuid;
    // TODO ideally this physics object would be made entirely client-sided, but this class is
    //      used on both the client and server (primarily through PlayerConfigHolder), making it very
    //      difficult to do so without some major changes to split this up further into a common class
    //      with a client extension class (e.g. the PlayerEntity & AbstractClientPlayerEntity classes)
    protected final BothBreastsPhysics breastPhysics;

    @ApiStatus.Internal
    public boolean forceSimplifiedPhysics = false;

    protected CONFIG config;

    protected EntityConfigHolder(UUID uuid, CONFIG config) {
        this.uuid = uuid;
        this.breastPhysics = new BothBreastsPhysics(this);
        this.config = config;
    }

    /// @return The current [entity configuration][CONFIG]
    public CONFIG config() {
        return config;
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

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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireGenderClient;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public final class WildfireAPI {

    private static final Map<Item, IGenderArmor> GENDER_ARMORS = new HashMap<>();

    private static final Codec<Vector2ic> VEC2I_LEGACY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(Vector2ic::x),
            Codec.INT.fieldOf("y").forGetter(Vector2ic::y)
    ).apply(instance, Vector2i::new));

    /* package-private */ static final Codec<Vector2ic> VECTOR_2I_CODEC = Codec.withAlternative(Codec.INT_STREAM.comapFlatMap(
            stream -> Util.fixedSize(stream, 2).map(Vector2i::new),
            vec2i -> IntStream.of(vec2i.x(), vec2i.y())
    ), VEC2I_LEGACY_CODEC);

    /**
     * Get the cached config for a {@link Player}
     *
     * @apiNote This method will not load a player's config if they aren't already cached, and will only return
     *		  the config of players the mod has already loaded.
     *
     * @param  uuid  the uuid of the target {@link Player}
     * @see	PlayerConfig
     */
    public static @Nullable PlayerConfig getPlayerById(UUID uuid) {
        return WildfireGender.getPlayerById(uuid);
    }

    /**
     * Get the player's {@link Gender}
     *
     * @param  uuid  the uuid of the target {@link Player}.
     * @see	Gender
     */
    public static Gender getPlayerGender(UUID uuid) {
        PlayerConfig cfg = WildfireGender.getPlayerById(uuid);
        if(cfg == null) return Configuration.GENDER.getDefault();
        return cfg.getGender();
    }
}

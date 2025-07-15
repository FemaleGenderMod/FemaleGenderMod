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
import com.wildfire.main.WildfireGenderClient;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.enums.Gender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
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
            stream -> Util.decodeFixedLengthArray(stream, 2).map(Vector2i::new),
            vec2i -> IntStream.of(vec2i.x(), vec2i.y())
    ), VEC2I_LEGACY_CODEC);

    /**
     * Add custom physics resistance attributes to a chestplate
     *
     * @deprecated Registering armor physics values through the API is deprecated; define these through resource pack
     *             data files instead.
     *
     * @implNote Implementations added through this method are presently ignored if a resource pack defines armor data
     *           at {@code NAMESPACE:wildfire_gender_data/ASSET_ID.json}, and are only used as a default implementation.
     *
     * @param  item  the item that you are linking this {@link IGenderArmor} to
     * @param  genderArmor the class implementing the {@link IGenderArmor} to apply to the item
     * @see    IGenderArmor
     */
    @Deprecated(since = "4.3.5", forRemoval = true)
    public static void addGenderArmor(Item item, IGenderArmor genderArmor) {
        GENDER_ARMORS.put(item, genderArmor);
    }

    /**
     * Get the cached config for a {@link PlayerEntity}
     *
     * @apiNote This method will not load a player's config if they aren't already cached, and will only return
     *          the config of players the mod has already loaded.
     *
     * @param  uuid  the uuid of the target {@link PlayerEntity}
     * @see    PlayerConfig
     */
    public static @Nullable PlayerConfig getPlayerById(UUID uuid) {
        return WildfireGender.getPlayerById(uuid);
    }

    /**
     * Get the player's {@link Gender}
     *
     * @param  uuid  the uuid of the target {@link PlayerEntity}.
     * @see    Gender
     */
    public static @NotNull Gender getPlayerGender(UUID uuid) {
        PlayerConfig cfg = WildfireGender.getPlayerById(uuid);
        if(cfg == null) return Configuration.GENDER.getDefault();
        return cfg.getGender();
    }

    /**
     * <p>Load data for the provided player UUID</p>
     *
     * <p>This attempts to load a local config file for the provided UUID, before falling back to making a
     * request to the {@link com.wildfire.main.cloud.CloudSync cloud sync} server for it
     * (if cloud syncing is enabled).</p>
     *
     * <p>Use of this method is <b>heavily</b> discouraged, as the mod will already perform this load process when
     * first accessing a player's config; the exact return type of this method may also change between versions.</p>
     *
     * @deprecated This method will likely be removed in the future; if you depend on this for any reason,
     *             please open an issue explaining your use case.
     *
     * @param  uuid  the uuid of the target {@link PlayerEntity}
     * @param  markForSync {@code true} if player data should be synced to the server upon being loaded; this only has an effect on the client player.
     */
    @Deprecated(since = "4.3.3", forRemoval = true)
    @Environment(EnvType.CLIENT)
    public static CompletableFuture<@Nullable PlayerConfig> loadGenderInfo(UUID uuid, boolean markForSync) {
        return WildfireGenderClient.loadGenderInfo(uuid, markForSync, false);
    }

    /**
     * Get every registered {@link IGenderArmor custom armor configuration}
     *
     * @deprecated Registering armor physics values through the API is deprecated; define these through resource pack
     *             data files instead.
     *
     * @implNote This does not include armors registered through resource packs;
     *           see {@link com.wildfire.resources.GenderArmorResourceManager} for that.
     *
     * @see #addGenderArmor
     */
    @Deprecated(since = "4.3.5", forRemoval = true)
    public static Map<Item, IGenderArmor> getGenderArmors() {
        return GENDER_ARMORS;
    }

}

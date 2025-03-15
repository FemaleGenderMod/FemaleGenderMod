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
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public final class WildfireAPI {

    public static final String MODID = "wildfire_gender";

    private WildfireAPI() {
    }

    /**
     * Item capability used for gender armor.
     *
     * @deprecated Define gender armor via data packs. This can be created with {@link com.wildfire.api.data.GenderArmorProvider}
     */
    @Deprecated(forRemoval = true, since = "4.0.0")
    public static final ItemCapability<IGenderArmor, Void> GENDER_ARMOR_CAPABILITY = ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath(MODID, "gender_armor"), IGenderArmor.class);

    private static final Codec<Vector2ic> VECTOR_2I_LEGACY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.INT.fieldOf("x").forGetter(Vector2ic::x),
          Codec.INT.fieldOf("y").forGetter(Vector2ic::y)
    ).apply(instance, Vector2i::new));
    static final Codec<Vector2ic> VECTOR_2I_CODEC = Codec.withAlternative(Codec.INT_STREAM.comapFlatMap(
          stream -> Util.fixedSize(stream, 2).map(Vector2i::new),
          vec2i -> IntStream.of(vec2i.x(), vec2i.y())
    ), VECTOR_2I_LEGACY_CODEC);
}
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
import com.wildfire.api.impl.BreastArmorTexture;
import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVMap;
import com.wildfire.main.uvs.UVQuad;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Map;

/**
 * Defines the texture data for a given armor piece when covering an entity's breasts
 */
public interface IBreastArmorTexture {
    /**
     * Default breast texture values, supplying values for armors directly compatible with the vanilla armor renderer.
     */
    IBreastArmorTexture DEFAULT = new IBreastArmorTexture() {
    };

    Vector2ic DEFAULT_TEXTURE_SIZE = new Vector2i(64, 32);
    Vector2ic DEFAULT_DIMENSIONS = new Vector2i(4, 5);

    @Deprecated
    Vector2ic DEFAULT_LEFT_UV = new Vector2i(16, 17);
    @Deprecated
    Vector2ic DEFAULT_RIGHT_UV = DEFAULT_LEFT_UV.add(DEFAULT_DIMENSIONS.x(), 0, new Vector2i());

    UVMap DEFAULT_UVS = new UVMap(
        /*left =*/ new UVLayout.Immutable(Map.of(
            UVDirection.EAST,  new UVQuad(24, 21, 28, 26),
            UVDirection.WEST,  new UVQuad(16, 21, 20, 26),
            UVDirection.DOWN,  new UVQuad(20, 17, 24, 21),
            UVDirection.UP,    new UVQuad(20, 25, 24, 27),
            UVDirection.NORTH, new UVQuad(20, 21, 24, 26)
        )),
        /*right =*/ new UVLayout.Immutable(Map.of(
            UVDirection.EAST,  new UVQuad(28, 21, 32, 26),
            UVDirection.WEST,  new UVQuad(20, 21, 24, 26),
            UVDirection.DOWN,  new UVQuad(24, 17, 28, 21),
            UVDirection.UP,    new UVQuad(24, 25, 28, 27),
            UVDirection.NORTH, new UVQuad(24, 21, 28, 26)
        ))
    );

    @ApiStatus.Internal
    Codec<IBreastArmorTexture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WildfireAPI.VECTOR_2I_CODEC
                    .optionalFieldOf("texture_size", DEFAULT_TEXTURE_SIZE)
                    .forGetter(IBreastArmorTexture::textureSize),
            UVMap.CODEC
                    .optionalFieldOf("uvs", DEFAULT_UVS)
                    .forGetter(IBreastArmorTexture::uvs)
    ).apply(instance, BreastArmorTexture::new));

    /**
     * The size of the armor sprite in pixels
     *
     * @implNote Defaults to {@code Vector2ic(64, 32)}
     *
     * @return A {@link Vector2ic} indicating how large the texture file is
     */
    default Vector2ic textureSize() {
        return DEFAULT_TEXTURE_SIZE;
    }

    /**
     * @deprecated This property is no longer used; use {@link #uvs()} instead
     */
    @Deprecated
    default Vector2ic dimensions() {
        return DEFAULT_DIMENSIONS;
    }

    /**
     * @deprecated This property is no longer used; use {@link #uvs()} instead
     */
    @Deprecated
    default Vector2ic leftUv() {
        return DEFAULT_LEFT_UV;
    }

    /**
     * @deprecated This property is no longer used; use {@link #uvs()} instead
     */
    @Deprecated
    default Vector2ic rightUv() {
        return DEFAULT_RIGHT_UV;
    }

    /**
     * @apiNote The returned {@link UVMap} is expected to consist of {@link UVLayout.Immutable immutable layouts}
     *
     * @return The UV layout
     */
    default UVMap uvs() {
        return DEFAULT_UVS;
    }
}

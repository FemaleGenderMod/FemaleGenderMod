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
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2i;
import org.joml.Vector2ic;

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
	Vector2ic DEFAULT_LEFT_UV = new Vector2i(16, 17);
	Vector2ic DEFAULT_RIGHT_UV = DEFAULT_LEFT_UV.add(DEFAULT_DIMENSIONS.x(), 0, new Vector2i());

	@ApiStatus.Internal
	Codec<IBreastArmorTexture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			WildfireAPI.VECTOR_2I_CODEC
					.optionalFieldOf("texture_size", DEFAULT_TEXTURE_SIZE)
					.forGetter(IBreastArmorTexture::textureSize),
			WildfireAPI.VECTOR_2I_CODEC
					.optionalFieldOf("left_uv", DEFAULT_LEFT_UV)
					.forGetter(IBreastArmorTexture::leftUv),
			WildfireAPI.VECTOR_2I_CODEC
					.optionalFieldOf("right_uv", new Vector2i(-1, -1))
					.forGetter(IBreastArmorTexture::rightUv),
			WildfireAPI.VECTOR_2I_CODEC
					.optionalFieldOf("dimensions", DEFAULT_DIMENSIONS)
					.forGetter(IBreastArmorTexture::dimensions)
	).apply(instance, (size, leftUv, rightUv, dimensions) -> {
		var right = rightUv;
		if(right.x() == -1 && right.y() == -1) {
			right = leftUv.add(dimensions.x(), 0, new Vector2i());
		}
		return new BreastArmorTexture(size, leftUv, right, dimensions);
	}));

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
	 * How large of an area from the sprite should be used for each breast
	 *
	 * @apiNote The X value of this should be halved from the total chest size to account for each breast side
	 *          rendering independently of each other.
	 *
	 * @implNote Defaults to {@code Vector2ic(4, 5)}
	 *
	 * @return A {@link Vector2ic} indicating how large of an area should be grabbed from the texture sprite to display over
	 *         the wearer's breasts
	 */
	default Vector2ic dimensions() {
		return DEFAULT_DIMENSIONS;
	}

	/**
	 * Where the left breast should grab the texture from on the sprite
	 *
	 * @implNote Defaults to {@code Vector2ic(16, 17)}
	 *
	 * @return A {@link Vector2ic} indicating the UV to use for the left breast
	 */
	default Vector2ic leftUv() {
		return DEFAULT_LEFT_UV;
	}

	/**
	 * Where the right breast should grab the texture from on the sprite
	 *
	 * @implNote Defaults to {@code Vector2ic(20, 17)}
	 *
	 * @return A {@link Vector2ic} indicating the UV to use for the right breast
	 */
	default Vector2ic rightUv() {
		return DEFAULT_RIGHT_UV;
	}
}

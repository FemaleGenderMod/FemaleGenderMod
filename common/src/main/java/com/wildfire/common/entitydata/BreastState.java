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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector3fc;

/// Minimal variant of [Breasts] to allow for mirroring serialization of [Breasts#CODEC] more easily
public record BreastState(float breastSize, float cleavage, Vector3fc offsets) {

    public static final Codec<BreastState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Breasts.BUST_SIZE.codec().fieldOf("size").forGetter(BreastState::breastSize),
        Breasts.BREASTS_CLEAVAGE.codec().fieldOf("cleavage").forGetter(BreastState::cleavage),
        Breasts.OFFSET_CODEC.fieldOf("offset").forGetter(BreastState::offsets)
    ).apply(instance, BreastState::new));

    public BreastState(BreastDataComponent component) {
        this(component.breastSize(), component.cleavage(), component.offsets());
    }

    public BreastState(Breasts breasts) {
        this(breasts.bustSize().get(), breasts.cleavage().get(), breasts.offset());
    }
}

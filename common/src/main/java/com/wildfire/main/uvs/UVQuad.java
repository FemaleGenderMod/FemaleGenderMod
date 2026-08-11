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

package com.wildfire.main.uvs;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record UVQuad(int x1, int y1, int x2, int y2) {
    public static final UVQuad UNUSED = new UVQuad(0, 0, 0, 0);

    public static final Codec<UVQuad> CODEC = Codec.INT.listOf(4, 4).xmap(UVQuad::fromIntList, UVQuad::toIntList);
    public static final Codec<UVQuad> LEGACY_CONFIG_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("x1").forGetter(UVQuad::x1),
        Codec.INT.fieldOf("y1").forGetter(UVQuad::y1),
        Codec.INT.fieldOf("x2").forGetter(UVQuad::x2),
        Codec.INT.fieldOf("y2").forGetter(UVQuad::y2)
    ).apply(instance, UVQuad::new));
    public static final Codec<UVQuad> OR_LEGACY = CODEC.withAlternative(LEGACY_CONFIG_CODEC);

    public static final StreamCodec<ByteBuf, UVQuad> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, UVQuad::x1,
            ByteBufCodecs.VAR_INT, UVQuad::y1,
            ByteBufCodecs.VAR_INT, UVQuad::x2,
            ByteBufCodecs.VAR_INT, UVQuad::y2,
            UVQuad::new
    );

    public UVQuad addX1(int x1) {
        return new UVQuad(this.x1 + x1, y1, x2, y2);
    }

    public UVQuad addY1(int y1) {
        return new UVQuad(x1, this.y1 + y1, x2, y2);
    }

    public UVQuad addX2(int x2) {
        return new UVQuad(x1, y1, this.x2 + x2, y2);
    }

    public UVQuad addY2(int y2) {
        return new UVQuad(x1, y1, x2, this.y2 + y2);
    }

    public List<Integer> toIntList() {
        return List.of(x1, y1, x2, y2);
    }

    public static UVQuad fromIntList(List<Integer> list) {
        Preconditions.checkArgument(list.size() == 4, "Expected exactly 4 integer elements, got %s instead", list.size());
        return new UVQuad(list.get(0), list.get(1), list.get(2), list.get(3));
    }
}

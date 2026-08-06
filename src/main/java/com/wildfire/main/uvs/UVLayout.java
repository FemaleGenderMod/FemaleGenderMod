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

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public class UVLayout {
    /// @apiNote Any layouts returned from this codec are [`immutable`][Immutable]
    public static final Codec<UVLayout> CODEC = Codec.unboundedMap(UVDirection.NAME_CODEC, UVQuad.CODEC).xmap(UVLayout::createImmutable, UVLayout::getQuads);
    public static final Codec<UVLayout> MUTABLE_CONFIG_CODEC = Codec.unboundedMap(UVDirection.NAME_CODEC, UVQuad.OR_LEGACY).xmap(UVLayout::new, UVLayout::getQuads);
    public static final StreamCodec<ByteBuf, UVLayout> STREAM_CODEC = ByteBufCodecs.map(
        _ -> new EnumMap<>(UVDirection.class),
        UVDirection.STREAM_CODEC,
        UVQuad.STREAM_CODEC,
        UVDirection.values().length
    ).map(UVLayout::new, UVLayout::getQuads);

    private final EnumMap<UVDirection, @Nullable UVQuad> quads = new EnumMap<>(UVDirection.class);

    private UVLayout(Map<UVDirection, @Nullable UVQuad> map) {
        this.quads.putAll(map);
        fillMissing();
    }

    public UVLayout(UVQuad east, UVQuad west, UVQuad down, UVQuad up, UVQuad north) {
        quads.put(UVDirection.EAST,  east);
        quads.put(UVDirection.WEST,  west);
        quads.put(UVDirection.DOWN,  down);
        quads.put(UVDirection.UP,    up);
        quads.put(UVDirection.NORTH, north);
    }

    public UVLayout() {
        this(Collections.emptyMap());
    }

    private void fillMissing() {
        quads.putIfAbsent(UVDirection.EAST,  null);
        quads.putIfAbsent(UVDirection.WEST,  null);
        quads.putIfAbsent(UVDirection.DOWN,  null);
        quads.putIfAbsent(UVDirection.UP,    null);
        quads.putIfAbsent(UVDirection.NORTH, null);
    }

    public void put(UVDirection dir, UVQuad quad) {
        quads.put(dir, quad);
    }

    public @Nullable UVQuad get(UVDirection dir) {
        return quads.get(dir);
    }

    public boolean has(UVDirection dir) {
        return quads.containsKey(dir) && quads.get(dir) != null;
    }

    @SuppressWarnings("NullableProblems") // not actually a problem
    @ApiStatus.Internal
    public EnumMap<UVDirection, UVQuad> getQuads() {
        var clone = quads.clone();
        clone.entrySet().removeIf(entry -> entry.getValue() == null);
        return clone;
    }

    public @UnmodifiableView Map<UVDirection, @Nullable UVQuad> getAllSides() {
        return Collections.unmodifiableMap(quads);
    }

    public UVLayout copy() {
        var copy = new UVLayout();
        copy.quads.putAll(this.quads);
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UVLayout other)) return false;
        return quads.equals(other.quads);
    }

    @Override
    public int hashCode() {
        return quads.hashCode();
    }

    // used to avoid potential class load deadlocks from referencing Immutable::new in CODEC
    private static Immutable createImmutable(Map<UVDirection, @Nullable UVQuad> quads) {
        return new Immutable(quads);
    }

    public static final class Immutable extends UVLayout {
        public Immutable(Map<UVDirection, @Nullable UVQuad> quads) {
            super(quads);
        }

        @Override
        public void put(UVDirection dir, UVQuad quad) {
            throw new UnsupportedOperationException();
        }
    }
}

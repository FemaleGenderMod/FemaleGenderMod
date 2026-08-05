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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.value.ConfigKey;
import com.wildfire.main.config.value.ConfigValue;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.minecraft.network.codec.StreamCodec;

public record UVs(Layer skin, Layer overlay) implements Iterable<UVLayout> {

    public static final MapCodec<UVs> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Layer.codec(Configuration.LEFT_BREAST_UV_LAYOUT, Configuration.RIGHT_BREAST_UV_LAYOUT).forGetter(UVs::skin),
        Layer.codec(Configuration.LEFT_BREAST_OVERLAY_UV_LAYOUT, Configuration.RIGHT_BREAST_OVERLAY_UV_LAYOUT).forGetter(UVs::overlay)
    ).apply(instance, UVs::new));
    public static final StreamCodec<ByteBuf, UVs> STREAM_CODEC = StreamCodec.composite(
        Layer.streamCodec(Configuration.LEFT_BREAST_UV_LAYOUT, Configuration.RIGHT_BREAST_UV_LAYOUT), UVs::skin,
        Layer.streamCodec(Configuration.LEFT_BREAST_OVERLAY_UV_LAYOUT, Configuration.RIGHT_BREAST_OVERLAY_UV_LAYOUT), UVs::overlay,
        UVs::new
    );

    @Override
    public Iterator<UVLayout> iterator() {
        return new LayoutIterator();
    }

    public boolean reset() {
        //Note: Intentionally uses bitwise or operators so that it runs on all, but we only have to save if at least one of them updated
        return skin.reset() | overlay.reset();
    }

    public record Layer(ConfigValue<UVLayout> left, ConfigValue<UVLayout> right) {

        private static MapCodec<Layer> codec(ConfigKey<UVLayout> leftKey, ConfigKey<UVLayout> rightKey) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                leftKey.codecOrDefault().forGetter(layer -> layer.left.get()),
                rightKey.codecOrDefault().forGetter(layer -> layer.right.get())
            ).apply(instance,  (left, right) -> new Layer(leftKey.createValueHandler(left), rightKey.createValueHandler(right))));
        }

        private static StreamCodec<ByteBuf, Layer> streamCodec(ConfigKey<UVLayout> leftKey, ConfigKey<UVLayout> rightKey) {
            return StreamCodec.composite(
                UVLayout.STREAM_CODEC, layer -> layer.left.get(),
                UVLayout.STREAM_CODEC, layer -> layer.right.get(),
                (left, right) -> new Layer(leftKey.createValueHandler(left), rightKey.createValueHandler(right))
            );
        }

        public boolean reset() {
            //Note: Intentionally uses bitwise or operators so that it runs on all, but we only have to save if at least one of them updated
            return left.reset() | right.reset();
        }
    }

    private class LayoutIterator implements Iterator<UVLayout> {

        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < 4;
        }

        @Override
        public UVLayout next() {
            return switch (index++) {
                case 0 -> skin().left().get();
                case 1 -> skin().right().get();
                case 2 -> overlay().left().get();
                case 3 -> overlay().right().get();
                default -> throw new NoSuchElementException("No more UVLayouts");
            };
        }
    }
}

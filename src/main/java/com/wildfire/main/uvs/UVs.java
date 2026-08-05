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
import com.wildfire.main.config.value.ConfigKey;
import com.wildfire.main.config.value.ConfigValue;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.minecraft.network.codec.StreamCodec;

public record UVs(Layer skin, Layer overlay) implements Iterable<UVLayout> {

    // TODO change these UVLayout entries to use UVMap objects?
    //        would probably require adding some form of migration capability to AbstractConfiguration
    //        Is the above even relevant anymore?

    // Base breasts
    private static final ConfigKey<UVLayout> LEFT_BREAST_UV_LAYOUT = ConfigKey.create("leftBreastUVLayout", new UVLayout(
        new UVQuad(24, 21, 27, 26),  // EAST
        new UVQuad(16, 21, 20, 26),  // WEST
        new UVQuad(20, 17, 24, 21),  // DOWN
        new UVQuad(20, 25, 24, 27),  // UP
        new UVQuad(20, 21, 24, 26)   // NORTH
    )::copy);//Note: We copy to ensure that the default instance doesn't get mutated
    private static final ConfigKey<UVLayout> RIGHT_BREAST_UV_LAYOUT = ConfigKey.create("rightBreastUVLayout", new UVLayout(
        new UVQuad(28, 21, 32, 26),  // EAST
        new UVQuad(21, 21, 24, 26),  // WEST
        new UVQuad(24, 17, 28, 21),  // DOWN
        new UVQuad(24, 25, 28, 27),  // UP
        new UVQuad(24, 21, 28, 26)   // NORTH
    )::copy);//Note: We copy to ensure that the default instance doesn't get mutated

    // Overlay breasts
    private static final ConfigKey<UVLayout> LEFT_BREAST_OVERLAY_UV_LAYOUT = ConfigKey.create("leftBreastOverlayUVLayout", new UVLayout(
        UVQuad.UNUSED,                              // EAST
        new UVQuad(17, 37, 20, 42),  // WEST
        new UVQuad(20, 34, 24, 37),  // DOWN
        new UVQuad(20, 41, 24, 44),  // UP
        new UVQuad(20, 37, 24, 42)   // NORTH
    )::copy);//Note: We copy to ensure that the default instance doesn't get mutated
    private static final ConfigKey<UVLayout> RIGHT_BREAST_OVERLAY_UV_LAYOUT = ConfigKey.create("rightBreastOverlayUVLayout", new UVLayout(
        new UVQuad(28, 37, 31, 42),  // EAST
        UVQuad.UNUSED,                              // WEST
        new UVQuad(24, 34, 28, 37),  // DOWN
        new UVQuad(24, 41, 28, 44),  // UP
        new UVQuad(24, 37, 28, 42)   // NORTH
    )::copy);//Note: We copy to ensure that the default instance doesn't get mutated

    public static final MapCodec<UVs> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Layer.codec(LEFT_BREAST_UV_LAYOUT, RIGHT_BREAST_UV_LAYOUT).forGetter(UVs::skin),
        Layer.codec(LEFT_BREAST_OVERLAY_UV_LAYOUT, RIGHT_BREAST_OVERLAY_UV_LAYOUT).forGetter(UVs::overlay)
    ).apply(instance, UVs::new));
    public static final StreamCodec<ByteBuf, UVs> STREAM_CODEC = StreamCodec.composite(
        Layer.streamCodec(LEFT_BREAST_UV_LAYOUT, RIGHT_BREAST_UV_LAYOUT), UVs::skin,
        Layer.streamCodec(LEFT_BREAST_OVERLAY_UV_LAYOUT, RIGHT_BREAST_OVERLAY_UV_LAYOUT), UVs::overlay,
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

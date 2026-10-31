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

package com.wildfire.common.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.api.uvs.UVLayout;
import com.wildfire.api.uvs.UVQuad;
import com.wildfire.common.WildfireHelper;
import com.wildfire.common.config.value.ConfigKey;
import com.wildfire.common.config.value.ConfigValue;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import net.minecraft.network.codec.StreamCodec;

public record UVs(Layer skin, Layer overlay) implements Iterable<UVLayout> {

    private static ConfigKey<UVLayout> createConfigKey(Supplier<UVLayout> defaultValueSupplier) {
        return new ConfigKey<>(defaultValueSupplier, UVLayout.MUTABLE_CONFIG_CODEC, UVLayout.STREAM_CODEC);
    }

    // Base breasts
    private static final ConfigKey<UVLayout> LEFT_BREAST_UV_LAYOUT = createConfigKey(new UVLayout(
        new UVQuad(24, 21, 27, 26),  // EAST
        new UVQuad(16, 21, 20, 26),  // WEST
        new UVQuad(20, 17, 24, 21),  // DOWN
        new UVQuad(20, 25, 24, 27),  // UP
        new UVQuad(20, 21, 24, 26)   // NORTH
    )::copy);//Note: We copy to ensure that the default instance doesn't get mutated
    private static final ConfigKey<UVLayout> RIGHT_BREAST_UV_LAYOUT = createConfigKey(new UVLayout(
        new UVQuad(28, 21, 32, 26),  // EAST
        new UVQuad(21, 21, 24, 26),  // WEST
        new UVQuad(24, 17, 28, 21),  // DOWN
        new UVQuad(24, 25, 28, 27),  // UP
        new UVQuad(24, 21, 28, 26)   // NORTH
    )::copy);//Note: We copy to ensure that the default instance doesn't get mutated

    // Overlay breasts
    private static final ConfigKey<UVLayout> LEFT_BREAST_OVERLAY_UV_LAYOUT = createConfigKey(new UVLayout(
        UVQuad.UNUSED,                              // EAST
        new UVQuad(17, 37, 20, 42),  // WEST
        new UVQuad(20, 34, 24, 37),  // DOWN
        new UVQuad(20, 41, 24, 44),  // UP
        new UVQuad(20, 37, 24, 42)   // NORTH
    )::copy);//Note: We copy to ensure that the default instance doesn't get mutated
    private static final ConfigKey<UVLayout> RIGHT_BREAST_OVERLAY_UV_LAYOUT = createConfigKey(new UVLayout(
        new UVQuad(28, 37, 31, 42),  // EAST
        UVQuad.UNUSED,                              // WEST
        new UVQuad(24, 34, 28, 37),  // DOWN
        new UVQuad(24, 41, 28, 44),  // UP
        new UVQuad(24, 37, 28, 42)   // NORTH
    )::copy);//Note: We copy to ensure that the default instance doesn't get mutated

    public static final Codec<UVs> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Layer.codec(LEFT_BREAST_UV_LAYOUT, RIGHT_BREAST_UV_LAYOUT).fieldOf("skin").forGetter(UVs::skin),
        Layer.codec(LEFT_BREAST_OVERLAY_UV_LAYOUT, RIGHT_BREAST_OVERLAY_UV_LAYOUT).fieldOf("overlay").forGetter(UVs::overlay)
    ).apply(instance, UVs::new));
    public static final MapCodec<UVs> LEGACY_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Layer.legacyCodec(LEFT_BREAST_UV_LAYOUT, "leftBreastUVLayout", RIGHT_BREAST_UV_LAYOUT, "rightBreastUVLayout").forGetter(UVs::skin),
        Layer.legacyCodec(LEFT_BREAST_OVERLAY_UV_LAYOUT, "leftBreastOverlayUVLayout", RIGHT_BREAST_OVERLAY_UV_LAYOUT, "rightBreastOverlayUVLayout").forGetter(UVs::overlay)
    ).apply(instance, UVs::new));
    //Note: We allow the legacy codec to handle loading as defaults if not present/malformed. If/when we remove the legacy codec, we will need to adjust
    // the main codec to have the layer parts be lenientOptionalFieldOf, or to have an orElse. We will also be able to move the fieldOf("uvs") to the caller
    public static final MapCodec<UVs> CODEC_OR_LEGACY = WildfireHelper.withAlternative(CODEC.fieldOf("uvs"), LEGACY_CODEC);

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

        private static Codec<Layer> codec(ConfigKey<UVLayout> leftKey, ConfigKey<UVLayout> rightKey) {
            return RecordCodecBuilder.create(instance -> instance.group(
                leftKey.codec().fieldOf("left").forGetter(layer -> layer.left.get()),
                rightKey.codec().fieldOf("right").forGetter(layer -> layer.right.get())
            ).apply(instance,  (left, right) -> new Layer(leftKey.createValueHandler(left), rightKey.createValueHandler(right))));
        }

        private static MapCodec<Layer> legacyCodec(ConfigKey<UVLayout> leftKey, String leftName, ConfigKey<UVLayout> rightKey, String rightName) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                leftKey.codecOrDefault(leftName).forGetter(layer -> layer.left.get()),
                rightKey.codecOrDefault(rightName).forGetter(layer -> layer.right.get())
            ).apply(instance,  (left, right) -> new Layer(leftKey.createValueHandler(left), rightKey.createValueHandler(right))));
        }

        private static StreamCodec<ByteBuf, Layer> streamCodec(ConfigKey<UVLayout> leftKey, ConfigKey<UVLayout> rightKey) {
            return StreamCodec.composite(
                leftKey.streamCodec(), layer -> layer.left.get(),
                rightKey.streamCodec(), layer -> layer.right.get(),
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

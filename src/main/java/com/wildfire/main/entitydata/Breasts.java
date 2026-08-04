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

package com.wildfire.main.entitydata;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.value.ConfigValue;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/// Data class representing an entity's breast appearance settings
@SuppressWarnings("UnusedReturnValue")
public final class Breasts {

    public static final MapCodec<Breasts> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Configuration.BREASTS_OFFSET_X.codecOrDefault().forGetter(breasts -> breasts.xOffset.get()),
        Configuration.BREASTS_OFFSET_Y.codecOrDefault().forGetter(breasts -> breasts.yOffset.get()),
        Configuration.BREASTS_OFFSET_Z.codecOrDefault().forGetter(breasts -> breasts.zOffset.get()),
        Configuration.BREASTS_UNIBOOB.codecOrDefault().forGetter(breasts -> breasts.uniboob.get()),
        Configuration.BREASTS_CLEAVAGE.codecOrDefault().forGetter(breasts -> breasts.cleavage.get())
    ).apply(instance, Breasts::new));
    public static final StreamCodec<ByteBuf, Breasts> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, breasts -> breasts.xOffset.get(),
        ByteBufCodecs.FLOAT, breasts -> breasts.yOffset.get(),
        ByteBufCodecs.FLOAT, breasts -> breasts.zOffset.get(),
        ByteBufCodecs.BOOL, breasts -> breasts.uniboob.get(),
        ByteBufCodecs.FLOAT, breasts -> breasts.cleavage.get(),
        Breasts::new
    );

    /// How far apart the player's breasts should be rendered from each other, also referred to as Separation in the UI
    ///
    /// @implNote Negative float values renders the breasts further apart, while positive values renders them closer together
    ///
    /// @return  A `float` between `-1f` and `1f`
    public final ConfigValue<Float> xOffset;
    /// How far up or down the player's breasts should be rendered, also referred to as Height in the UI
    ///
    /// @implNote Negative values renders the breasts lower down, while positive values renders them higher up
    ///
    /// @return  A `float` between `-1f` and `1f`
    public final ConfigValue<Float> yOffset;
    /// How far back the player's breasts should be rendered, also referred to as Depth in the UI
    ///
    /// @return  A `float` between `0f` and `1f`
    public final ConfigValue<Float> zOffset;
    /// How much rotation outward there should be on each of the player's breasts
    ///
    /// @return  A `float` between `0f` and `0.1f`
    public final ConfigValue<Float> cleavage;
    /// Determines if breast physics should be independent of each other; also referred to as Dual-Physics in the UI
    ///
    /// @return `false` if physics should be independent on each breast, `true` if both should use the same physics
    public final ConfigValue<Boolean> uniboob;

    private Breasts(float xOffset, float yOffset, float zOffset, boolean uniboob, float cleavage) {
        this.xOffset = Configuration.BREASTS_OFFSET_X.createValueHandler(xOffset);
        this.yOffset = Configuration.BREASTS_OFFSET_Y.createValueHandler(yOffset);
        this.zOffset = Configuration.BREASTS_OFFSET_Z.createValueHandler(zOffset);
        this.uniboob = Configuration.BREASTS_UNIBOOB.createValueHandler(uniboob);
        this.cleavage = Configuration.BREASTS_CLEAVAGE.createValueHandler(cleavage);
    }

    public Vector3f getOffsets() {
        return new Vector3f(xOffset.get(), yOffset.get(), zOffset.get());
    }

    public void updateOffsets(Vector3fc offsets) {
        xOffset.update(offsets.x());
        yOffset.update(offsets.y());
        zOffset.update(offsets.z());
    }

    /// Copy settings from the provided [`breasts data`][Breasts] onto the current instance
    public void copyFrom(Breasts breasts) {
        updateOffsets(breasts.getOffsets());
        this.cleavage.update(breasts.cleavage.get());
        this.uniboob.update(breasts.uniboob.get());
    }
}

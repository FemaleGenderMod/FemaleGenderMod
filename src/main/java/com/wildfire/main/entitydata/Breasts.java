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
import com.wildfire.main.config.value.ConfigKey;
import com.wildfire.main.config.value.ConfigValue;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector3f;

/// Data class representing an entity's breast appearance settings
///
/// @param xOffset  How far apart the player's breasts should be rendered from each other, also referred to as Separation in the UI.  A value between `-1F` and `1F`.
///                 Negative float values renders the breasts further apart, while positive values renders them closer together
/// @param yOffset  How far up or down the player's breasts should be rendered, also referred to as Height in the UI.  A value between `-1F` and `1F`.
///                 Negative values renders the breasts lower down, while positive values renders them higher up
/// @param zOffset  How far back the player's breasts should be rendered, also referred to as Depth in the UI. A value between `0F` and `1F`
/// @param cleavage How much rotation outward there should be on each of the player's breasts.  A value between `0F` and `0.1F`
public record Breasts(ConfigValue<Float> xOffset, ConfigValue<Float> yOffset, ConfigValue<Float> zOffset, ConfigValue<Float> bustSize, ConfigValue<Float> cleavage,
                      Physics physics) {

    public static final ConfigKey<Float> BREASTS_OFFSET_X = ConfigKey.create("breasts_xOffset", 0.0F, -1, 1);
    public static final ConfigKey<Float> BREASTS_OFFSET_Y = ConfigKey.create("breasts_yOffset", 0.0F, -1, 1);
    public static final ConfigKey<Float> BREASTS_OFFSET_Z = ConfigKey.create("breasts_zOffset", 0.0F, -1, 0);
    public static final ConfigKey<Float> BUST_SIZE = ConfigKey.create("bust_size", 0.6F, 0, 0.8f);
    public static final ConfigKey<Float> BREASTS_CLEAVAGE = ConfigKey.create("breasts_cleavage", 0, 0, 0.1F);

    public static final MapCodec<Breasts> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        BREASTS_OFFSET_X.codecOrDefault().forGetter(breasts -> breasts.xOffset.get()),
        BREASTS_OFFSET_Y.codecOrDefault().forGetter(breasts -> breasts.yOffset.get()),
        BREASTS_OFFSET_Z.codecOrDefault().forGetter(breasts -> breasts.zOffset.get()),
        BUST_SIZE.codecOrDefault().forGetter(config -> config.bustSize.get()),
        BREASTS_CLEAVAGE.codecOrDefault().forGetter(breasts -> breasts.cleavage.get()),
        Physics.CODEC.forGetter(Breasts::physics)
    ).apply(instance, Breasts::new));
    public static final StreamCodec<ByteBuf, Breasts> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, breasts -> breasts.xOffset.get(),
        ByteBufCodecs.FLOAT, breasts -> breasts.yOffset.get(),
        ByteBufCodecs.FLOAT, breasts -> breasts.zOffset.get(),
        ByteBufCodecs.FLOAT, breasts -> breasts.bustSize.get(),
        ByteBufCodecs.FLOAT, breasts -> breasts.cleavage.get(),
        Physics.STREAM_CODEC, Breasts::physics,
        Breasts::new
    );

    private Breasts(float xOffset, float yOffset, float zOffset, float bustSize, float cleavage, Physics physics) {
        this(BREASTS_OFFSET_X.createValueHandler(xOffset), BREASTS_OFFSET_Y.createValueHandler(yOffset), BREASTS_OFFSET_Z.createValueHandler(zOffset),
            BUST_SIZE.createValueHandler(bustSize), BREASTS_CLEAVAGE.createValueHandler(cleavage),
            physics
        );
    }

    public Vector3f offset() {
        return new Vector3f(xOffset.get(), yOffset.get(), zOffset.get());
    }

    public void updateFromComponent(BreastDataComponent component) {
        physics.enabled().update(false);
        xOffset.update(component.offsets().x());
        yOffset.update(component.offsets().y());
        zOffset.update(component.offsets().z());
        bustSize.update(component.breastSize());
        cleavage.update(component.cleavage());
    }

    public List<String> getDebugInfo() {
        List<String> info = new ArrayList<>();
        info.add("Breast size: " + bustSize());
        info.add("Physics enabled: " + physics().enabled());
        info.add("Uniboob: " + physics().uniboob());
        info.add("Cleavage: " + cleavage());
        info.add("Offsets: " + offset());//TODO: Validate this converts it to string as expected
        return info;
    }

    /// @param uniboob  Determines if breast physics should be independent of each other; also referred to as Dual-Physics in the UI.
    ///                 `false` if physics should be independent on each breast, `true` if both should use the same physics
    public record Physics(ConfigValue<Boolean> enabled, ConfigValue<Boolean> uniboob, ConfigValue<Float> bounceMultiplier, ConfigValue<Float> floppiness) {

        private static final ConfigKey<Boolean> BREAST_PHYSICS = ConfigKey.create("breast_physics", true);
        private static final ConfigKey<Boolean> BREASTS_UNIBOOB = ConfigKey.create("breasts_uniboob", true);
        private static final ConfigKey<Float> BOUNCE_MULTIPLIER = ConfigKey.create("bounce_multiplier", 0.333F, 0, 0.5f);
        private static final ConfigKey<Float> FLOPPY_MULTIPLIER = ConfigKey.create("floppy_multiplier", 0.75F, 0.25f, 1);

        public static final MapCodec<Physics> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BREAST_PHYSICS.codecOrDefault().forGetter(physics -> physics.enabled.get()),
            BREASTS_UNIBOOB.codecOrDefault().forGetter(physics -> physics.uniboob.get()),
            BOUNCE_MULTIPLIER.codecOrDefault().forGetter(physics -> physics.bounceMultiplier.get()),
            FLOPPY_MULTIPLIER.codecOrDefault().forGetter(physics -> physics.floppiness.get())
        ).apply(instance, Physics::new));
        public static final StreamCodec<ByteBuf, Physics> STREAM_CODEC = StreamCodec.composite(
            //TODO: If physics aren't enabled we don't need to sync bounce multiplier or floppiness
            ByteBufCodecs.BOOL, physics -> physics.enabled.get(),
            ByteBufCodecs.BOOL, physics -> physics.uniboob.get(),
            ByteBufCodecs.FLOAT, physics -> physics.bounceMultiplier.get(),
            ByteBufCodecs.FLOAT, physics -> physics.floppiness.get(),
            Physics::new
        );

        private Physics(boolean physics, boolean uniboob, float bounceMultiplier, float floppiness) {
            this(BREAST_PHYSICS.createValueHandler(physics),
                BREASTS_UNIBOOB.createValueHandler(uniboob),
                BOUNCE_MULTIPLIER.createValueHandler(bounceMultiplier),
                FLOPPY_MULTIPLIER.createValueHandler(floppiness)
            );
        }
    }
}

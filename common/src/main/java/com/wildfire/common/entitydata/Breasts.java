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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.common.WildfireHelper;
import com.wildfire.common.config.validator.ConfigRange;
import com.wildfire.common.config.value.ConfigKey;
import com.wildfire.common.config.value.ConfigValue;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector3f;
import org.joml.Vector3fc;

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

    public static final ConfigKey<Float> BREASTS_OFFSET_X = ConfigKey.create(0.0F, -1, 1);
    public static final ConfigKey<Float> BREASTS_OFFSET_Y = ConfigKey.create(0.0F, -1, 1);
    public static final ConfigKey<Float> BREASTS_OFFSET_Z = ConfigKey.create(0.0F, -1, 0);
    public static final ConfigKey<Float> BUST_SIZE = ConfigKey.create(0.6F, 0, 0.8F);
    public static final ConfigKey<Float> BREASTS_CLEAVAGE = ConfigKey.create(0, 0, 0.1F);

    //TODO: Once we remove the legacy codec, maybe we want to change the config value for offsets to be of a Vector3f, and then have this OFFSET_CODEC be the legacy way
    public static final Codec<Vector3fc> OFFSET_CODEC = WildfireHelper.validatedVector(
        (ConfigRange<Float>) BREASTS_OFFSET_X.validator(),
        (ConfigRange<Float>) BREASTS_OFFSET_Y.validator(),
        (ConfigRange<Float>) BREASTS_OFFSET_Z.validator()).promotePartial(_ -> {//TODO: Do we want to log that it was invalid and we promoted the clamped value instead?
    });

    public static final Codec<Breasts> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        OFFSET_CODEC.fieldOf("offset").forGetter(Breasts::offset),
        BUST_SIZE.codec().fieldOf("size").forGetter(config -> config.bustSize.get()),
        BREASTS_CLEAVAGE.codec().fieldOf("cleavage").forGetter(breasts -> breasts.cleavage.get()),
        Physics.CODEC.fieldOf("physics").forGetter(Breasts::physics)
    ).apply(instance, Breasts::new));
    public static final MapCodec<Breasts> LEGACY_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        BREASTS_OFFSET_X.codecOrDefault("breasts_xOffset").forGetter(breasts -> breasts.xOffset.get()),
        BREASTS_OFFSET_Y.codecOrDefault("breasts_yOffset").forGetter(breasts -> breasts.yOffset.get()),
        BREASTS_OFFSET_Z.codecOrDefault("breasts_zOffset").forGetter(breasts -> breasts.zOffset.get()),
        BUST_SIZE.codecOrDefault("bust_size").forGetter(config -> config.bustSize.get()),
        BREASTS_CLEAVAGE.codecOrDefault("breasts_cleavage").forGetter(breasts -> breasts.cleavage.get()),
        Physics.LEGACY_CODEC.forGetter(Breasts::physics)
    ).apply(instance, Breasts::new));
    //Note: We allow the legacy codec to handle loading as defaults if not present/malformed. If/when we remove the legacy codec, we will need to adjust
    // the main codec to have the layer parts be lenientOptionalFieldOf or make use of codecOrDefault, or to have an orElse. We will also be able to move the fieldOf("breasts") to the caller
    public static final MapCodec<Breasts> CODEC_OR_LEGACY = WildfireHelper.withAlternative(CODEC.fieldOf("breasts"), LEGACY_CODEC);

    public static final StreamCodec<ByteBuf, Breasts> STREAM_CODEC = StreamCodec.composite(
        BREASTS_OFFSET_X.streamCodec(), breasts -> breasts.xOffset.get(),
        BREASTS_OFFSET_Y.streamCodec(), breasts -> breasts.yOffset.get(),
        BREASTS_OFFSET_Z.streamCodec(), breasts -> breasts.zOffset.get(),
        BUST_SIZE.streamCodec(), breasts -> breasts.bustSize.get(),
        BREASTS_CLEAVAGE.streamCodec(), breasts -> breasts.cleavage.get(),
        Physics.STREAM_CODEC, Breasts::physics,
        Breasts::new
    );

    private Breasts(Vector3fc offset, float bustSize, float cleavage, Physics physics) {
        this(offset.x(), offset.y(), offset.z(), bustSize, cleavage, physics);
    }

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
        info.add("Offsets: (" + xOffset.get() + ", " + yOffset.get() + ", " + zOffset.get() + ")");
        return info;
    }

    /// @param uniboob  Determines if breast physics should be independent of each other; also referred to as Dual-Physics in the UI.
    ///                 `false` if physics should be independent on each breast, `true` if both should use the same physics
    public record Physics(ConfigValue<Boolean> enabled, ConfigValue<Boolean> uniboob, ConfigValue<Float> bounceMultiplier, ConfigValue<Float> floppiness) {

        private static final ConfigKey<Boolean> BREAST_PHYSICS = ConfigKey.DEFAULT_TRUE;
        private static final ConfigKey<Boolean> BREASTS_UNIBOOB = ConfigKey.DEFAULT_TRUE;
        private static final ConfigKey<Float> BOUNCE_MULTIPLIER = ConfigKey.create(0.333F, 0, 0.5F);
        private static final ConfigKey<Float> FLOPPINESS = ConfigKey.create(0.75F, 0.25F, 1);

        private static final Codec<Physics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BREAST_PHYSICS.codec().fieldOf("enabled").forGetter(physics -> physics.enabled.get()),
            BREASTS_UNIBOOB.codec().fieldOf("uniboob").forGetter(physics -> physics.uniboob.get()),
            BOUNCE_MULTIPLIER.codec().fieldOf("bounce_multiplier").forGetter(physics -> physics.bounceMultiplier.get()),
            FLOPPINESS.codec().fieldOf("floppiness").forGetter(physics -> physics.floppiness.get())
        ).apply(instance, Physics::new));
        private static final MapCodec<Physics> LEGACY_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BREAST_PHYSICS.codecOrDefault("breast_physics").forGetter(physics -> physics.enabled.get()),
            BREASTS_UNIBOOB.codecOrDefault("breasts_uniboob").forGetter(physics -> physics.uniboob.get()),
            BOUNCE_MULTIPLIER.codecOrDefault("bounce_multiplier").forGetter(physics -> physics.bounceMultiplier.get()),
            FLOPPINESS.codecOrDefault("floppy_multiplier").forGetter(physics -> physics.floppiness.get())
        ).apply(instance, Physics::new));
        private static final StreamCodec<ByteBuf, Physics> WITH_PHYSICS_STREAM_CODEC = StreamCodec.composite(
            BREASTS_UNIBOOB.streamCodec(), physics -> physics.uniboob.get(),
            BOUNCE_MULTIPLIER.streamCodec(), physics -> physics.bounceMultiplier.get(),
            FLOPPINESS.streamCodec(), physics -> physics.floppiness.get(),
            (uniboob, bounceMultiplier, floppiness) -> new Physics(true, uniboob, bounceMultiplier, floppiness)
        );
        private static final StreamCodec<ByteBuf, Physics> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public Physics decode(final ByteBuf input) {
                if (input.readBoolean()) {
                    return WITH_PHYSICS_STREAM_CODEC.decode(input);
                }
                return new Physics(false, BREASTS_UNIBOOB.defaultValue(), BOUNCE_MULTIPLIER.defaultValue(), FLOPPINESS.defaultValue());
            }

            @Override
            public void encode(final ByteBuf output, final Physics physics) {
                if (physics.enabled.get()) {
                    output.writeBoolean(true);
                    WITH_PHYSICS_STREAM_CODEC.encode(output, physics);
                } else {
                    output.writeBoolean(false);
                }
            }
        };

        private Physics(boolean physics, boolean uniboob, float bounceMultiplier, float floppiness) {
            this(BREAST_PHYSICS.createValueHandler(physics),
                BREASTS_UNIBOOB.createValueHandler(uniboob),
                BOUNCE_MULTIPLIER.createValueHandler(bounceMultiplier),
                FLOPPINESS.createValueHandler(floppiness)
            );
        }
    }
}

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

package com.wildfire.main;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.wildfire.api.IGenderArmor;
import com.wildfire.main.config.validator.ConfigRange;
import com.wildfire.resources.GenderArmorResourceManager;
import java.util.StringJoiner;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.TriState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.concurrent.ThreadLocalRandom;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class WildfireHelper {

    private WildfireHelper() {
        throw new UnsupportedOperationException();
    }

    public static final PrimitiveCodec<TriState> TRISTATE = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<TriState> read(final DynamicOps<T> ops, final T input) {
            return DataResult.success(ops.getBooleanValue(input)
                    .map(TriState::from)
                    .result().orElse(TriState.DEFAULT));
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final TriState value) {
            if(value == TriState.DEFAULT) {
                return ops.empty();
            }
            return ops.createBoolean(value == TriState.TRUE);
        }

        @Override
        public String toString() {
            return "TriState";
        }
    };

    public static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    public static float randFloat(float min, float max) {
        return (float) ThreadLocalRandom.current().nextDouble(min, (double) max + 1);
    }

    public static float round(float num, float decimalPlaces) {
        float factor = (float) Math.pow(10, decimalPlaces);
        return Math.round(num * factor) / factor;
    }

    @Environment(EnvType.CLIENT)
    public static IGenderArmor getArmorConfig(ItemStack stack) {
        if(stack.isEmpty()) {
            return IGenderArmor.EMPTY;
        }

        return GenderArmorResourceManager.get(stack)
            .orElseGet(() -> stack.has(DataComponents.EQUIPPABLE) ? IGenderArmor.DEFAULT : IGenderArmor.EMPTY);
    }

    public static String getModVersion(String modId) {
        var mod = FabricLoader.getInstance().getModContainer(modId).orElseThrow();
        return mod.getMetadata().getVersion().getFriendlyString();
    }

    public static String toFormattedPercent(double value) {
        return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value * 100.0);
    }

    public static boolean onClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static double snapToStep(double value, double stepSize) {
        return Math.round(value / stepSize) * stepSize;
    }

    public static <T> MapCodec<T> withAlternative(MapCodec<T> primary, MapCodec<? extends T> alternative) {
        return Codec.mapEither(primary, alternative).xmap(Either::unwrap, Either::left);
    }

    public static Codec<Vector3fc> validatedVector(ConfigRange<Float> xRange, ConfigRange<Float> yRange, ConfigRange<Float> zRange) {
        return ExtraCodecs.VECTOR3F.validate(value -> {
            boolean xValid = xRange.validate(value.x());
            boolean yValid = yRange.validate(value.y());
            boolean zValid = zRange.validate(value.z());
            if (xValid && yValid && zValid) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> {
                StringJoiner message = new StringJoiner(". ");
                if (!xValid) message.add("X value must be within range [" + xRange.minInclusive() + ";" + xRange.maxInclusive() + "]: " + value.x());
                if (!yValid) message.add("Y value must be within range [" + yRange.minInclusive() + ";" + yRange.maxInclusive() + "]: " + value.y());
                if (!zValid) message.add("Z value must be within range [" + zRange.minInclusive() + ";" + zRange.maxInclusive() + "]: " + value.z());
                return message.toString();
            }, new Vector3f(
                Math.clamp(value.x(), xRange.minInclusive(), xRange.maxInclusive()),
                Math.clamp(value.y(), yRange.minInclusive(), yRange.maxInclusive()),
                Math.clamp(value.z(), zRange.minInclusive(), zRange.maxInclusive())
            ));
        });
    }
}

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
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.wildfire.main.config.validator.ConfigRange;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.StringJoiner;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class WildfireHelper {

    private WildfireHelper() {
        throw new UnsupportedOperationException();
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    @Internal
    private static final ClassLoader SERVICE_CL = WildfireGender.class.getClassLoader();

    public static float round(float num, float decimalPlaces) {
        float factor = (float) Math.pow(10, decimalPlaces);
        return Math.round(num * factor) / factor;
    }

    public static String toFormattedPercent(double value) {
        return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value * 100.0);
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

    /// Loads a WildfireGender service from ServiceLoader, ensuring that the correct classloader is used instead of relying on the context classloader, which may not be
    /// correct
    ///
    /// @param serviceClass the interface class to search for
    ///
    /// @return the concrete implementation
    ///
    /// @throws IllegalStateException when an implementation is not found
    @Internal
    public static <SERVICE> SERVICE getService(Class<SERVICE> serviceClass) {
        SERVICE service = getOptionalService(serviceClass);
        if (service != null) {
            return service;
        }

        IllegalStateException illegalStateException = new IllegalStateException("No valid ServiceImpl for " + serviceClass.getSimpleName() + " found");
        LOGGER.error("Failed to load service", illegalStateException);
        LOGGER.error("CL: {} CCL: {}", SERVICE_CL, Thread.currentThread().getContextClassLoader());
        throw illegalStateException;
    }

    /// Loads a WildfireGender service from ServiceLoader, ensuring that the correct classloader is used instead of relying on the context classloader, which may not be
    /// correct
    ///
    /// @param serviceClass the interface class to search for
    ///
    /// @return the concrete implementation, or `null` if no implementation is found
    @Nullable
    @Internal
    public static <SERVICE> SERVICE getOptionalService(Class<SERVICE> serviceClass) {
        Iterator<SERVICE> service = ServiceLoader.load(serviceClass, SERVICE_CL).iterator();
        if (service.hasNext()) {
            return service.next();
        }
        return null;
    }
}

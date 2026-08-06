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

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.value.ConfigKey;
import com.wildfire.main.config.validator.ConfigRange;
import java.util.function.Function;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/// Data component-like class for storing player breast settings on armor equipped onto armor stands
///
/// Note that while this is treated similarly to any other [`data component`][DataComponents] for performance reasons,
/// this is never written as its own component on item stacks, but instead uses the [`custom NBT data component`][DataComponents#CUSTOM_DATA]
/// (under the `WildfireGender` key) for compatibility with vanilla clients on servers.
public record BreastDataComponent(float breastSize, float cleavage, Vector3fc offsets, boolean jacket, @Nullable CustomData nbtComponent) {

    private static final String LEGACY_KEY = "WildfireGender";
    private static final Codec<BreastDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        orLegacy(Breasts.BUST_SIZE, "BreastSize").forGetter(BreastDataComponent::breastSize),
        orLegacy(Breasts.BREASTS_CLEAVAGE, "Cleavage").forGetter(BreastDataComponent::cleavage),
        Codec.BOOL.optionalFieldOf("Jacket", true).forGetter(BreastDataComponent::jacket),
        orLegacy(Breasts.BREASTS_OFFSET_X, "XOffset").forGetter(component -> component.offsets.x()),
        orLegacy(Breasts.BREASTS_OFFSET_Y, "YOffset").forGetter(component -> component.offsets.y()),
        orLegacy(Breasts.BREASTS_OFFSET_Z, "ZOffset").forGetter(component -> component.offsets.y())
        ).apply(instance, (breastSize, cleavage, jacket, x, y, z) -> new BreastDataComponent(breastSize, cleavage, new Vector3f(x, y, z), jacket, null))
    );

    private static MapCodec<Float> orLegacy(ConfigKey<Float> configKey, String legacyKey) {
        if (configKey.validator() instanceof ConfigRange<Float>(Float minInclusive, Float maxInclusive)) {
            return Codec.mapEither(
                //Don't use the default here so we can try loading via the legacy way
                configKey.codec(),
                //Note: We make this be lenient so that if it failed to deserialize either with this or the new codec, then it falls back to the default value
                ExtraCodecs.floatRange(minInclusive, maxInclusive).lenientOptionalFieldOf(legacyKey, configKey.defaultValue())
            ).xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);
        }
        WildfireGender.LOGGER.warn("No range defined for config key: {}", configKey.key());
        return configKey.codecOrDefault();
    }

    public static @Nullable BreastDataComponent fromPlayer(Player player, PlayerConfigHolder config) {
        if(!config.gender().get().canHaveBreasts() || !config.showBreastsInArmor().get()) {
            return null;
        }

        Breasts breasts = config.breasts();
        return new BreastDataComponent(breasts.bustSize().get(), breasts.cleavage().get(), breasts.offset(), player.isModelPartShown(PlayerModelPart.JACKET), null);
    }

    public static @Nullable BreastDataComponent fromComponent(@Nullable CustomData component) {
        if(component == null) {
            return null;
        }

        CompoundTag compoundTag = component.copyTag();
        return CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound(WildfireGender.MODID).orElseGet(() -> compoundTag.getCompoundOrEmpty(LEGACY_KEY)))
                .result()
                .map(breastDataComponent -> breastDataComponent.withComponent(component))
                .orElse(null);
    }

    public void write(ItemStack stack) {
        if(stack.isEmpty()) {
            throw new IllegalArgumentException("The provided ItemStack must not be empty");
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> {
            if (nbt.contains(LEGACY_KEY)) {//Remove legacy key if it already had it
                nbt.remove(LEGACY_KEY);
            }
            nbt.store(WildfireGender.MODID, CODEC, this);
        });
    }

    public static void removeFromStack(ItemStack stack) {
        if(stack.isEmpty()) return;
        CustomData component = stack.get(DataComponents.CUSTOM_DATA);
        if(component != null) {
            CompoundTag compoundTag = component.copyTag();
            if (compoundTag.contains(WildfireGender.MODID) || compoundTag.contains(LEGACY_KEY)) {
                CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> {
                    nbt.remove(WildfireGender.MODID);
                    nbt.remove(LEGACY_KEY);
                });
            }
        }
    }

    private BreastDataComponent withComponent(CustomData component) {
        return new BreastDataComponent(breastSize, cleavage, offsets, jacket, component);
    }
}

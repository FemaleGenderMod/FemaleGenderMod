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
import com.wildfire.common.config.value.ConfigKey;
import com.wildfire.common.config.validator.ConfigRange;
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

    private static final String KEY = "female_gender";
    private static final String LEGACY_KEY = "WildfireGender";

    private static final Codec<BreastDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BreastState.CODEC.fieldOf("breasts").forGetter(BreastState::new),
        Codec.BOOL.optionalFieldOf("Jacket", true).forGetter(BreastDataComponent::jacket)
    ).apply(instance, (state, jacket) -> new BreastDataComponent(state.breastSize(), state.cleavage(), state.offsets(), jacket, null)));
    private static final Codec<BreastDataComponent> LEGACY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        legacy(Breasts.BUST_SIZE, "BreastSize").forGetter(BreastDataComponent::breastSize),
        legacy(Breasts.BREASTS_CLEAVAGE, "Cleavage").forGetter(BreastDataComponent::cleavage),
        legacy(Breasts.BREASTS_OFFSET_X, "XOffset").forGetter(component -> component.offsets.x()),
        legacy(Breasts.BREASTS_OFFSET_Y, "YOffset").forGetter(component -> component.offsets.y()),
        legacy(Breasts.BREASTS_OFFSET_Z, "ZOffset").forGetter(component -> component.offsets.y()),
        Codec.BOOL.optionalFieldOf("Jacket", true).forGetter(BreastDataComponent::jacket)
    ).apply(instance, (breastSize, cleavage, x, y, z, jacket) -> new BreastDataComponent(breastSize, cleavage, new Vector3f(x, y, z), jacket, null)));
    private static final Codec<BreastDataComponent> OR_LEGACY = CODEC.withAlternative(LEGACY_CODEC);

    private static MapCodec<Float> legacy(ConfigKey<Float> configKey, String legacyKey) {
        if (configKey.validator() instanceof ConfigRange<Float>(Float minInclusive, Float maxInclusive)) {
            return ExtraCodecs.floatRange(minInclusive, maxInclusive).lenientOptionalFieldOf(legacyKey, configKey.defaultValue());
        }
        throw new IllegalArgumentException("No range defined for config key: " + legacyKey);
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
        return OR_LEGACY.parse(NbtOps.INSTANCE, compoundTag.getCompound(KEY).orElseGet(() -> compoundTag.getCompoundOrEmpty(LEGACY_KEY)))
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
            nbt.store(KEY, CODEC, this);
        });
    }

    public static void removeFromStack(ItemStack stack) {
        if(stack.isEmpty()) return;
        CustomData component = stack.get(DataComponents.CUSTOM_DATA);
        if(component != null) {
            CompoundTag compoundTag = component.copyTag();
            if (compoundTag.contains(KEY) || compoundTag.contains(LEGACY_KEY)) {
                CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> {
                    nbt.remove(KEY);
                    nbt.remove(LEGACY_KEY);
                });
            }
        }
    }

    private BreastDataComponent withComponent(CustomData component) {
        return new BreastDataComponent(breastSize, cleavage, offsets, jacket, component);
    }
}

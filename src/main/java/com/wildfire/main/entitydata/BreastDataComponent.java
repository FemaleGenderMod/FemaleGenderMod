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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.Configuration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * <p>Data component-like class for storing player breast settings on armor equipped onto armor stands</p>
 *
 * <p>Note that while this is treated similarly to any other {@link DataComponents data component} for performance reasons,
 * this is never written as its own component on item stacks, but instead uses the {@link DataComponents#CUSTOM_DATA custom NBT data component}
 * (under the {@code WildfireGender} key) for compatibility with vanilla clients on servers.</p>
 */
public record BreastDataComponent(float breastSize, float cleavage, Vector3f offsets, boolean jacket, @Nullable CustomData nbtComponent) {

	private static final String KEY = "WildfireGender";
	private static final Codec<BreastDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			WildfireHelper.boundedFloat(Configuration.BUST_SIZE)
					.optionalFieldOf("BreastSize", 0f)
					.forGetter(BreastDataComponent::breastSize),
			WildfireHelper.boundedFloat(Configuration.BREASTS_CLEAVAGE)
					.optionalFieldOf("Cleavage", Configuration.BREASTS_CLEAVAGE.getDefault())
					.forGetter(BreastDataComponent::cleavage),
			Codec.BOOL
					.optionalFieldOf("Jacket", true)
					.forGetter(BreastDataComponent::jacket),
			WildfireHelper.boundedFloat(Configuration.BREASTS_OFFSET_X)
					.optionalFieldOf("XOffset", 0f)
					.forGetter(component -> component.offsets.x),
			WildfireHelper.boundedFloat(Configuration.BREASTS_OFFSET_Y)
					.optionalFieldOf("YOffset", 0f)
					.forGetter(component -> component.offsets.y),
			WildfireHelper.boundedFloat(Configuration.BREASTS_OFFSET_Z)
					.optionalFieldOf("ZOffset", 0f)
					.forGetter(component -> component.offsets.y)
		).apply(instance, (breastSize, cleavage, jacket, x, y, z) -> new BreastDataComponent(breastSize, cleavage, new Vector3f(x, y, z), jacket, null))
	);

	public static @Nullable BreastDataComponent fromPlayer(@NotNull Player player, @NotNull PlayerConfig config) {
		if(!config.getGender().canHaveBreasts() || !config.showBreastsInArmor()) {
			return null;
		}

		return new BreastDataComponent(config.getBustSize(), config.getBreasts().getCleavage(), config.getBreasts().getOffsets(),
				player.isModelPartShown(PlayerModelPart.JACKET), null);
	}

	public static @Nullable BreastDataComponent fromComponent(@Nullable CustomData component) {
		if(component == null) {
			return null;
		}

		return CODEC.decode(NbtOps.INSTANCE, component.copyTag().getCompoundOrEmpty(KEY))
				.result()
				.map(Pair::getFirst)
				.map(breastDataComponent -> breastDataComponent.withComponent(component))
				.orElse(null);
	}

	public void write(ItemStack stack) {
		if(stack.isEmpty()) {
			throw new IllegalArgumentException("The provided ItemStack must not be empty");
		}

		CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> nbt.store(KEY, CODEC, this));
	}

	public static void removeFromStack(ItemStack stack) {
		if(stack.isEmpty()) return;
		CustomData component = stack.get(DataComponents.CUSTOM_DATA);
		if(component != null && component.copyTag().contains(KEY)) {
			CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> nbt.remove(KEY));
		}
	}

	private BreastDataComponent withComponent(CustomData component) {
		return new BreastDataComponent(breastSize, cleavage, offsets, jacket, component);
	}
}
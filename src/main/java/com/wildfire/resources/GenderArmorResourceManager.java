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

package com.wildfire.resources;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.WildfireGender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class GenderArmorResourceManager extends SimpleJsonResourceReloadListener<IGenderArmor> {
	private GenderArmorResourceManager() {
		super(IGenderArmor.CODEC, FileToIdConverter.json("wildfire_gender_data"));
	}

	public static final Identifier ID = WildfireGender.id("armor_data");
	public static final GenderArmorResourceManager INSTANCE = new GenderArmorResourceManager();
	private @Unmodifiable Map<Identifier, IGenderArmor> configs = Map.of();

	public static @Nullable IGenderArmor get(Identifier model) {
		return INSTANCE.configs.get(model);
	}

	public static Optional<IGenderArmor> get(ItemStack item) {
		return Optional.ofNullable(item.get(DataComponents.EQUIPPABLE))
				.flatMap(Equippable::assetId)
				.map(ResourceKey::identifier)
				.map(GenderArmorResourceManager::get);
	}

	@Override
	protected void apply(Map<Identifier, IGenderArmor> prepared, ResourceManager manager, ProfilerFiller profiler) {
		this.configs = Collections.unmodifiableMap(prepared);
	}
}

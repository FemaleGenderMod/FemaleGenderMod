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

package com.wildfire.datagen.provider;

import com.mojang.serialization.JsonOps;
import com.wildfire.api.IGenderArmor;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class GenderArmorProvider implements DataProvider {

	private final DataOutput.PathResolver pathResolver;
	private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup;
	private final Map<Identifier, IGenderArmor> armorConfigs = new HashMap<>();

	protected GenderArmorProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
		this.pathResolver = output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "wildfire_gender_data");
		this.registryLookup = registryLookup;
	}

	@Override
	public CompletableFuture<?> run(DataWriter writer) {
		return registryLookup.thenCompose(lookupProvider -> {
			addDefaults(lookupProvider);
			List<CompletableFuture<?>> list = new ArrayList<>(armorConfigs.size());
			for(Map.Entry<Identifier, IGenderArmor> entry : armorConfigs.entrySet()) {
				list.add(DataProvider.writeToPath(
						writer,
						IGenderArmor.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow(),
						pathResolver.resolveJson(entry.getKey())
				));
			}
			return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
		});
	}

	@Override
	public String getName() {
		return "Gender Armor Data";
	}

	protected abstract void addDefaults(RegistryWrapper.WrapperLookup lookupProvider);

	protected void add(RegistryKey<EquipmentAsset> armorMaterial, IGenderArmor armor) {
		add(armorMaterial.getValue(), armor);
	}

	protected void add(Identifier material, IGenderArmor armor) {
		if(armorConfigs.put(material, armor) != null) {
			throw new IllegalArgumentException("Attempted to register multiple entries for " + material);
		}
	}
}

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

package com.wildfire.datagen;

import com.wildfire.api.IGenderArmor;
import com.wildfire.api.impl.GenderArmor;
import com.wildfire.datagen.provider.GenderArmorProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
class WildfireGenderArmorProvider extends GenderArmorProvider {
	public WildfireGenderArmorProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
		super(output, registryLookup);
	}

	@Override
	protected void addDefaults(RegistryWrapper.WrapperLookup lookupProvider) {
		add(EquipmentAssetKeys.LEATHER, new GenderArmor(0.3f, 0.5f));
		add(EquipmentAssetKeys.CHAINMAIL, new GenderArmor(0.5f, 0.2f));
		add(EquipmentAssetKeys.GOLD, new GenderArmor(0.85f, 0, true));
		add(EquipmentAssetKeys.IRON, new GenderArmor(1, 0));
		add(EquipmentAssetKeys.DIAMOND, new GenderArmor(1, 0));
		add(EquipmentAssetKeys.NETHERITE, new GenderArmor(1, 0));
		add(EquipmentAssetKeys.ELYTRA, IGenderArmor.EMPTY);
	}
}

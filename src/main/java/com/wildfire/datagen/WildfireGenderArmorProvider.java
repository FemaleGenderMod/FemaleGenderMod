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
import com.wildfire.api.data.GenderArmorProvider;
import com.wildfire.main.WildfireGender;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
class WildfireGenderArmorProvider extends GenderArmorProvider {
	public WildfireGenderArmorProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, WildfireGender.MODID);
	}

	@Override
	protected void addDefaults(HolderLookup.Provider lookupProvider) {
        add(EquipmentAssets.CHAINMAIL, new GenderArmor(0.5F, 0.2F));
        add(EquipmentAssets.COPPER, new GenderArmor(1, 0));
        add(EquipmentAssets.DIAMOND, new GenderArmor(1, 0));
        add(EquipmentAssets.ELYTRA, IGenderArmor.EMPTY);
		add(EquipmentAssets.GOLD, new GenderArmor(0.85f, 0, true));
		add(EquipmentAssets.IRON, new GenderArmor(1, 0));
        add(EquipmentAssets.LEATHER, new GenderArmor(0.3F, 0.5F));
		add(EquipmentAssets.NETHERITE, new GenderArmor(1, 0));
	}
}

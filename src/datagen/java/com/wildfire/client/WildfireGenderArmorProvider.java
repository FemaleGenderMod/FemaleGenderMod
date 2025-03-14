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

package com.wildfire.client;

import com.wildfire.api.data.GenderArmorProvider;
import com.wildfire.api.impl.GenderArmor;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterials;

public class WildfireGenderArmorProvider extends GenderArmorProvider {

    public WildfireGenderArmorProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, WildfireGender.MODID);
    }

    @Override
    protected void addDefaults(HolderLookup.Provider lookupProvider) {
        add(ArmorMaterials.LEATHER, WildfireHelper.LEATHER);
        add(ArmorMaterials.CHAIN, WildfireHelper.CHAIN_MAIL);
        add(ArmorMaterials.GOLD, WildfireHelper.GOLD);
        add(ArmorMaterials.IRON, WildfireHelper.IRON);
        add(ArmorMaterials.DIAMOND, WildfireHelper.DIAMOND);
        add(ArmorMaterials.NETHERITE, WildfireHelper.NETHERITE);
        //TODO - 1.21.4: EquipmentAssets.ELYTRA
        add(ResourceLocation.withDefaultNamespace("elytra"), GenderArmor.EMPTY);
    }
}
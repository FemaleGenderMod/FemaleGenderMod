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

package com.wildfire.api.data;

import com.wildfire.api.IGenderArmor;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.EquipmentAsset;

public abstract class GenderArmorProvider implements DataProvider {

    private final Map<Identifier, IGenderArmor> armorConfigs = new HashMap<>();
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final PathProvider pathProvider;
    private final String modid;

    protected GenderArmorProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.registries = registries;
        this.pathProvider = output.createPathProvider(Target.RESOURCE_PACK, "wildfire_gender_data");
        this.modid = output.getModId();
    }

    @Override
    public final String getName() {
        return "Gender Armor Provider: " + modid;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput cachedOutput) {
        return this.registries.thenCompose(lookupProvider -> {
            addDefaults(lookupProvider);
            List<CompletableFuture<?>> list = new ArrayList<>(armorConfigs.size());
            for (Map.Entry<Identifier, IGenderArmor> entry : armorConfigs.entrySet()) {
                list.add(DataProvider.saveStable(cachedOutput, lookupProvider, IGenderArmor.CODEC, entry.getValue(), pathProvider.json(entry.getKey())));
            }
            return CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
        });
    }

    protected abstract void addDefaults(HolderLookup.Provider lookupProvider);

    protected void add(ArmorMaterial armorMaterial, IGenderArmor armor) {
        add(armorMaterial.assetId(), armor);
    }

    protected void add(ResourceKey<EquipmentAsset> armorMaterial, IGenderArmor armor) {
        add(armorMaterial.identifier(), armor);
    }

    protected void add(Identifier material, IGenderArmor armor) {
        if (armorConfigs.put(material, armor) != null) {
            throw new IllegalArgumentException("Attempted to register multiple entries for " + material);
        }
    }
}

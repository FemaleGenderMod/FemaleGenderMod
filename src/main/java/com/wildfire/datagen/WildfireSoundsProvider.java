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

import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireSounds;
import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
class WildfireSoundsProvider extends FabricSoundsProvider {
	public WildfireSoundsProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
		super(dataOutput, registryLookup);
	}

	@Override
	protected void configure(RegistryWrapper.WrapperLookup registryLookup, SoundExporter exporter) {
		exporter.add(
				WildfireSounds.FEMALE_HURT,
				SoundTypeBuilder.of()
						.category(SoundCategory.PLAYERS)
						.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.of(WildfireGender.MODID, "female_damage")))
						.sound(SoundTypeBuilder.EntryBuilder.ofFile(Identifier.of(WildfireGender.MODID, "female_damage2")))
		);
	}

	@Override
	public String getName() {
		return "Sounds";
	}
}

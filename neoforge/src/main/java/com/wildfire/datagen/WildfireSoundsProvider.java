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

import com.wildfire.common.WildfireGender;
import java.util.Arrays;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
class WildfireSoundsProvider extends SoundDefinitionsProvider {
	WildfireSoundsProvider(PackOutput output) {
		super(output, WildfireGender.MODID);
	}

	@Override
    public void registerSounds() {
        WildfireSoundData.generateSounds(this::add, (subtitleTranslationKey, sounds) -> definition().subtitle(subtitleTranslationKey)
            .with(Arrays.stream(sounds).map(SoundDefinitionsProvider::sound).toArray(SoundDefinition.Sound[]::new)));
	}
}

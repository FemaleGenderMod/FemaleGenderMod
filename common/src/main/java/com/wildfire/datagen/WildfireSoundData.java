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
import com.wildfire.main.WildfireLang;
import com.wildfire.main.WildfireSounds;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class WildfireSoundData {

    private WildfireSoundData() {
    }

    public static <RESULT> void generateSounds(BiConsumer<SoundEvent, RESULT> builder, SoundCreator<RESULT> soundCreator) {
        builder.accept(WildfireSounds.INSTANCE.femaleHurt(), soundCreator.create(
            WildfireLang.HURT_SOUND_SUBTITLE.getTranslationKey(),
            WildfireGender.id("female_damage"),
            WildfireGender.id("female_damage2"))
        );
    }

    @FunctionalInterface
    public interface SoundCreator<RESULT> {

        RESULT create(String subtitleTranslationKey, Identifier... sounds);
    }
}

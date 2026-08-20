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

package com.wildfire.neoforge.datagen;

import com.wildfire.datagen.lang.WildfireLangData;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
class WildfireLangProvider extends LanguageProvider {

    private final WildfireLangData langData;

    WildfireLangProvider(PackOutput output, String modId) {
        super(output, modId, "en_us");
        this.langData = new WildfireLangData(output, modId);
    }

    @Override
    protected void addTranslations() {
        langData.generateTranslations(this::add);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return super.run(cache).thenCompose(_ -> langData.run(cache));
    }
}

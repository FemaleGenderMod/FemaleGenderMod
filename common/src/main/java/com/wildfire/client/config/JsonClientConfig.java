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

package com.wildfire.client.config;

import com.mojang.serialization.JsonOps;
import com.wildfire.common.config.Configuration;
import org.jspecify.annotations.Nullable;

class JsonClientConfig implements ClientConfig {

    private final Configuration<ClientConfigInstance> cfgFile = new Configuration<>(".", "female_gender_mod", ClientConfigInstance.CODEC);
    //Note: Theoretically this can never fail so it is safe to use getOrThrow as everything in the codec has orElse(default)
    private ClientConfigInstance config = ClientConfigInstance.CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()).getOrThrow();

    JsonClientConfig() {
        if (!cfgFile.exists()) {
            save();
        }
    }

    @Override
    public ClientConfigInstance current() {
        return config;
    }

    @Override
    public void load(@Nullable Object ignored) {
        config = cfgFile.load();
    }

    @Override
    public void save() {
        cfgFile.save(config);
    }
}

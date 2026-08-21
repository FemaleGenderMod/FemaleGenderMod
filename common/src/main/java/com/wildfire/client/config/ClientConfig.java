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

import com.wildfire.common.WildfireHelper;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public interface ClientConfig {

    //Get the service loader provided one, or fall back to using a json config (for cases like fabric)
    ClientConfig INSTANCE = Objects.requireNonNullElseGet(WildfireHelper.getOptionalService(ClientConfig.class), JsonClientConfig::new);

    static ClientConfigInstance config() {
        return INSTANCE.current();
    }

    ClientConfigInstance current();

    void load(@Nullable Object data);

    void save();

    // region Debug options
    default boolean displayOwnNameTag() {
        return ClientConfigInstance.DISPLAY_OWN_NAMETAG;
    }
    // endregion
}

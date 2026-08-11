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

package com.wildfire.common.networking.packets.hello;

import com.wildfire.common.WildfireGender;
import com.wildfire.common.networking.WildfireSync;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ConfigurationTask;

public abstract class AbstractHelloConfigurationTask implements ConfigurationTask {

    private static final Identifier ID = WildfireGender.id("hello");
    public static final Type TYPE = new Type(ID.toString());

    protected ClientboundSyncHelloPacket createPacket() {
        WildfireGender.LOGGER.debug(WildfireSync.MARKER, "Sending hello packet to client");
        return new ClientboundSyncHelloPacket();
    }

    @Override
    public final Type type() {
        return TYPE;
    }
}

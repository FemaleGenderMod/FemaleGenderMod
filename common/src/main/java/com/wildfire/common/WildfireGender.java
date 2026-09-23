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

package com.wildfire.common;

import com.wildfire.api.WildfireAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WildfireGender {
    public static final Logger LOGGER = LoggerFactory.getLogger("FemaleGenderMod");

    public static final Component COMMAND_PREFIX = WildfireLang.GENERIC_BRACKETS
        .translateColored(TextColor.GRAY, WildfireLang.GENERIC_CONCAT.translate(
            WildfireLang.MISC_F.translateColored(TextColor.LIGHT_PURPLE),
            WildfireLang.MISC_GM.translateColored(TextColor.WHITE)
        ));

    public static String getModVersion() {
        return LoaderAgnostics.INSTANCE.getModVersion(WildfireAPI.MODID);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(WildfireAPI.MODID, path);
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> clientBoundPacket(String path) {
        return packet("clientbound/" + path);
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> serverBoundPacket(String path) {
        return packet("serverbound/" + path);
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> packet(String path) {
        return new CustomPacketPayload.Type<>(id(path));
    }
}

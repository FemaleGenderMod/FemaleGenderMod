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

package com.wildfire.main.cloud;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.wildfire.main.WildfireGender;
import com.wildfire.mixins.accessors.YggdrasilMinecraftSessionServiceAccessor;
import net.minecraft.client.MinecraftClient;

import java.util.Objects;

public final class CloudUtils {
	private CloudUtils() {
		throw new UnsupportedOperationException();
	}

	private static final String EXPECTED_YGGDRASIL_BASE_URL = "https://sessionserver.mojang.com/session/minecraft/";

	static MinecraftSessionService getSessionService() {
		return MinecraftClient.getInstance().getApiServices().sessionService();
	}

	static boolean hasTheSessionServiceBeenTamperedWith() {
		var sessionService = getSessionService();
		// minecraft normally uses yggdrasil here; if this is not the case, either mojang has made some serious
		// changes to sessions, or someone is replacing this with something that shouldn't be here.
		if(sessionService.getClass() != YggdrasilMinecraftSessionService.class) {
			WildfireGender.LOGGER.info("Detected likely session service tampering; got {} instead of the expected Yggdrasil session service", sessionService.getClass());
			return true;
		} else {
			// additionally verify for potential cracked client tampering here
			var accessor = (YggdrasilMinecraftSessionServiceAccessor) sessionService;
			if(!Objects.equals(accessor.getBaseUrl(), EXPECTED_YGGDRASIL_BASE_URL)) {
				WildfireGender.LOGGER.info("Detected likely session service tampering; Yggdrasil base URL is not the expected Mojang-provided value");
				return true;
			}
		}

		return false;
	}
}
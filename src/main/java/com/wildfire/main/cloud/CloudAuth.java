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

import net.minecraft.client.Minecraft;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

record CloudAuth(boolean success, String token, UUID account, Instant expires) {
	// Assume that authentication tokens have already expired if they're due to expire within 30 seconds to account
	// for potential clock drift and network latency
	static final Duration AUTH_INVALIDATION_ADJUSTMENT = Duration.ofSeconds(30);

	boolean isExpired() {
		return expires.minus(AUTH_INVALIDATION_ADJUSTMENT).isBefore(Instant.now());
	}

	boolean isInvalidForClientPlayer() {
		var client = Minecraft.getInstance();
		return client.player == null || !account.equals(client.player.getUUID());
	}
}

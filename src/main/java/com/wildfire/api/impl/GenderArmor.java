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

package com.wildfire.api.impl;

import com.wildfire.api.IBreastArmorTexture;
import com.wildfire.api.IGenderArmor;
import org.jetbrains.annotations.ApiStatus;

/**
 * Default record implementation of {@link IGenderArmor} used for resource pack entries
 *
 * @see IGenderArmor
 */
public record GenderArmor(
		float physicsResistance,
		float tightness,
		boolean coversBreasts,
		boolean alwaysHidesBreasts,
		boolean armorStandsCopySettings,
		IBreastArmorTexture texture
) implements IGenderArmor {
	/**
	 * @deprecated Use {@link IGenderArmor#DEFAULT} instead
	 */
	@Deprecated(forRemoval = true)
	@ApiStatus.ScheduledForRemoval(inVersion = "First release of 26.1")
	public static final IGenderArmor DEFAULT = IGenderArmor.DEFAULT;

	/**
	 * @deprecated Use {@link IGenderArmor#EMPTY} instead
	 */
	@Deprecated(forRemoval = true)
	@ApiStatus.ScheduledForRemoval(inVersion = "First release of 26.1")
	public static final IGenderArmor EMPTY = IGenderArmor.EMPTY;
}

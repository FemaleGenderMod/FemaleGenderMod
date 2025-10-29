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

package com.wildfire.main.config.enums;

import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum SyncVerbosity implements StringRepresentable {
	DEFAULT,
	SHOW_FETCHES;

	public static final IntFunction<SyncVerbosity> BY_ID = ByIdMap.continuous(SyncVerbosity::ordinal, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);

	@Override
	public String getSerializedName() {
		return toString();
	}

	public static class SyncVerbosityArgumentType extends StringRepresentableArgument<SyncVerbosity> {
		public SyncVerbosityArgumentType() {
			super(StringRepresentable.fromEnum(SyncVerbosity::values), SyncVerbosity::values);
		}
	}
}

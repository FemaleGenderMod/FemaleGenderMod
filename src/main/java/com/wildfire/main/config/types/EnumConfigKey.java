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

package com.wildfire.main.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.function.IntFunction;

public class EnumConfigKey<TYPE extends Enum<TYPE>> extends ConfigKey<TYPE> {
	private final IntFunction<TYPE> ordinal;

	public EnumConfigKey(String key, TYPE defaultValue, IntFunction<TYPE> ordinalMapper) {
		super(key, defaultValue);
		this.ordinal = ordinalMapper;
	}

	@Override
	protected TYPE read(JsonElement element) {
		if(element instanceof JsonPrimitive prim && prim.isNumber()) {
			return ordinal.apply(prim.getAsInt());
		}
		return defaultValue;
	}

	@Override
	public void save(JsonObject object, TYPE value) {
		object.addProperty(key, value.ordinal());
	}
}

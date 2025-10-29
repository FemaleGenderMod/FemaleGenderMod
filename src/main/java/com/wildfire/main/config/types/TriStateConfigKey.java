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
import net.minecraft.util.TriState;

public class TriStateConfigKey extends ConfigKey<TriState> {

	public TriStateConfigKey(String key, TriState defaultValue) {
		super(key, defaultValue);
	}

	public TriStateConfigKey(String key) {
		this(key, TriState.DEFAULT);
	}

	@Override
	protected TriState read(JsonElement element) {
		if(element.isJsonNull()) {
			return TriState.DEFAULT;
		}
		if(element instanceof JsonPrimitive primitive) {
			if(primitive.isBoolean()) {
				return primitive.getAsBoolean() ? TriState.TRUE : TriState.FALSE;
			}
			if(primitive.isString()) {
				try {
					return TriState.valueOf(primitive.getAsString());
				} catch(IllegalArgumentException ignored) {
				}
			}
		}
		return defaultValue;
	}

	@Override
	public void save(JsonObject object, TriState value) {
		object.addProperty(key, value.toString());
	}
}
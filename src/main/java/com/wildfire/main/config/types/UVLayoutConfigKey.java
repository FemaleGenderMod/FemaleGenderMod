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
import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;

public class UVLayoutConfigKey extends ConfigKey<UVLayout> {

	public UVLayoutConfigKey(String key, UVLayout defaultValue) {
		super(key, defaultValue);
	}

	@Override
	protected UVLayout read(JsonElement element) {
		if (!element.isJsonObject()) return defaultValue.copy();

		JsonObject obj = element.getAsJsonObject();
		UVLayout layout = new UVLayout();

		for (UVDirection dir : UVDirection.values()) {
			JsonElement sideElem = obj.get(dir.getSaveName());
			if (sideElem != null && sideElem.isJsonObject()) {
				JsonObject quadObj = sideElem.getAsJsonObject();
				int x1 = quadObj.get("x1").getAsInt();
				int y1 = quadObj.get("y1").getAsInt();
				int x2 = quadObj.get("x2").getAsInt();
				int y2 = quadObj.get("y2").getAsInt();
				layout.put(dir, new UVQuad(x1, y1, x2, y2));
			}
		}

		return layout;
	}

	@Override
	public void save(JsonObject object, UVLayout value) {
		JsonObject layoutObj = new JsonObject();
		for (UVDirection dir : UVDirection.values()) {
			UVQuad quad = value.get(dir);
			if (quad != null) {
				JsonObject quadObj = new JsonObject();
				quadObj.addProperty("x1", quad.x1());
				quadObj.addProperty("y1", quad.y1());
				quadObj.addProperty("x2", quad.x2());
				quadObj.addProperty("y2", quad.y2());
				layoutObj.add(dir.getSaveName(), quadObj);
			}
		}
		object.add(key, layoutObj);
	}

	@Override
	public UVLayout getDefault() {
		return defaultValue.copy();
	}
}

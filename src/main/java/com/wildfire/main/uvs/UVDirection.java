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

package com.wildfire.main.uvs;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.IntFunction;

public enum UVDirection {
	EAST("east", "", "E", 0xFFFF0000, new Vec3i(1, 0, 0)),
	WEST("west", "", "W", 0xFF00FF00, new Vec3i(-1, 0, 0)),
	DOWN("down", "wildfire_gender.uv_editor.faces.bottom", "D", 0xFF0000FF, new Vec3i(0, -1, 0)),
	UP("up", "wildfire_gender.uv_editor.faces.top", "U", 0xFF00FFFF, new Vec3i(0, 1, 0)),
	NORTH("north", "wildfire_gender.uv_editor.faces.front", "N", 0xFFFF00FF, new Vec3i(0, 0, -1));

	private final String unlocalizedName;
	private final String shortName;
	private final String saveName;
	private final int baseColor;
	private final Vector3fc floatVector;

	public static final IntFunction<UVDirection> BY_ID = ByIdMap.continuous(UVDirection::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
	public static final StreamCodec<ByteBuf, UVDirection> PACKET_CODEC = ByteBufCodecs.idMapper(BY_ID, UVDirection::ordinal);

	UVDirection(String saveName, String unlocalizedName, String shortName, int baseColor, Vec3i vector) {
		this.unlocalizedName = unlocalizedName;
		this.saveName = saveName;
		this.shortName = shortName;
		this.baseColor = baseColor;
		this.floatVector = new Vector3f((float)vector.getX(), (float)vector.getY(), (float)vector.getZ());
	}

	public int getFaceColor(boolean faded) {
		if (!faded) return baseColor;

		int alpha = 0x33;
		int rgb = baseColor & 0x00FFFFFF;
		return (alpha << 24) | rgb;
	}

	public Vector3f getUnitVector() {
		return new Vector3f(this.floatVector);
	}

	public Component getDirectionText(BreastTypes type) {

		if (this == EAST || this == WEST) {
			String key = (type == BreastTypes.LEFT || type == BreastTypes.LEFT_OVERLAY)
					? "wildfire_gender.uv_editor.faces.inner"
					: "wildfire_gender.uv_editor.faces.outer";
			return Component.translatable(key);
		}

		if (unlocalizedName != null && !unlocalizedName.isEmpty()) {
			return Component.translatable(unlocalizedName);
		}

		return Component.literal(saveName);
	}

	public String getSaveName() {
		return saveName;
	}

	public String getShortName() {
		return shortName;
	}

	public String getUnlocalizedName() {
		return unlocalizedName;
	}
}

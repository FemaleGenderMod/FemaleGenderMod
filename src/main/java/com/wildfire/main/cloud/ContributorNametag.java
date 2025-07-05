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

import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireGenderClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public record ContributorNametag(String text, @Nullable Integer color) {
	// requireNonNull() to help IDEs figure out that @Nullable only applies to non-color Formatting entries
	private static final int DEFAULT_COLOR = Objects.requireNonNull(Formatting.GOLD.getColorValue());

	public int getColor() {
		return color == null ? DEFAULT_COLOR : color;
	}

	public Text asText() {
		return Text.literal(this.text).withColor(getColor());
	}

	public static @Nullable Integer getContributorColor(UUID uuid) {
		ContributorNametag custom;
		try {
			custom = WildfireGenderClient.CONTRIBUTOR_NAMETAGS.getNow(Map.of()).get(uuid);
		} catch(Exception e) {
			custom = null;
		}

		if(custom != null && custom.color() != null) {
			return custom.color();
		} else if(WildfireGender.CREATOR_UUID.equals(uuid)) {
			return Formatting.LIGHT_PURPLE.getColorValue();
		} else if(WildfireGender.CONTRIBUTOR_UUIDS.contains(uuid)) {
			return Formatting.GOLD.getColorValue();
		}

		return null;
	}
}

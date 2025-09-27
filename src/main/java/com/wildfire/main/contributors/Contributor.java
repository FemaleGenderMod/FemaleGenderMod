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

package com.wildfire.main.contributors;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public record Contributor(
		// TODO this technically supports multiple roles due to this using a bitmask, but any additional roles other than
		//		the topmost one defined in Role is currently ignored
		int roles,
		@Nullable Integer color,
		@Nullable String name,
		@SerializedName("show_in_credits")
		@Nullable Boolean showInCredits
) {
	// requireNonNull() to help IDEs figure out that @Nullable only applies to non-color Formatting entries
	private static final int DEFAULT_COLOR = Objects.requireNonNull(Formatting.GOLD.getColorValue());

	public int getColor() {
		if(color != null) {
			return color;
		}
		return getRole().getColor();
	}

	public @Nullable Text asText() {
		return getRole().nametag().withColor(getColor());
	}

	public @NotNull Role getRole() {
		if(roles == 0) {
			return Role.GENERIC;
		}

		for(var role : Role.values()) {
			if(role.isIn(this.roles)) {
				return role;
			}
		}

		return Role.GENERIC;
	}

	public enum Role {
		// New entries should be added at the bottom of this enum, as otherwise existing bitmasks will
		// reference the wrong entries
		MOD_CREATOR(Formatting.LIGHT_PURPLE.getColorValue()),
		// TODO should this have a special color as well? this is currently set to an light blue/purple-ish color
		FABRIC_MAINTAINER(0xA78FFF),
		NEOFORGE_MAINTAINER(0xA78FFF),
		DEVELOPER,
		TRANSLATOR(0x66CCFF),
		MASCOT,
		VOICE_ACTOR_FEMALE,
		GENERIC,
		;

		private final @Nullable Integer color;

		Role(@Nullable Integer color) {
			this.color = color;
		}

		Role() {
			this(null);
		}

		public int bit() {
			return 1 << ordinal();
		}

		public boolean isIn(int bitmask) {
			return (bitmask & bit()) == bit();
		}

		public int getColor() {
			return color == null ? DEFAULT_COLOR : color;
		}

		public @NotNull MutableText withColor(@NotNull MutableText text) {
			Preconditions.checkNotNull(text);
			if(color != null) {
				return text.withColor(color);
			}
			return text;
		}

		public @NotNull MutableText withColor(@NotNull MutableText text, @NotNull Formatting defaultColor) {
			Preconditions.checkNotNull(text);
			if(color != null) {
				return text.withColor(color);
			}

			Preconditions.checkNotNull(defaultColor.getColorValue());
			return text.withColor(defaultColor.getColorValue());
		}

		public @NotNull MutableText nametag() {
			return Text.translatable("wildfire_gender.contributor.role." + name().toLowerCase(Locale.ROOT));
		}

		public @NotNull MutableText shortName() {
			return Text.translatable("wildfire_gender.contributor.role." + name().toLowerCase(Locale.ROOT) + ".short");
		}
	}
}

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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

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
    private static final int DEFAULT_COLOR = 0xFFAA00; // ChatFormatting.GOLD

    public int getColor() {
        if(color != null) {
            return color;
        }
        return getRole().getColor();
    }

    public Component asText() {
        return getRole().nametag().withColor(getColor());
    }

    public Role getRole() {
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
        MOD_CREATOR(0, 0xFF55FF), // ChatFormatting.LIGHT_PURPLE
        FABRIC_MAINTAINER(1, 0xA78FFF),
        NEOFORGE_MAINTAINER(2, 0xA78FFF),
        CI_MAINTAINER(8, 0x50C878),
        DEVELOPER(3),
        TRANSLATOR(4, 0x66CCFF),
        MASCOT(5),
        VOICE_ACTOR_FEMALE(6),
        GENERIC(7),
        ;

        private final int bit;
        private final @Nullable Integer color;

        Role(int bit, @Nullable Integer color) {
            this.bit = 1 << bit;
            this.color = color;
        }

        Role(int bit) {
            this(bit, null);
        }

        public int bit() {
            return bit;
        }

        public boolean isIn(int bitmask) {
            return (bitmask & bit()) == bit();
        }

        public int getColor() {
            return color == null ? DEFAULT_COLOR : color;
        }

        public MutableComponent withColor(MutableComponent text) {
            Preconditions.checkNotNull(text);
            if(color != null) {
                return text.withColor(color);
            }
            return text;
        }

        public MutableComponent nametag() {
            return Component.translatable("wildfire_gender.contributor.role." + name().toLowerCase(Locale.ROOT));
        }

        public MutableComponent shortName() {
            return Component.translatable("wildfire_gender.contributor.role." + name().toLowerCase(Locale.ROOT) + ".short");
        }
    }
}

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

//~ color_as_rgb !named_text_color *disable replacing with ChatFormatting, and instead enable replacing with RGB representation for this file*
package com.wildfire.client.contributors;

import com.google.common.base.Preconditions;
import com.wildfire.common.WildfireLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;

public record Contributor(String name, Role role, boolean showInCredits) {
    private static final TextColor DEFAULT_COLOR = TextColor.GOLD;

    public TextColor getColor() {
        return role.getColor();
    }

    public Component asText() {
        return role.langEntry.translateColored(getColor());
    }

    public enum Role {
        MOD_CREATOR(WildfireLang.CONTRIBUTOR_ROLE_MOD_CREATOR, TextColor.LIGHT_PURPLE),
        FABRIC_MAINTAINER(WildfireLang.CONTRIBUTOR_ROLE_FABRIC_MAINTAINER, 0xA78FFF),
        NEOFORGE_MAINTAINER(WildfireLang.CONTRIBUTOR_ROLE_NEO_MAINTAINER, 0xA78FFF),
        CI_MAINTAINER(WildfireLang.CONTRIBUTOR_ROLE_CI_MAINTAINER, 0x50C878),
        DEVELOPER(WildfireLang.CONTRIBUTOR_ROLE_DEVELOPER),
        TRANSLATOR(WildfireLang.CONTRIBUTOR_ROLE_TRANSLATOR, 0x66CCFF),
        MASCOT(WildfireLang.CONTRIBUTOR_ROLE_MASCOT),
        VOICE_ACTOR_FEMALE(WildfireLang.CONTRIBUTOR_ROLE_FEMALE_VOICE_ACTOR),
        GENERIC(WildfireLang.CONTRIBUTOR_ROLE_GENERIC),
        ;

        private final WildfireLang langEntry;
        private final @Nullable TextColor color;

        Role(WildfireLang langEntry, int color) {
            this(langEntry, TextColor.fromRgb(color));
        }

        Role(WildfireLang langEntry, @Nullable TextColor color) {
            this.langEntry = langEntry;
            this.color = color;
        }

        Role(WildfireLang langEntry) {
            this(langEntry, null);
        }

        public TextColor getColor() {
            return color == null ? DEFAULT_COLOR : color;
        }

        public MutableComponent withColor(MutableComponent text) {
            Preconditions.checkNotNull(text);
            //~ if >=26.2 'getColor().getValue()' -> 'getColor()'
            return text.withColor(getColor());
        }

        public MutableComponent shortName() {
            return langEntry.translateShort();
        }
    }
}

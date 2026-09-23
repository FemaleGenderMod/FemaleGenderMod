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

package com.wildfire.api;

import com.mojang.serialization.Codec;
import com.wildfire.common.WildfireLang;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public enum Gender implements StringRepresentable, NamedEnum {
    //~ color_as_rgb !named_text_color
    // NOTE: The order of these should remain unchanged! Changing these WILL modify player configs!
    FEMALE("female", WildfireLang.LABEL_FEMALE, TextColor.LIGHT_PURPLE, true),
    MALE("male", WildfireLang.LABEL_MALE, TextColor.BLUE, false),
    OTHER("other", WildfireLang.LABEL_OTHER, TextColor.GREEN, true);
    //~ !color_as_rgb named_text_color

    public static final IntFunction<Gender> BY_ID = ByIdMap.continuous(Gender::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final Codec<Gender> CODEC = StringRepresentable.fromEnum(Gender::values);
    public static final Codec<Gender> BY_ID_CODEC = ExtraCodecs.idResolverCodec(Gender::ordinal, BY_ID, 0);
    public static final Codec<Gender> CODEC_OR_LEGACY = CODEC.withAlternative(BY_ID_CODEC);
    public static final StreamCodec<ByteBuf, Gender> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Gender::ordinal);

    private final String saveName;
    private final WildfireLang name;
    private final TextColor color;
    private final boolean canHaveBreasts;

    Gender(String saveName, WildfireLang displayName, TextColor color, boolean canHaveBreasts) {
        this.saveName = saveName;
        this.name = displayName;
        this.color = color;
        this.canHaveBreasts = canHaveBreasts;
    }

    @Override
    public String getSerializedName() {
        return saveName;
    }

    @Override
    public Component getDisplayName() {
        return name.translateColored(color);
    }

    public boolean canHaveBreasts() {
        return canHaveBreasts;
    }

    public Gender next() {
        return switch(this) {
            case MALE -> FEMALE;
            case FEMALE -> OTHER;
            case OTHER -> MALE;
        };
    }

    @Deprecated
    public String getTranslationKey() {
        return name.getTranslationKey();
    }
}

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

package com.wildfire.main.entitydata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.config.value.ConfigKey;
import com.wildfire.main.config.value.ConfigValue;
import com.wildfire.main.uvs.UVs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/// A version of [EntityConfig] backed by a [Configuration] for use with players
public class PlayerConfig extends EntityConfig {

    private static final ConfigKey<Boolean> SHOW_IN_ARMOR = ConfigKey.create("show_in_armor", true);
    private static final ConfigKey<Boolean> HOLIDAY_THEMES = ConfigKey.create("holiday_themes", true);

    public static final Codec<PlayerConfig> CODEC = RecordCodecBuilder.create(instance -> codecGroup(instance)
        .and(Sounds.CODEC.forGetter(config -> config.sounds))
        .and(SHOW_IN_ARMOR.codecOrDefault().forGetter(config -> config.showBreastsInArmor.get()))
        .and(HOLIDAY_THEMES.codecOrDefault().forGetter(config -> config.holidayThemes.get()))
        .apply(instance, PlayerConfig::new));
    // remember to update SyncHelloPacket.VERSION when modifying this codec if the changes result in a change
    // to the underlying packet structure
    public static final StreamCodec<ByteBuf, PlayerConfig> STREAM_CODEC = StreamCodec.composite(
        //From EntityConfig
        Gender.STREAM_CODEC, config -> config.gender.get(),
        Breasts.STREAM_CODEC, config -> config.breasts,
        UVs.STREAM_CODEC, config -> config.uvs,
        //From PlayerConfig
        Sounds.STREAM_CODEC, config -> config.sounds,
        ByteBufCodecs.BOOL, config -> config.showBreastsInArmor.get(),
        ByteBufCodecs.BOOL, config -> config.holidayThemes.get(),
        PlayerConfig::new
    );

    public final ConfigValue<Boolean> showBreastsInArmor;
    public final ConfigValue<Boolean> holidayThemes;
    public final Sounds sounds;

    private PlayerConfig(Gender gender, Breasts breasts, UVs uvs, Sounds sounds, boolean showBreastsInArmor, boolean holidayThemes) {
        this.sounds = sounds;
        this.showBreastsInArmor = SHOW_IN_ARMOR.createValueHandler(showBreastsInArmor);
        this.holidayThemes = HOLIDAY_THEMES.createValueHandler(holidayThemes);
        super(gender, breasts, uvs);
    }
}

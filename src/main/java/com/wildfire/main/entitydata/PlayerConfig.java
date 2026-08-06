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
import com.mojang.serialization.JsonOps;
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

    private static final ConfigKey<Boolean> SHOW_IN_ARMOR = ConfigKey.DEFAULT_TRUE;
    private static final ConfigKey<Boolean> HOLIDAY_THEMES = ConfigKey.DEFAULT_TRUE;

    public static final Codec<PlayerConfig> CODEC = RecordCodecBuilder.create(instance -> codecGroup(instance)
        .and(Sounds.CODEC_OR_LEGACY.forGetter(config -> config.sounds))
        .and(SHOW_IN_ARMOR.codecOrDefault("show_in_armor").forGetter(config -> config.showBreastsInArmor.get()))
        .and(HOLIDAY_THEMES.codecOrDefault("holiday_themes").forGetter(config -> config.holidayThemes.get()))
        .apply(instance, PlayerConfig::new));
    // remember to update SyncHelloPacket.VERSION when modifying this codec if the changes result in a change
    // to the underlying packet structure
    public static final StreamCodec<ByteBuf, PlayerConfig> STREAM_CODEC = StreamCodec.composite(
        //From EntityConfig
        Gender.STREAM_CODEC, config -> config.gender.get(),
        //TODO: Technically if the gender is male, none of this other stuff needs to be synced
        Breasts.STREAM_CODEC, config -> config.breasts,
        UVs.STREAM_CODEC, config -> config.uvs,
        //From PlayerConfig
        Sounds.STREAM_CODEC, config -> config.sounds,
        ByteBufCodecs.BOOL, config -> config.showBreastsInArmor.get(),
        ByteBufCodecs.BOOL, config -> config.holidayThemes.get(),
        PlayerConfig::new
    );

    //TODO: Remove this hacky way of enforcing encoding using the old syntax, by changing the serialization to CODEC once the cloud server can support doing it on its side
    public static final Codec<PlayerConfig> CLOUD_SYNC_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Gender.BY_ID_CODEC.fieldOf("gender").orElseGet(GENDER::defaultValue).forGetter(config -> config.gender.get()),
        Breasts.LEGACY_CODEC.forGetter(config -> config.breasts),
        //Note: it is safe to use the newer codec syntax here as the cloud server doesn't care about UVs
        UVs.CODEC_OR_LEGACY.forGetter(config -> config.uvs),
        Sounds.LEGACY_CODEC.forGetter(config -> config.sounds),
        SHOW_IN_ARMOR.codecOrDefault("show_in_armor").forGetter(config -> config.showBreastsInArmor.get()),
        HOLIDAY_THEMES.codecOrDefault("holiday_themes").forGetter(config -> config.holidayThemes.get())
    ).apply(instance, PlayerConfig::new));

    public static PlayerConfig createDefault() {
        //TODO: Re-evaluate this? I think it is the thing that makes the most sense
        //TODO: If not success do we want to log it failed? Can it even fail? Given the fact everything has orDefault
        return CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()).getOrThrow();
    }

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

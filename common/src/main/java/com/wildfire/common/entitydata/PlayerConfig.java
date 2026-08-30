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

package com.wildfire.common.entitydata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.common.config.Configuration;
import com.wildfire.api.Gender;
import com.wildfire.common.config.value.ConfigKey;
import com.wildfire.common.config.value.ConfigValue;
import com.wildfire.common.config.UVs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

/// A version of [EntityConfig] backed by a [Configuration] for use with players
public class PlayerConfig extends EntityConfig {

    private static final ConfigKey<Boolean> SHOW_IN_ARMOR = ConfigKey.DEFAULT_TRUE;

    public static final Codec<PlayerConfig> CODEC = RecordCodecBuilder.create(instance -> codecGroup(instance)
        .and(Sounds.CODEC_OR_LEGACY.forGetter(config -> config.sounds))
        .and(SHOW_IN_ARMOR.codecOrDefault("show_in_armor").forGetter(config -> config.showBreastsInArmor.get()))
        .apply(instance, PlayerConfig::new));
    // remember to update SyncHelloPacket.VERSION when modifying this codec if the changes result in a change
    // to the underlying packet structure
    public static final StreamCodec<ByteBuf, PlayerConfig> STREAM_CODEC = StreamCodec.composite(
        //From EntityConfig
        GENDER.streamCodec(), config -> config.gender.get(),
        Breasts.STREAM_CODEC, config -> config.breasts,
        UVs.STREAM_CODEC, config -> config.uvs,
        //From PlayerConfig
        Sounds.STREAM_CODEC, config -> config.sounds,
        SHOW_IN_ARMOR.streamCodec(), config -> config.showBreastsInArmor.get(),
        PlayerConfig::new
    );
    public static final StreamCodec<ByteBuf, PlayerConfig> COMPACT_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PlayerConfig decode(final ByteBuf input) {
            if (input.readBoolean()) {
                return STREAM_CODEC.decode(input);
            }
            return createDefault();
        }

        @Override
        public void encode(final ByteBuf output, final PlayerConfig config) {
            if (config.gender.get() == Gender.MALE) {
                output.writeBoolean(false);
            } else {
                output.writeBoolean(true);
                STREAM_CODEC.encode(output, config);
            }
        }
    };

    public static PlayerConfig createDefault() {
        //Note: Theoretically this can never fail so it is safe to use getOrThrow as everything in the codec has orElse(default)
        return CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.emptyMap()).getOrThrow();
    }

    public final ConfigValue<Boolean> showBreastsInArmor;
    public final Sounds sounds;

    private PlayerConfig(Gender gender, Breasts breasts, UVs uvs, Sounds sounds, boolean showBreastsInArmor) {
        this.sounds = sounds;
        this.showBreastsInArmor = SHOW_IN_ARMOR.createValueHandler(showBreastsInArmor);
        super(gender, breasts, uvs);
    }
}

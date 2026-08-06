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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.value.ConfigKey;
import com.wildfire.main.config.value.ConfigValue;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Sounds(ConfigValue<Boolean> hurt, ConfigValue<Float> voicePitch) {

    private static final ConfigKey<Boolean> HURT_SOUNDS = ConfigKey.DEFAULT_TRUE;
    private static final ConfigKey<Float> VOICE_PITCH = ConfigKey.create(1F, 0.8F, 1.2F);

    public static final Codec<Sounds> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        HURT_SOUNDS.codec().fieldOf("override_hurt").forGetter(sounds -> sounds.hurt.get()),
        VOICE_PITCH.codec().fieldOf("voice_pitch").forGetter(sounds -> sounds.voicePitch.get())
    ).apply(instance, Sounds::new));
    public static final MapCodec<Sounds> LEGACY_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        HURT_SOUNDS.codecOrDefault("hurt_sounds").forGetter(sounds -> sounds.hurt.get()),
        VOICE_PITCH.codecOrDefault("voice_pitch").forGetter(sounds -> sounds.voicePitch.get())
    ).apply(instance, Sounds::new));
    //Note: We allow the legacy codec to handle loading as defaults if not present/malformed. If/when we remove the legacy codec, we will need to adjust
    // the main codec to be lenientOptionalFieldOf, or to have an orElse. We will also be able to move the fieldOf("sound") to the caller
    public static final MapCodec<Sounds> CODEC_OR_LEGACY = WildfireHelper.withAlternative(CODEC.fieldOf("sound"), LEGACY_CODEC);
    public static final StreamCodec<ByteBuf, Sounds> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, sounds -> sounds.hurt.get(),
        //Note: While we could avoid syncing pitch when hurt is false, we don't because if we add overrides in the future, voice would need to be synced anyway
        ByteBufCodecs.FLOAT, sounds -> sounds.voicePitch.get(),
        Sounds::new
    );

    private Sounds(boolean hurt, float voicePitch) {
        this(HURT_SOUNDS.createValueHandler(hurt), VOICE_PITCH.createValueHandler(voicePitch));
    }
}

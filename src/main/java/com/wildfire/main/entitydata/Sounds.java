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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.main.config.value.ConfigKey;
import com.wildfire.main.config.value.ConfigValue;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Sounds(ConfigValue<Boolean> hurt, ConfigValue<Float> voicePitch) {

    private static final ConfigKey<Boolean> HURT_SOUNDS = ConfigKey.create("hurt_sounds", true);
    private static final ConfigKey<Float> VOICE_PITCH = ConfigKey.create("voice_pitch", 1F, 0.8f, 1.2f);

    public static final MapCodec<Sounds> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        HURT_SOUNDS.codecOrDefault().forGetter(sounds -> sounds.hurt.get()),
        VOICE_PITCH.codecOrDefault().forGetter(sounds -> sounds.voicePitch.get())
    ).apply(instance, Sounds::new));
    public static final StreamCodec<ByteBuf, Sounds> STREAM_CODEC = StreamCodec.composite(
        //TODO: If physics aren't enabled we don't need to sync pitch
        ByteBufCodecs.BOOL, sounds -> sounds.hurt.get(),
        ByteBufCodecs.FLOAT, sounds -> sounds.voicePitch.get(),
        Sounds::new
    );

    private Sounds(boolean hurt, float voicePitch) {
        this(HURT_SOUNDS.createValueHandler(hurt), VOICE_PITCH.createValueHandler(voicePitch));
    }
}

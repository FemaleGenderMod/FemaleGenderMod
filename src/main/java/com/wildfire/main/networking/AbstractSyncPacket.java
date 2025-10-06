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

package com.wildfire.main.networking;

import com.mojang.datafixers.util.Function11;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;

import java.util.UUID;

abstract class AbstractSyncPacket {

    protected static <T extends AbstractSyncPacket> PacketCodec<ByteBuf, T> codec(SyncPacketConstructor<T> constructor) {
        return PacketCodec.tuple(
                Uuids.PACKET_CODEC, p -> p.uuid,
                Gender.CODEC, p -> p.gender,
                PacketCodecs.FLOAT, p -> p.bustSize,
                PacketCodecs.BOOLEAN, p -> p.hurtSounds,
                PacketCodecs.FLOAT, p -> p.voicePitch,
                BreastPhysics.CODEC, p -> p.physics,
                Breasts.CODEC, p -> p.breasts,
                UV_CODEC, p -> p.leftBreastUVLayout,
                UV_CODEC, p -> p.rightBreastUVLayout,
                UV_CODEC, p -> p.leftBreastOverlayUVLayout,
                UV_CODEC, p -> p.rightBreastOverlayUVLayout,
                constructor
        );
    }

    protected final UUID uuid;
    protected final Gender gender;
    protected final float bustSize;
    protected final boolean hurtSounds;
    protected final float voicePitch;
    protected final BreastPhysics physics;
    protected final Breasts breasts;
    protected final UVLayout leftBreastUVLayout;
    protected final UVLayout rightBreastUVLayout;
    protected final UVLayout leftBreastOverlayUVLayout;
    protected final UVLayout rightBreastOverlayUVLayout;

    protected AbstractSyncPacket(UUID uuid, Gender gender, float bustSize, boolean hurtSounds, float voicePitch, BreastPhysics physics, Breasts breasts, UVLayout leftBreastUVLayout, UVLayout rightBreastUVLayout, UVLayout leftBreastOverlayUVLayout, UVLayout rightBreastOverlayUVLayout) {
        this.uuid = uuid;
        this.gender = gender;
        this.bustSize = bustSize;
        this.hurtSounds = hurtSounds;
        this.voicePitch = voicePitch;
        this.physics = physics;
        this.breasts = breasts;
        this.leftBreastUVLayout = leftBreastUVLayout;
        this.rightBreastUVLayout = rightBreastUVLayout;
        this.leftBreastOverlayUVLayout = leftBreastOverlayUVLayout;
        this.rightBreastOverlayUVLayout = rightBreastOverlayUVLayout;
    }

    protected AbstractSyncPacket(PlayerConfig plr) {
        this(plr.uuid, plr.getGender(), plr.getBustSize(), plr.hasHurtSounds(), plr.getVoicePitch(), new BreastPhysics(plr), plr.getBreasts(), plr.getLeftBreastUVLayout(), plr.getRightBreastUVLayout(), plr.getLeftBreastOverlayUVLayout(), plr.getRightBreastOverlayUVLayout());
    }

    // TODO add support for mannequins?
    protected void updatePlayerFromPacket(PlayerConfig plr) {
        plr.updateGender(gender);
        plr.updateBustSize(bustSize);
        plr.updateHurtSounds(hurtSounds);
        plr.updateVoicePitch(voicePitch);
        physics.applyTo(plr);
        plr.getBreasts().copyFrom(breasts);
        plr.updateLeftBreastUVLayout(leftBreastUVLayout);
        plr.updateRightBreastUVLayout(rightBreastUVLayout);
        plr.updateLeftBreastOverlayUVLayout(leftBreastOverlayUVLayout);
        plr.updateRightBreastOverlayUVLayout(rightBreastOverlayUVLayout);
    }

    protected record BreastPhysics(boolean physics, boolean showInArmor, float bounceMultiplier, float floppyMultiplier) {

        public static final PacketCodec<ByteBuf, BreastPhysics> CODEC = PacketCodec.tuple(
                PacketCodecs.BOOLEAN, BreastPhysics::physics,
                PacketCodecs.BOOLEAN, BreastPhysics::showInArmor,
                PacketCodecs.FLOAT, BreastPhysics::bounceMultiplier,
                PacketCodecs.FLOAT, BreastPhysics::floppyMultiplier,
                BreastPhysics::new
        );

        private BreastPhysics(PlayerConfig plr) {
            this(plr.hasBreastPhysics(), plr.showBreastsInArmor(), plr.getBounceMultiplier(), plr.getFloppiness());
        }

        private void applyTo(PlayerConfig plr) {
            plr.updateBreastPhysics(physics);
            plr.updateShowBreastsInArmor(showInArmor);
            plr.updateBounceMultiplier(bounceMultiplier);
            plr.updateFloppiness(floppyMultiplier);
        }
    }

    @FunctionalInterface
    protected interface SyncPacketConstructor<T extends AbstractSyncPacket> extends Function11<UUID, Gender, Float, Boolean, Float, BreastPhysics, Breasts, UVLayout, UVLayout, UVLayout, UVLayout, T> {
    }


    public static final PacketCodec<ByteBuf, UVLayout> UV_CODEC = new PacketCodec<>() {

        @Override
        public void encode(ByteBuf buf, UVLayout value) {
            for (int i = 0; i < WildfireHelper.SERIALIZED_DIRECTIONS.length; i++) {
                UVQuad quad = value.get(WildfireHelper.SERIALIZED_DIRECTIONS[i]);
                PacketCodecs.VAR_INT.encode(buf, quad.x1());
                PacketCodecs.VAR_INT.encode(buf, quad.y1());
                PacketCodecs.VAR_INT.encode(buf, quad.x2());
                PacketCodecs.VAR_INT.encode(buf, quad.y2());
            }
        }

        @Override
        public UVLayout decode(ByteBuf buf) {
            UVLayout layout = new UVLayout();

            for (int i = 0; i < WildfireHelper.SERIALIZED_DIRECTIONS.length; i++) {
                layout.put(WildfireHelper.SERIALIZED_DIRECTIONS[i], new UVQuad(
                        PacketCodecs.VAR_INT.decode(buf),
                        PacketCodecs.VAR_INT.decode(buf),
                        PacketCodecs.VAR_INT.decode(buf),
                        PacketCodecs.VAR_INT.decode(buf))
                );
            }

            return layout;
        }
    };
}

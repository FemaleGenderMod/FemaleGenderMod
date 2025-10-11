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

import com.mojang.datafixers.util.Function8;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.entitydata.Breasts;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;

import java.util.EnumMap;
import java.util.UUID;

abstract class AbstractSyncPacket {

    // remember to update SyncHelloPacket.VERSION when modifying this codec if the changes result in a change
    // to the underlying packet structure
    protected static <T extends AbstractSyncPacket> PacketCodec<ByteBuf, T> codec(SyncPacketConstructor<T> constructor) {
        return PacketCodec.tuple(
                Uuids.PACKET_CODEC, p -> p.uuid,
                Gender.CODEC, p -> p.gender,
                PacketCodecs.FLOAT, p -> p.bustSize,
                PacketCodecs.BOOLEAN, p -> p.hurtSounds,
                PacketCodecs.FLOAT, p -> p.voicePitch,
                BreastPhysics.CODEC, p -> p.physics,
                Breasts.CODEC, p -> p.breasts,
                UV_LAYOUTS_CODEC, p -> p.uvLayouts,
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
    protected final UVLayouts uvLayouts;

    protected AbstractSyncPacket(UUID uuid, Gender gender, float bustSize, boolean hurtSounds, float voicePitch, BreastPhysics physics, Breasts breasts, UVLayouts uvLayouts) {
        this.uuid = uuid;
        this.gender = gender;
        this.bustSize = bustSize;
        this.hurtSounds = hurtSounds;
        this.voicePitch = voicePitch;
        this.physics = physics;
        this.breasts = breasts;
        this.uvLayouts = uvLayouts;
    }

    protected AbstractSyncPacket(PlayerConfig plr) {
        this(plr.uuid, plr.getGender(), plr.getBustSize(), plr.hasHurtSounds(), plr.getVoicePitch(), new BreastPhysics(plr), plr.getBreasts(), UVLayouts.from(plr));
    }

    // TODO add support for mannequins?
    protected void updatePlayerFromPacket(PlayerConfig plr) {
        plr.updateGender(gender);
        plr.updateBustSize(bustSize);
        plr.updateHurtSounds(hurtSounds);
        plr.updateVoicePitch(voicePitch);
        physics.applyTo(plr);
        plr.getBreasts().copyFrom(breasts);
        uvLayouts.applyTo(plr);
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
    protected interface SyncPacketConstructor<T extends AbstractSyncPacket> extends Function8<UUID, Gender, Float, Boolean, Float, BreastPhysics, Breasts, UVLayouts, T> {
    }

    public record UVLayouts(Layer skin, Layer overlay) {
        public static UVLayouts from(PlayerConfig plr) {
            return new UVLayouts(
                    /*skin = */ new Layer(plr.getLeftBreastUVLayout().copy(), plr.getRightBreastUVLayout().copy()),
                    /*overlay = */ new Layer(plr.getLeftBreastOverlayUVLayout().copy(), plr.getRightBreastOverlayUVLayout().copy())
            );
        }

        private void applyTo(PlayerConfig plr) {
            plr.updateLeftBreastUVLayout(skin.left);
            plr.updateRightBreastUVLayout(skin.right);
            plr.updateLeftBreastOverlayUVLayout(overlay.left);
            plr.updateRightBreastOverlayUVLayout(overlay.right);
        }

        public record Layer(UVLayout left, UVLayout right) {
        }
    }

    static final PacketCodec<ByteBuf, UVLayout> UV_CODEC = PacketCodecs.map(
            size -> new EnumMap<>(UVDirection.class),
            UVDirection.PACKET_CODEC,
            UVQuad.PACKET_CODEC,
            UVDirection.values().length
    ).xmap(UVLayout::new, UVLayout::getQuads);

    static final PacketCodec<ByteBuf, UVLayouts.Layer> UV_LAYER_CODEC = PacketCodec.tuple(
            UV_CODEC, UVLayouts.Layer::left,
            UV_CODEC, UVLayouts.Layer::right,
            UVLayouts.Layer::new
    );

    static final PacketCodec<ByteBuf, UVLayouts> UV_LAYOUTS_CODEC = PacketCodec.tuple(
            UV_LAYER_CODEC, UVLayouts::skin,
            UV_LAYER_CODEC, UVLayouts::overlay,
            UVLayouts::new
    );
}

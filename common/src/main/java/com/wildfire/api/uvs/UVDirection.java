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

package com.wildfire.api.uvs;

import com.mojang.serialization.Codec;
import com.wildfire.api.WildfireAPI;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.CommonColors;
import net.minecraft.util.StringRepresentable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import java.util.function.IntFunction;

public enum UVDirection implements StringRepresentable {
    EAST("east", CommonColors.RED, new Vec3i(1, 0, 0)),
    WEST("west", CommonColors.GREEN, new Vec3i(-1, 0, 0)),
    DOWN("down", CommonColors.BLUE, new Vec3i(0, -1, 0)),
    UP("up", 0xFF00FFFF, new Vec3i(0, 1, 0)),
    NORTH("north", 0xFFFF00FF, new Vec3i(0, 0, -1));

    private final Component shortName;
    private final String saveName;
    private final int baseColor;
    private final Vector3fc floatVector;

    public static final IntFunction<UVDirection> BY_ID = ByIdMap.continuous(UVDirection::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final Codec<UVDirection> CODEC = StringRepresentable.fromEnum(UVDirection::values);
    public static final StreamCodec<ByteBuf, UVDirection> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, UVDirection::ordinal);

    UVDirection(String saveName, int baseColor, Vec3i vector) {
        this.saveName = saveName;
        this.baseColor = baseColor;
        this.shortName = Component.translatable(getShortTranslationKey());
        this.floatVector = new Vector3f(vector.getX(), vector.getY(), vector.getZ());
    }

    public int getFaceColor(boolean faded) {
        if (!faded) return baseColor;

        return ARGB.color(0x33, baseColor);
    }

    public Vector3f getUnitVector() {
        return new Vector3f(this.floatVector);
    }

    public FaceDirection getDirection(BreastTypes type) {
        return switch (this) {
            case EAST, WEST -> type.isLeft() ? FaceDirection.INNER : FaceDirection.OUTER;
            case DOWN -> FaceDirection.BOTTOM;
            case UP -> FaceDirection.TOP;
            case NORTH -> FaceDirection.FRONT;
        };
    }

    public MutableComponent getDirectionText(BreastTypes type) {
        return Component.translatable(getDirection(type).getTranslationKey());
    }

    public String getShortTranslationKey() {
        return WildfireAPI.MODID + ".uv.direction." + saveName + ".short";
    }

    public Component getShortName() {
        return shortName;
    }

    @Override
    public String getSerializedName() {
        return saveName;
    }
}

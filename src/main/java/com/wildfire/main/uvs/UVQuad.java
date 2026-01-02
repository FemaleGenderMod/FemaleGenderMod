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

package com.wildfire.main.uvs;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record UVQuad(int x1, int y1, int x2, int y2) {
	public static final StreamCodec<ByteBuf, UVQuad> PACKET_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, UVQuad::x1,
			ByteBufCodecs.VAR_INT, UVQuad::y1,
			ByteBufCodecs.VAR_INT, UVQuad::x2,
			ByteBufCodecs.VAR_INT, UVQuad::y2,
			UVQuad::new
	);

	public UVQuad addX1(int x1) {
		return new UVQuad(this.x1 + x1, y1, x2, y2);
	}

	public UVQuad addY1(int y1) {
		return new UVQuad(x1, this.y1 + y1, x2, y2);
	}

	public UVQuad addX2(int x2) {
		return new UVQuad(x1, y1, this.x2 + x2, y2);
	}

	public UVQuad addY2(int y2) {
		return new UVQuad(x1, y1, x2, this.y2 + y2);
	}
}

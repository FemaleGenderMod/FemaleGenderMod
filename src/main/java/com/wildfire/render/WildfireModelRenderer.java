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

package com.wildfire.render;

import com.google.common.base.Preconditions;
import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3fc;

@Environment(EnvType.CLIENT)
public final class WildfireModelRenderer {
	private WildfireModelRenderer() {
		throw new UnsupportedOperationException();
	}

	public static class ModelBox {
		public final WildfireModelRenderer.TexturedQuad[] quads;
		public final float posX1;
		public final float posY1;
		public final float posZ1;
		public final float posX2;
		public final float posY2;
		public final float posZ2;

		protected final UVLayout dynamicUvLayouts;

		protected ModelBox(int tW, int tH, float x, float y, float z, int dx, int dy, int dz, float delta, int quads, UVLayout dynamicUvLayouts) {
			this.posX1 = x;
			this.posY1 = y;
			this.posZ1 = z;
			this.posX2 = x + (float) dx;
			this.posY2 = y + (float) dy;
			this.posZ2 = z + (float) dz;
			this.quads = new TexturedQuad[quads];
			this.dynamicUvLayouts = dynamicUvLayouts;

			float f = x + (float) dx;
			float f1 = y + (float) dy;
			float f2 = z + (float) dz;
			x = x - delta;
			y = y - delta;
			z = z - delta;
			f = f + delta;
			f1 = f1 + delta;
			f2 = f2 + delta;

			initQuads(tW, tH, dx, dy, dz, quads,
					new PositionTextureVertex(f, y, z, 0.0F, 8.0F),
					new PositionTextureVertex(f, f1, z, 8.0F, 8.0F),
					new PositionTextureVertex(x, f1, z, 8.0F, 0.0F),
					new PositionTextureVertex(x, y, f2, 0.0F, 0.0F),
					new PositionTextureVertex(f, y, f2, 0.0F, 8.0F),
					new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F),
					new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F),
					new PositionTextureVertex(x, y, z, 0.0F, 0.0F)
			);
		}

		protected void initQuads(int tW, int tH, int dx, int dy, int dz, int quads,
								 PositionTextureVertex vertex, PositionTextureVertex vertex1, PositionTextureVertex vertex2,
								 PositionTextureVertex vertex3, PositionTextureVertex vertex4, PositionTextureVertex vertex5,
								 PositionTextureVertex vertex6, PositionTextureVertex vertex7) {
			PositionTextureVertex[][] faceVertices = {
					{vertex4, vertex, vertex1, vertex5},	// EAST
					{vertex7, vertex3, vertex6, vertex2},	// WEST
					{vertex4, vertex3, vertex7, vertex},	// DOWN
					{vertex1, vertex2, vertex6, vertex5},	// UP
					{vertex, vertex7, vertex2, vertex1},	// NORTH
					{vertex3, vertex4, vertex5, vertex6}	// SOUTH
			};

			int i = 0;
			for(var entry : dynamicUvLayouts.getAllSides().entrySet()) {
				UVDirection direction = entry.getKey();
				UVQuad quad = entry.getValue();
				if(quad == null) continue;

				this.quads[i] = new TexturedQuad(
						quad.x1(), quad.y1(), quad.x2(), quad.y2(),
						tW, tH,
						direction,
						faceVertices[i][0],
						faceVertices[i][1],
						faceVertices[i][2],
						faceVertices[i][3]
				);
				i++;
			}
		}
	}

	public static class OverlayModelBox extends ModelBox {
		public OverlayModelBox(int tW, int tH, float x, float y, float z, int dx, int dy, int dz, float delta, UVLayout dynamicUvLayouts) {
			super(tW, tH, x, y, z, dx, dy, dz, delta, 5, dynamicUvLayouts);
		}
	}

	public static class BreastModelBox extends ModelBox {
		public BreastModelBox(int tW, int tH, float x, float y, float z, int dx, int dy, int dz, float delta, UVLayout dynamicUvLayouts) {
			super(tW, tH, x, y, z, dx, dy, dz, delta, 5, dynamicUvLayouts);
		}
	}

	public record PositionTextureVertex(float x, float y, float z, float u, float v) {
		public PositionTextureVertex withTexturePosition(float texU, float texV) {
			return new PositionTextureVertex(x, y, z, texU, texV);
		}
	}

	public static class TexturedQuad {
		public final WildfireModelRenderer.PositionTextureVertex[] vertexPositions;
		public final Vector3fc normal;
		public final float[] uvs;

		public TexturedQuad(float u1, float v1, float u2, float v2, float texWidth, float texHeight, UVDirection directionIn, PositionTextureVertex... positionsIn) {
			Preconditions.checkArgument(positionsIn.length == 4, "Incorrect number of vertices; expected 4, got %s", positionsIn.length);

			//Set UVs in array to reference in render side.
			this.uvs = new float[]{ u1, v1, u2, v2 };

			this.vertexPositions = positionsIn;
			float f = 0.0F / texWidth;
			float f1 = 0.0F / texHeight;
			positionsIn[0] = positionsIn[0].withTexturePosition(u2 / texWidth - f, v1 / texHeight + f1);
			positionsIn[1] = positionsIn[1].withTexturePosition(u1 / texWidth + f, v1 / texHeight + f1);
			positionsIn[2] = positionsIn[2].withTexturePosition(u1 / texWidth + f, v2 / texHeight - f1);
			positionsIn[3] = positionsIn[3].withTexturePosition(u2 / texWidth - f, v2 / texHeight - f1);
			this.normal = directionIn.getUnitVector();
		}
	}
}
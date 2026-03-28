package com.wildfire.render;

import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public final class WildfireModelRenderer {

    private WildfireModelRenderer() {
        throw new UnsupportedOperationException();
    }

    public static class ModelBox {
        public final TexturedQuad[] quads;
        public final float posX1, posY1, posZ1;
        public final float posX2, posY2, posZ2;

        public ModelBox(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
            this(tW, tH, texU, texV, x, y, z, dx, dy, dz, delta, mirror, 5);
        }

        protected ModelBox(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror, int quads) {
            this(tW, tH, texU, texV, x, y, z, dx, dy, dz, delta, mirror, quads, false);
        }

        protected ModelBox(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror, int quads, boolean extra) {
            this.posX1 = x;
            this.posY1 = y;
            this.posZ1 = z;
            this.posX2 = x + (float)dx;
            this.posY2 = y + (float)dy;
            this.posZ2 = z + (float)dz;
            this.quads = new TexturedQuad[quads];

            float f = x + (float)dx;
            float f1 = y + (float)dy;
            float f2 = z + (float)dz;

            x -= delta; y -= delta; z -= delta;
            f += delta; f1 += delta; f2 += delta;

            if (mirror) {
                float temp = f;
                f = x;
                x = temp;
            }

            // Definición de vértices del cubo
            PositionTextureVertex v0 = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
            PositionTextureVertex v1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
            PositionTextureVertex v2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
            PositionTextureVertex v3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
            PositionTextureVertex v4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
            PositionTextureVertex v5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
            PositionTextureVertex v6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
            PositionTextureVertex v7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);

            this.initQuads(tW, tH, texU, texV, dx, dy, dz, mirror, extra, v0, v1, v2, v3, v4, v5, v6, v7);
        }

        protected void initQuads(int tW, int tH, int texU, int texV, int dx, int dy, int dz, boolean mirror, boolean extra, PositionTextureVertex v0, PositionTextureVertex v1, PositionTextureVertex v2, PositionTextureVertex v3, PositionTextureVertex v4, PositionTextureVertex v5, PositionTextureVertex v6, PositionTextureVertex v7) {
            this.quads[0] = new TexturedQuad((float)(texU + dz + dx), (float)(texV + dz), (float)(texU + dz + dx + dz), (float)(texV + dz + dy), (float)tW, (float)tH, mirror, Direction.SOUTH, new PositionTextureVertex[]{v4, v0, v1, v5});
            this.quads[1] = new TexturedQuad((float)texU, (float)(texV + dz), (float)(texU + dz), (float)(texV + dz + dy), (float)tW, (float)tH, mirror, Direction.NORTH, new PositionTextureVertex[]{v7, v3, v6, v2});
            this.quads[2] = new TexturedQuad((float)(texU + dz), (float)texV, (float)(texU + dz + dx), (float)(texV + dz), (float)tW, (float)tH, mirror, Direction.UP, new PositionTextureVertex[]{v4, v3, v7, v0});
            this.quads[3] = new TexturedQuad((float)(texU + dz), (float)(texV + dz + 4), (float)(texU + dz + dx), (float)(texV + 1 + dz + dy), (float)tW, (float)(tH - 1), mirror, Direction.DOWN, new PositionTextureVertex[]{v1, v2, v6, v5});
            this.quads[4] = new TexturedQuad((float)(texU + dz), (float)(texV + dz), (float)(texU + dz + dx), (float)(texV + dz + dy), (float)tW, (float)tH, mirror, Direction.WEST, new PositionTextureVertex[]{v0, v7, v2, v1});
        }
    }

    public static class OverlayModelBox extends ModelBox {
        public OverlayModelBox(boolean isLeft, int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
            super(tW, tH, texU, texV, x, y, z, dx, dy, dz, delta, mirror, 4, isLeft);
        }

        @Override
        protected void initQuads(int tW, int tH, int texU, int texV, int dx, int dy, int dz, boolean mirror, boolean isLeft, PositionTextureVertex v0, PositionTextureVertex v1, PositionTextureVertex v2, PositionTextureVertex v3, PositionTextureVertex v4, PositionTextureVertex v5, PositionTextureVertex v6, PositionTextureVertex v7) {
            if (!isLeft) {
                this.quads[0] = new TexturedQuad((float)(texU + dz + dx), (float)(texV + dz), (float)(texU + dz + dx + dz), (float)(texV + dz + dy), (float)tW, (float)tH, mirror, Direction.SOUTH, new PositionTextureVertex[]{v4, v0, v1, v5});
            } else {
                this.quads[0] = new TexturedQuad((float)texU, (float)(texV + dz), (float)(texU + dz), (float)(texV + dz + dy), (float)tW, (float)tH, mirror, Direction.NORTH, new PositionTextureVertex[]{v7, v3, v6, v2});
            }
            this.quads[1] = new TexturedQuad((float)(texU + dz), (float)texV, (float)(texU + dz + dx), (float)(texV + dz), (float)tW, (float)tH, mirror, Direction.UP, new PositionTextureVertex[]{v4, v3, v7, v0});
            this.quads[2] = new TexturedQuad((float)(texU + dz), (float)(texV + dz + 4), (float)(texU + dz + dx), (float)(texV + 1 + dz + dy), (float)tW, (float)(tH - 1), mirror, Direction.DOWN, new PositionTextureVertex[]{v1, v2, v6, v5});
            this.quads[3] = new TexturedQuad((float)(texU + dz), (float)(texV + dz), (float)(texU + dz + dx), (float)(texV + dz + dy), (float)tW, (float)tH, mirror, Direction.WEST, new PositionTextureVertex[]{v0, v7, v2, v1});
        }
    }

    public static class BreastModelBox extends ModelBox {
        public BreastModelBox(int tW, int tH, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
            super(tW, tH, texU, texV, x, y, z, dx, dy, dz, delta, mirror);
        }

        @Override
        protected void initQuads(int tW, int tH, int texU, int texV, int dx, int dy, int dz, boolean mirror, boolean extra, PositionTextureVertex v0, PositionTextureVertex v1, PositionTextureVertex v2, PositionTextureVertex v3, PositionTextureVertex v4, PositionTextureVertex v5, PositionTextureVertex v6, PositionTextureVertex v7) {
            this.quads[0] = new TexturedQuad((float)(texU + 4 + dx), (float)(texV + 4), (float)(texU + 4 + dx + 4), (float)(texV + 4 + dy), (float)tW, (float)tH, mirror, Direction.SOUTH, new PositionTextureVertex[]{v4, v0, v1, v5});
            this.quads[1] = new TexturedQuad((float)texU, (float)(texV + 4), (float)(texU + 4), (float)(texV + 4 + dy), (float)tW, (float)tH, mirror, Direction.NORTH, new PositionTextureVertex[]{v7, v3, v6, v2});
            this.quads[2] = new TexturedQuad((float)(texU + 4), (float)texV, (float)(texU + 4 + dx), (float)(texV + 4), (float)tW, (float)tH, mirror, Direction.UP, new PositionTextureVertex[]{v4, v3, v7, v0});
            this.quads[3] = new TexturedQuad((float)(texU + 4), (float)(texV + 4 + 4), (float)(texU + 4 + dx), (float)(texV + 1 + 4 + dy), (float)tW, (float)(tH - 1), mirror, Direction.DOWN, new PositionTextureVertex[]{v1, v2, v6, v5});
            this.quads[4] = new TexturedQuad((float)(texU + 4), (float)(texV + 4), (float)(texU + 4 + dx), (float)(texV + 4 + dy), (float)tW, (float)tH, mirror, Direction.WEST, new PositionTextureVertex[]{v0, v7, v2, v1});
        }
    }

    public static record PositionTextureVertex(float x, float y, float z, float u, float v) {
        public PositionTextureVertex withTexturePosition(float texU, float texV) {
            return new PositionTextureVertex(this.x, this.y, this.z, texU, texV);
        }
    }

    public static class TexturedQuad {
        public final PositionTextureVertex[] vertexPositions;
        public final Vector3f normal;

        public TexturedQuad(float u1, float v1, float u2, float v2, float texWidth, float texHeight, boolean mirrorIn, Direction directionIn, PositionTextureVertex... positionsIn) {
            if (positionsIn.length != 4) {
                throw new IllegalArgumentException("Wrong number of vertices. Expected: 4, Received: " + positionsIn.length);
            } else {
                this.vertexPositions = positionsIn;

                positionsIn[0] = positionsIn[0].withTexturePosition(u2 / texWidth, v1 / texHeight);
                positionsIn[1] = positionsIn[1].withTexturePosition(u1 / texWidth, v1 / texHeight);
                positionsIn[2] = positionsIn[2].withTexturePosition(u1 / texWidth, v2 / texHeight);
                positionsIn[3] = positionsIn[3].withTexturePosition(u2 / texWidth, v2 / texHeight);

                if (mirrorIn) {
                    int len = positionsIn.length;
                    for(int j = 0; j < len / 2; ++j) {
                        PositionTextureVertex temp = positionsIn[j];
                        positionsIn[j] = positionsIn[len - 1 - j];
                        positionsIn[len - 1 - j] = temp;
                    }
                }

                // method_23955() en Mojang es step() para obtener el vector normal
                this.normal = directionIn.step();
                if (mirrorIn) {
                    this.normal.mul(-1.0F, 1.0F, 1.0F);
                }
            }
        }
    }
}
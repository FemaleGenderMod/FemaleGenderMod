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

package com.wildfire.gui.screen;

import com.wildfire.gui.GuiUtils;
import com.wildfire.main.WildfireGender;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class WildfireBreastUVEditorScreen extends BaseWildfireScreen {

    private static final Text TITLE = Text.translatable("wildfire_gender.cloud_details.title");

    private static final Identifier BACKGROUND = Identifier.of(WildfireGender.MODID, "textures/gui/breast_uv_editor.png");

    public WildfireBreastUVEditorScreen(Screen parent, UUID uuid) {
        super(Text.translatable("wildfire_gender.uv_editor"), parent, uuid);
    }

    @Override
    public void init() {
        int x = this.width / 2;
        int y = this.height / 2;

        super.init();
    }


    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        //this.renderInGameBackground(ctx);
        //ctx.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND, (this.width - 190) / 2, (this.height - 107) / 2, 0, 0, 190, 107, 512, 512);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (client == null || client.world == null) return;
        super.render(ctx, mouseX, mouseY, delta);

        int x = this.width / 2;
        int y = this.height / 2;

        final int textureDrawWidth = 196;
        final int textureSourceWidth = 64;
        final float scaleFactor = (float) textureDrawWidth / (float) textureSourceWidth;

        int screenXBase = (this.width) / 2 + 120;
        int screenYBase = (this.height - 64) / 2 - 64;

        if(client.player != null) {

            ctx.drawTexture(RenderPipelines.GUI_TEXTURED, client.player.getSkin().body().id(),
                    screenXBase, screenYBase,
                    0, 0, textureDrawWidth, textureDrawWidth, textureDrawWidth, textureDrawWidth);


            final int borderThickness = 1;

            final int[] FACE_COLORS = new int[]{
                    0xFFFF0000,
                    0xFF00FF00,
                    0xFF0000FF,
                    0xFF00FFFF,
                    0xFFFF00FF
            };

            final String[] FACE_NAMES = new String[]{
                    "E",
                    "W",
                    "D",
                    "U",
                    "N"
            };

            int faceIndex = 0;

            for (int[] faceUV : getPlayer().getLeftBreastUVLayout()) {

                final int borderColor = FACE_COLORS[faceIndex];
                final String faceName = FACE_NAMES[faceIndex];

                int u1 = faceUV[0];
                int v1 = faceUV[1];
                int u2 = faceUV[2];
                int v2 = faceUV[3];

                int rectX1 = (int) (screenXBase + (float) (u1) * scaleFactor);
                int rectY1 = (int) (screenYBase + (float) (v1 - 1) * scaleFactor);
                int rectX2 = (int) (screenXBase + (float) (u2) * scaleFactor);
                int rectY2 = (int) (screenYBase + (float) (v2 - 1) * scaleFactor);

                ctx.fill(rectX1, rectY1, rectX2, rectY1 + borderThickness, borderColor);
                ctx.fill(rectX1, rectY2 - borderThickness, rectX2, rectY2, borderColor);
                ctx.fill(rectX1, rectY1, rectX1 + borderThickness, rectY2, borderColor);
                ctx.fill(rectX2 - borderThickness, rectY1, rectX2, rectY2, borderColor);

                int centerX = (rectX1 + rectX2) / 2;
                int centerY = (rectY1 + rectY2) / 2;
                int textWidth = textRenderer.getWidth(faceName);
                int textHeight = textRenderer.fontHeight;

                ctx.getMatrices().pushMatrix();
                ctx.getMatrices().translate(centerX, centerY);
                ctx.getMatrices().scale(0.6f);

                ctx.drawText(textRenderer, faceName, -textWidth / 2, -textHeight / 2, 0xFFFFFFFF, true);

                ctx.getMatrices().popMatrix();

                faceIndex++;
            }

        }

        GuiUtils.drawCenteredText(ctx, textRenderer, TITLE, x, y - 94, 4473924);
    }

    @Override
    public void close() {
        super.close();
    }
}

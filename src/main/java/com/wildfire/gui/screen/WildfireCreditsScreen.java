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

import com.wildfire.events.EntityHurtSoundEvent;
import com.wildfire.gui.GuiUtils;
import com.wildfire.gui.WildfireSlider;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.enums.Gender;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

import java.util.Objects;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class WildfireCreditsScreen extends BaseWildfireScreen {

    private static final Identifier CREDIT_CONTAINER = Identifier.of(WildfireGender.MODID, "textures/gui/credit_container.png");

    private CreditBox[] CREDIT_BOXES = new CreditBox[] {
            new CreditBox("WildfireMC", UUID.fromString("23b6feed-2dfe-4f2e-9429-863fd4adb946")),
            new CreditBox("celeste", UUID.fromString("70336328-0de7-430e-8cba-2779e2a05ab5")),
            new CreditBox("pupnewfster", UUID.fromString("64e57307-72e5-4f43-be9c-181e8e35cc9b")),
            new CreditBox("Kichura", UUID.fromString("618a8390-51b1-43b2-a53a-ab72c1bbd8bd")),
            new CreditBox("DiaDemiEmi", UUID.fromString("ad8ee68c-0aa1-47f9-b29f-f92fa1ef66dc")),
            new CreditBox("ArcticWah", UUID.fromString("8fb5e95d-7f41-4b4c-b8c5-4f15ea3fa2c1")),
            new CreditBox("IzzyBizzy45", UUID.fromString("3f36f7e9-7459-43fe-87ce-4e8a5d47da80")),
            new CreditBox("Powerless001", UUID.fromString("525b0455-15e9-49b7-b61d-f291e8ee6c5b"))
    };
    public WildfireCreditsScreen(Screen parent, UUID uuid) {
        super(Text.translatable("wildfire_gender.credits.title"), parent, uuid);
    }

    @Override
    public void init() {
        int yPos = this.height / 2 - 11;

        /*addButton(builder -> builder
                .message(() -> Text.literal("X"))
                .position(this.width / 2 + 73, yPos - 121)
                .size(9, 9)
                .onPress(button -> close())
                .narration(text -> GuiUtils.doneNarrationText()));*/

        super.init();
    }

    @Override
    public void tick() {
        for(CreditBox creditBox : CREDIT_BOXES) {
            if(creditBox.getEntity() != null) {
                if(!creditBox.getEntity().getUuidAsString().equalsIgnoreCase(client.player.getUuidAsString())) {
                    PlayerConfig aPlr = WildfireGender.getOrAddPlayerById(creditBox.getUUID());
                    aPlr.updateGender(Gender.FEMALE);
                    aPlr.updateBustSize(0.8f);
                    aPlr.getBreasts().updateCleavage(0.05f);
                    aPlr.getBreasts().updateUniboob(false);
                    aPlr.tickBreastPhysics(creditBox.getEntity());
                }
            }
        }
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(ctx);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {

        Matrix3x2fStack mStack = ctx.getMatrices();

        mStack.pushMatrix();
            GuiUtils.drawCenteredText(ctx, textRenderer, Text.translatable("wildfire_gender.credits.title"), width / 2, height / 2 - 85, ColorHelper.fullAlpha(0xFFFFFF));
        mStack.popMatrix();

        int startX = width / 2 - (5 * 60) / 2 + 4;
        int startY = height / 2 - (2 * 74) / 2 + 4;
        int index = 0;
        int y = 0;
        for(CreditBox creditBox : CREDIT_BOXES) {
            if(creditBox.getEntity() != null) {
                int creditBoxX = startX + (index * 60);
                int creditBoxY = startY + (y * 74);
                ctx.drawTexture(RenderPipelines.GUI_TEXTURED, CREDIT_CONTAINER, creditBoxX, creditBoxY, 0, 0, 52, 68, 52, 68);


                int xP = creditBoxX + (52 / 2);
                int yP = creditBoxY + (68 / 2);
                ctx.enableScissor(xP - 21, yP - 79, xP + 21, yP + 20);
                GuiUtils.drawEntityOnScreenNoScissor(ctx, xP - 38, yP - 29, xP + 38, yP + 59, 40, mouseX, mouseY + 35, creditBox.getEntity());
                ctx.disableScissor();

                mStack.pushMatrix();
                mStack.translate(xP, yP + 47);
                mStack.scale(new Vector2f(0.55f, 0.55f));
                mStack.translate(-xP, (-yP) - 47);
                GuiUtils.drawCenteredTextWrapped(ctx, textRenderer, Text.literal(creditBox.getName()), xP, yP + 7, (int) (50 * 1.45f), ColorHelper.fullAlpha(0xFFFFFF));
                mStack.popMatrix();

                index++;
                if(index % 5 == 0) {
                    y++;
                    index = 0;
                }
            }
        }
    }
}

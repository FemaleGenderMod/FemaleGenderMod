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

import com.wildfire.gui.FakeGUIPlayer;
import com.wildfire.gui.GuiUtils;
import com.wildfire.main.GenderConfigs;
import com.wildfire.main.WildfireGender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class WildfireCreditsScreen extends BaseWildfireScreen {

    private static final Identifier CREDIT_CONTAINER = Identifier.of(WildfireGender.MODID, "textures/gui/credit_container.png");

    private final FakeGUIPlayer[] CREDIT_BOXES = new FakeGUIPlayer[] {
            new FakeGUIPlayer(
                    "WildfireMC",
                    UUID.fromString("23b6feed-2dfe-4f2e-9429-863fd4adb946"),
                    "Mod Creator",
                    "",
                    GenderConfigs.DEFAULT_FEMALE
            ),
            new FakeGUIPlayer(
                    "celeste",
                    UUID.fromString("70336328-0de7-430e-8cba-2779e2a05ab5"),
                    "Fabric Maintainer",
                    "",
                    GenderConfigs.DEFAULT_FEMALE
            ),
            new FakeGUIPlayer(
                    "pupnewfster",
                    UUID.fromString("64e57307-72e5-4f43-be9c-181e8e35cc9b"),
                    "NeoForge Maintainer",
                    "",
                    GenderConfigs.DEFAULT_FEMALE
            ),
            new FakeGUIPlayer(
                    "Kichura",
                    UUID.fromString("618a8390-51b1-43b2-a53a-ab72c1bbd8bd"),
                    "Programmer",
                    "",
                    GenderConfigs.DEFAULT_FEMALE
            ),
            new FakeGUIPlayer(
                    "DiaDemiEmi",
                    UUID.fromString("ad8ee68c-0aa1-47f9-b29f-f92fa1ef66dc"),
                    "Programmer",
                    "",
                    GenderConfigs.DEFAULT_FEMALE
            ),
            new FakeGUIPlayer(
                    "ArcticWah",
                    UUID.fromString("8fb5e95d-7f41-4b4c-b8c5-4f15ea3fa2c1"),
                    "Mod Translator",
                    "",
                    GenderConfigs.DEFAULT_FEMALE
            ),
            new FakeGUIPlayer(
                    "Bluelight",
                    UUID.fromString("33feda66-c706-4725-8983-f62e5e6cbee7"),
                    "Mod Translator",
                    "",
                    GenderConfigs.DEFAULT_FEMALE
            ),
            new FakeGUIPlayer(
                    "IzzyBizzy45",
                    UUID.fromString("3f36f7e9-7459-43fe-87ce-4e8a5d47da80"),
                    "Programmer",
                    "",
                    GenderConfigs.DEFAULT_FEMALE
            ),
            new FakeGUIPlayer(
                    "Powerless001",
                    UUID.fromString("525b0455-15e9-49b7-b61d-f291e8ee6c5b"),
                    "Contributor",
                    "",
                    GenderConfigs.DEFAULT_FEMALE
            )
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
        for(FakeGUIPlayer player : CREDIT_BOXES) {
            player.tick();
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

        int columns = 5;
        int boxW = 60;
        int boxH = 74;

        int startY = height / 2 - (2 * boxH) / 2 + 4;

        int index = 0;
        int row = 0;

        for(FakeGUIPlayer creditBox : CREDIT_BOXES) {
            if(creditBox.getEntity() != null) {

                int col = index % columns;

                int remaining = CREDIT_BOXES.length - (row * columns);
                int boxesInRow = Math.min(columns, remaining);

                int rowWidth = boxesInRow * boxW;
                int startX = (width / 2) - (rowWidth / 2) + 4;

                int creditBoxX = startX + (col * boxW);
                int creditBoxY = startY + (row * boxH);

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

                if(mouseX > xP - 24 && mouseX < xP + 23 && mouseY > yP + 22 && mouseY < yP + 31) {
                    //Tooltip on hover name
                    List<Text> txtList = new ArrayList<>();
                    txtList.add(Text.empty().append(GuiUtils.FEMALE_GENDER_MOD_LOGO_TEXT).append(Text.literal(" Contributor").formatted(Formatting.RESET)));
                    txtList.add(Text.literal("Name: ").formatted(Formatting.WHITE).append(Text.literal(creditBox.getName()).formatted(Formatting.GRAY)));
                    Formatting roleColor = switch(creditBox.getDevTitle()) {
                        case "Mod Creator" -> Formatting.GOLD;
                        default -> Formatting.GRAY;
                    };
                    txtList.add(Text.literal("Role: ").formatted(Formatting.WHITE).append(Text.literal(creditBox.getDevTitle()).formatted(roleColor)));
                    if(creditBox.getDescription() != null && !creditBox.getDescription().isEmpty())
                        txtList.add(Text.literal(creditBox.getDescription()).formatted(Formatting.GRAY));

                    ctx.drawTooltip(textRenderer, txtList, mouseX, mouseY);
                }

                index++;
                if (index % columns == 0) {
                    row++;
                }
            }
        }
    }
}

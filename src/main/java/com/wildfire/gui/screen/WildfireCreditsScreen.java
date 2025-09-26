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
import com.wildfire.main.contributors.Contributors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class WildfireCreditsScreen extends BaseWildfireScreen {

    private static final Identifier CREDIT_CONTAINER = Identifier.of(WildfireGender.MODID, "textures/gui/credits/credit_container.png");
    private static final Identifier BUTTON_CONTAINER = Identifier.of(WildfireGender.MODID, "textures/gui/credits/button_container.png");

    private final FakeGUIPlayer[] CREDIT_BOXES = Contributors.getContributors().entrySet().stream()
            .filter(it -> it.getValue().name() != null)
            .filter(it -> Boolean.TRUE.equals(it.getValue().showInCredits()))
            .sorted(Comparator.comparing(it -> it.getValue().name()))
            .sorted(Comparator.comparing(it -> it.getValue().getRole()))
            .map(it -> new FakeGUIPlayer(it.getValue().name(), it.getKey(), GenderConfigs.DEFAULT_FEMALE))
            .toArray(FakeGUIPlayer[]::new);

    private final int boxesPerPage = 12;
    private final int totalPages = (int) Math.ceil((double) CREDIT_BOXES.length / boxesPerPage);

    private int creditsPage = 0;

    public WildfireCreditsScreen(Screen parent, UUID uuid) {
        super(Text.translatable("wildfire_gender.credits.title"), parent, uuid);
    }

    private int navigationY;
    @Override
    public void init() {

        final var ref = new Object() {
            ClickableWidget prevPage, nextPage;
        };

        navigationY = this.height / 2 + 82;
        addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.details.go_back"))
                .position(this.width / 2 - 25, navigationY + 6)
                .size(50, 13)
                .onPress(button -> close())
                .narration(text -> GuiUtils.doneNarrationText()));

        ref.nextPage = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.details.next_page"))
                .position(this.width / 2 + 29, navigationY + 6)
                .size(60, 13)
                .active(creditsPage < totalPages-1)
                .onPress(button -> {
                    if(creditsPage < totalPages-1) {
                        creditsPage++;
                    }
                    ref.prevPage.active = creditsPage != 0;
                    ref.nextPage.active = creditsPage < totalPages-1;
                })
                .narration(text -> GuiUtils.doneNarrationText()));

        ref.prevPage = addButton(builder -> builder
                .message(() -> Text.translatable("wildfire_gender.details.prev_page"))
                .position(this.width / 2 - 89, navigationY + 6)
                .size(60, 13)
                .active(creditsPage != 0)
                .onPress(button -> {
                    if(creditsPage > 0) {
                        creditsPage--;
                    }
                    ref.prevPage.active = creditsPage != 0;
                    ref.nextPage.active = creditsPage < totalPages;
                })
                .narration(text -> GuiUtils.doneNarrationText()));

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

        // FIXME any additional contributors will result in the rendered boxes overlapping with this text & the close button
        mStack.pushMatrix();
        GuiUtils.drawCenteredText(ctx, textRenderer, Text.translatable("wildfire_gender.credits.title"), width / 2, height / 2 - 100, ColorHelper.fullAlpha(0xFFFFFF));
        GuiUtils.drawCenteredText(ctx, textRenderer, Text.translatable("wildfire_gender.credits.description"), width / 2, height / 2 - 85, ColorHelper.fullAlpha(0x888888));
        mStack.popMatrix();

        //draw button container
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, BUTTON_CONTAINER, this.width / 2 - (190 / 2), navigationY, 0, 0, 190, 25, 190, 25);

        int columns = 6;
        int boxW = 60;
        int boxH = 74;

        // Calculate which range of contributors to render
        int startIndex = creditsPage * boxesPerPage;
        int endIndex = Math.min(startIndex + boxesPerPage, CREDIT_BOXES.length);

        int startY = height / 2 - (2 * boxH) / 2 + 4;

        for (int i = startIndex; i < endIndex; i++) {
            var creditBox = CREDIT_BOXES[i];

            int localIndex = i - startIndex;
            int col = localIndex % columns;
            int row = localIndex / columns;

            int remaining = Math.min(endIndex - startIndex - (row * columns), columns);
            int rowWidth = remaining * boxW;
            int startX = (width / 2) - (rowWidth / 2) + 4;

            int creditBoxX = startX + (col * boxW);
            int creditBoxY = startY + (row * boxH);

            // draw box
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

            if (mouseX > xP - 24 && mouseX < xP + 23 && mouseY > yP + 22 && mouseY < yP + 31) {
                List<Text> txtList = new ArrayList<>();
                var role = creditBox.getRoleOrGeneric();
                txtList.add(role.withColor(Text.empty()
                        .append(creditBox.getName())
                        .append(Text.literal(" - ").formatted(Formatting.DARK_GRAY))
                        .append(role.shortName())));
                if (creditBox.getDescription() != null && !creditBox.getDescription().isEmpty()) {
                    txtList.add(Text.literal(creditBox.getDescription()).formatted(Formatting.GRAY));
                }
                ctx.drawTooltip(textRenderer, txtList, mouseX, mouseY);
            }
        }

        //String pageInfo = (creditsPage) + " / " + (totalPages-1);
        //GuiUtils.drawCenteredText(ctx, textRenderer, Text.literal(pageInfo), width / 2, height / 2, ColorHelper.fullAlpha(0xFFFFFF));

        super.render(ctx, mouseX, mouseY, delta);
    }
}

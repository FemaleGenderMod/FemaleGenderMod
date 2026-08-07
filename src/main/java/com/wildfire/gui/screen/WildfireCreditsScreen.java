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
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLang;
import com.wildfire.main.contributors.Contributor;
import com.wildfire.main.contributors.Contributor.Role;
import com.wildfire.main.contributors.Contributors;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.UnknownNullability;

@Environment(EnvType.CLIENT)
public class WildfireCreditsScreen extends BaseWildfireScreen {

    private static final Identifier CREDIT_CONTAINER = WildfireGender.id("credits/credit_container");
    private static final Identifier CREDIT_OUTLINE = WildfireGender.id("credits/credit_outline");
    private static final Identifier BUTTON_CONTAINER = WildfireGender.id("credits/button_container");
    private static final Identifier TAB_CONTAINER = WildfireGender.id("credits/tab_container");

    //General contributor list
    private final FakeGUIPlayer[] C_GENERAL = Contributors.getContributors().entrySet().stream()
            .filter(it -> it.getValue().name() != null)
            .filter(it -> Boolean.TRUE.equals(it.getValue().showInCredits()))
            .filter(it -> it.getValue().getRole() != Contributor.Role.TRANSLATOR) // exclude translators
            .sorted(Comparator.comparing(it -> it.getValue().name()))
            .sorted(Comparator.comparing(it -> it.getValue().getRole()))
            .map(it -> new FakeGUIPlayer(it.getValue().name(), it.getKey(), FakeGUIPlayer.FEMALE_CHANGES))
            .toArray(FakeGUIPlayer[]::new);

    //Translator list
    private final FakeGUIPlayer[] C_TRANSLATORS = Contributors.getContributors().entrySet().stream()
            .filter(it -> it.getValue().name() != null)
            .filter(it -> Boolean.TRUE.equals(it.getValue().showInCredits()))
            .filter(it -> it.getValue().getRole() == Contributor.Role.TRANSLATOR) // only have translators
            .sorted(Comparator.comparing(it -> it.getValue().name()))
            .sorted(Comparator.comparing(it -> it.getValue().getRole()))
            .map(it -> new FakeGUIPlayer(it.getValue().name(), it.getKey(), FakeGUIPlayer.FEMALE_CHANGES))
            .toArray(FakeGUIPlayer[]::new);

    private static final int boxesPerPage = 12;

    private enum Category {
        GENERAL, TRANSLATORS
    }
    private Category categoryTab = Category.GENERAL;
    private int creditsPage = 0;

    public WildfireCreditsScreen(Screen parent, UUID uuid) {
        super(WildfireLang.CREDITS_TITLE.translate(), parent, uuid);
    }

    private int navigationY;

    @Override
    public void init() {
        super.init();
        final var ref = new Object() {
            @UnknownNullability
            AbstractWidget prevPage, nextPage, generalTab, translatorTab;
        };

        navigationY = this.height / 2 + 82;

        //category tab
        ref.generalTab = addButton(builder -> builder
                .message(WildfireLang.CREDITS_GENERAL::translate)
                .position(this.width / 2 - 89, navigationY + 34)
                .size(87, 13)
                .active(categoryTab == Category.TRANSLATORS)
                .onPress(_ -> {
                    categoryTab = Category.GENERAL;
                    creditsPage = 0;
                    ref.prevPage.active = false;
                    ref.nextPage.active = creditsPage < getTotalPages()-1;
                    ref.generalTab.active = false;
                    ref.translatorTab.active = true;

                }));

        ref.translatorTab = addButton(builder -> builder
                .message(WildfireLang.CREDITS_TRANSLATORS::translate)
                .position(this.width / 2 + 2, navigationY + 34)
                .size(87, 13)
                .active(categoryTab == Category.GENERAL)
                .onPress(_ -> {
                    categoryTab = Category.TRANSLATORS;
                    creditsPage = 0;
                    ref.prevPage.active = false;
                    ref.nextPage.active = creditsPage < getTotalPages()-1;
                    ref.generalTab.active = true;
                    ref.translatorTab.active = false;
                }));

        //page tab
        addButton(builder -> builder
                .message(WildfireLang.DETAILS_BACK::translate)
                .position(this.width / 2 - 25, navigationY + 6)
                .size(50, 13)
                .onPress(_ -> onClose()));

        ref.nextPage = addButton(builder -> builder
                .message(WildfireLang.DETAILS_NEXT_PAGE::translate)
                .position(this.width / 2 + 29, navigationY + 6)
                .size(60, 13)
                .active(creditsPage < getTotalPages()-1)
                .onPress(_ -> {
                    if(creditsPage < getTotalPages()-1) {
                        creditsPage++;
                    }
                    ref.prevPage.active = creditsPage != 0;
                    ref.nextPage.active = creditsPage < getTotalPages()-1;
                }));

        ref.prevPage = addButton(builder -> builder
                .message(WildfireLang.DETAILS_PREV_PAGE::translate)
                .position(this.width / 2 - 89, navigationY + 6)
                .size(60, 13)
                .active(creditsPage != 0)
                .onPress(_ -> {
                    if(creditsPage > 0) {
                        creditsPage--;
                    }
                    ref.prevPage.active = creditsPage != 0;
                    ref.nextPage.active = creditsPage < getTotalPages();
                }));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        extractTransparentBackground(graphics);
    }

    @Override
    public void tick() {
        super.tick();
        for(FakeGUIPlayer player : getActiveBoxes()) {
            player.tick();
        }
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) getActiveBoxes().length / boxesPerPage);
    }

    private FakeGUIPlayer[] getActiveBoxes() {
        return categoryTab == Category.TRANSLATORS ? C_TRANSLATORS : C_GENERAL;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        drawScrollingString(graphics, getTitle(), 0, height / 2 - 100, TextAlignment.CENTER, CommonColors.WHITE, graphics.guiWidth(), 5, false);
        drawScrollingString(graphics, WildfireLang.CREDITS_DESCRIPTION.translate(), 0, height / 2 - 85, TextAlignment.CENTER, 0xFF888888, graphics.guiWidth(), 5, false);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON_CONTAINER, this.width / 2 - (190 / 2), navigationY, 190, 25);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TAB_CONTAINER, this.width / 2 - (190 / 2), navigationY + 28, 190, 25);

        int columns = 6;
        int boxW = 60;
        int boxH = 74;

        int startIndex = creditsPage * boxesPerPage;
        int endIndex = Math.min(startIndex + boxesPerPage, getActiveBoxes().length);

        int startY = height / 2 - (2 * boxH) / 2 + 4;

        for (int i = startIndex; i < endIndex; i++) {
            var creditBox = getActiveBoxes()[i];

            int localIndex = i - startIndex;
            int col = localIndex % columns;
            int row = localIndex / columns;

            int remaining = Math.min(endIndex - startIndex - (row * columns), columns);
            int rowWidth = remaining * boxW;
            int startX = (width / 2) - (rowWidth / 2) + 4;

            int creditBoxX = startX + (col * boxW);
            int creditBoxY = startY + (row * boxH);

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CREDIT_CONTAINER, creditBoxX, creditBoxY, 52, 68);

            Role role = creditBox.getRoleOrGeneric();
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CREDIT_OUTLINE, creditBoxX + 3, creditBoxY + 3, 46, 53,
                ARGB.opaque(role.getColor().getValue()));

            int xP = creditBoxX + (52 / 2);
            int yP = creditBoxY + (68 / 2);
            var entity = creditBox.getEntity();
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, xP - 38, yP - 29, xP + 38, yP + 20, 40, getEntityScale(entity, 0.5F), mouseX, mouseY, entity);

            drawScaledScrollingString(graphics, Component.literal(creditBox.getName()), creditBoxX + 3, yP + 23, TextAlignment.CENTER, CommonColors.WHITE, 46,  1, false, 0.55F);

            if (mouseX > xP - 24 && mouseX < xP + 23 && mouseY > yP + 22 && mouseY < yP + 31) {
                List<Component> txtList = new ArrayList<>();
                //~ if >=26.2 'net.minecraft.ChatFormatting' -> 'TextColor'
                txtList.add(WildfireLang.GENERIC_DASH_EXPLANATION.translateColored(TextColor.DARK_GRAY,
                    role.withColor(Component.literal(creditBox.getName())),
                    role.withColor(role.shortName()))
                );
                if (creditBox.getDescription() != null) {
                    //~ if >=26.2 'withStyle(net.minecraft.ChatFormatting.' -> 'withColor(TextColor.'
                    txtList.add(creditBox.getDescription().copy().withColor(TextColor.GRAY));
                }
                graphics.setComponentTooltipForNextFrame(font, txtList, mouseX, mouseY);
            }
        }

        //String pageInfo = (creditsPage) + " / " + (totalPages-1);
        //GuiUtils.drawCenteredText(ctx, textRenderer, Text.literal(pageInfo), width / 2, height / 2, CommonColors.WHITE);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }
}

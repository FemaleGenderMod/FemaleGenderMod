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

package com.wildfire.gui;

import com.wildfire.gui.screen.WildfireBreastCustomizationScreen;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.BreastPresetConfiguration;
import com.wildfire.main.entitydata.PlayerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class WildfireBreastPresetList extends EntryListWidget<WildfireBreastPresetList.Entry> {

    public boolean active = true;
    public boolean visible = true;

    public class BreastPresetListEntry {

        public Identifier ident;
        public String name;
        private BreastPresetConfiguration data;

        public BreastPresetListEntry(String name, BreastPresetConfiguration data) {
            this.name = name;
            this.data = data;
            this.ident = Identifier.of(WildfireGender.MODID, "textures/presets/iknowthisisnull.png");
        }

    }

    private BreastPresetListEntry[] BREAST_PRESETS = new BreastPresetListEntry[] {

    };
    private static final Identifier TXTR_SYNC = Identifier.of(WildfireGender.MODID, "textures/sync.png");
    private static final Identifier TXTR_UNKNOWN = Identifier.of(WildfireGender.MODID, "textures/unknown.png");
    private static final Identifier TXTR_CACHED = Identifier.of(WildfireGender.MODID, "textures/cached.png");
    private final int listWidth;
    private final WildfireBreastCustomizationScreen parent;

    public WildfireBreastPresetList(WildfireBreastCustomizationScreen parent, int listWidth, int top) {
        super(MinecraftClient.getInstance(), 156, parent.height, top, 32);
        this.parent = parent;
        this.listWidth = listWidth;
        this.refreshList();
    }

    public BreastPresetListEntry[] getPresetList() {
        return BREAST_PRESETS;
    }

    @Override
    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {}

    @Override
    protected void drawMenuListBackground(DrawContext context) {}

    // copy of super without the added margin between entries
    @Override
    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        int left = this.getRowLeft();
        int width = this.getRowWidth();
	    int count = this.getEntryCount();

        for(int index = 0; index < count; ++index) {
            int top = this.getRowTop(index);
            int bottom = this.getRowBottom(index);
            if(bottom >= this.getY() && top <= this.getBottom()) {
                this.renderEntry(context, mouseX, mouseY, delta, index, left, top, width, itemHeight);
            }
        }
    }

    @Override
    public int getRowTop(int index) {
        return this.getY() - (int)this.getScrollY() + index * this.itemHeight + this.headerHeight;
    }

    @Override
    protected int getScrollbarX() {
        return parent.width / 2 + 181;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    public void refreshList() {
        this.clearEntries();

        //BREAST_PRESETS
        BreastPresetConfiguration[] CONFIGS = BreastPresetConfiguration.getBreastPresetConfigurationFiles();
        ArrayList<BreastPresetListEntry> tmpPresets = new ArrayList<>();
        for(BreastPresetConfiguration presetCfg : CONFIGS) {
            System.out.println("Preset Name: " + presetCfg.get(BreastPresetConfiguration.PRESET_NAME));
            tmpPresets.add(new BreastPresetListEntry(presetCfg.get(BreastPresetConfiguration.PRESET_NAME), presetCfg));
        }
        BREAST_PRESETS = tmpPresets.toArray(BreastPresetListEntry[]::new);

        if(this.client.world == null || this.client.player == null) return;

        for(BreastPresetListEntry breastPreset : BREAST_PRESETS) {
            addEntry(new Entry(breastPreset));
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    @Environment(EnvType.CLIENT)
    public class Entry extends EntryListWidget.Entry<WildfireBreastPresetList.Entry> {
        private final Identifier thumbnail;
        public final BreastPresetListEntry nInfo;
        private WildfireButton btnOpenGUI;

        private Entry(final BreastPresetListEntry nInfo) {
            this.nInfo = nInfo;
            this.thumbnail = nInfo.ident;
//            btnOpenGUI = new WildfireButton(0, 0, getRowWidth() - 6, itemHeight, Text.empty(), button -> {
//                PlayerConfig plr = Objects.requireNonNull(parent.getPlayer(), "getPlayer()");
//                plr.updateBustSize(nInfo.data.get(BreastPresetConfiguration.BUST_SIZE));
//                plr.getBreasts().updateXOffset(nInfo.data.get(BreastPresetConfiguration.BREASTS_OFFSET_X));
//                plr.getBreasts().updateYOffset(nInfo.data.get(BreastPresetConfiguration.BREASTS_OFFSET_Y));
//                plr.getBreasts().updateZOffset(nInfo.data.get(BreastPresetConfiguration.BREASTS_OFFSET_Z));
//                plr.getBreasts().updateCleavage(nInfo.data.get(BreastPresetConfiguration.BREASTS_CLEAVAGE));
//                plr.getBreasts().updateUniboob(nInfo.data.get(BreastPresetConfiguration.BREASTS_UNIBOOB));
//                PlayerConfig.saveGenderInfo(plr);
//            });
        }

        @Override
        public void render(DrawContext ctx, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            if(!visible) return;

            btnOpenGUI.active = WildfireBreastPresetList.this.active;
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            //ctx.fill(x, y, x + entryWidth, y + entryHeight, 0x55005555);

            ctx.drawTexture(RenderLayer::getGuiTextured, thumbnail, x + 2, y + 2, 0, 0, 28, 28, 28, 28);

            ctx.drawText(font, Text.of(nInfo.name), x + 34, y + 4, 0xFFFFFFFF, false);
            //ctx.drawText(font, Text.translatable("07/25/2023 1:19 AM").formatted(Formatting.ITALIC), x + 34, y + 20, 0xFF888888, false);
            this.btnOpenGUI.setX(x);
            this.btnOpenGUI.setY(y);
            this.btnOpenGUI.render(ctx, mouseX, mouseY, partialTicks);

        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(active && visible) {
                if (this.btnOpenGUI.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
            return false;
        }
    }
}

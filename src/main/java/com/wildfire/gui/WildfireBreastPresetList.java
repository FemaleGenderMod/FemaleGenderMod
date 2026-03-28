package com.wildfire.gui;

import com.wildfire.gui.screen.WildfireBreastCustomizationScreen;
import com.wildfire.main.config.BreastPresetConfiguration;
import com.wildfire.main.entitydata.PlayerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class WildfireBreastPresetList extends ObjectSelectionList<WildfireBreastPresetList.Entry> {
    public boolean active = true;
    public boolean visible = true;
    private BreastPresetListEntry[] BREAST_PRESETS = new BreastPresetListEntry[0];

    private static final ResourceLocation TXTR_SYNC = ResourceLocation.fromNamespaceAndPath("wildfire_gender", "textures/sync.png");
    private static final ResourceLocation TXTR_UNKNOWN = ResourceLocation.fromNamespaceAndPath("wildfire_gender", "textures/unknown.png");
    private static final ResourceLocation TXTR_CACHED = ResourceLocation.fromNamespaceAndPath("wildfire_gender", "textures/cached.png");

    private final int listWidth;
    private final WildfireBreastCustomizationScreen parent;

    public WildfireBreastPresetList(WildfireBreastCustomizationScreen parent, int listWidth, int top) {
        // Super: Minecraft, width, height, top, itemHeight
        super(Minecraft.getInstance(), 156, parent.height, top, 32);
        this.setRenderHeader(false, 0); // method_25315
        this.parent = parent;
        this.listWidth = listWidth;
        this.refreshList();
    }

    public BreastPresetListEntry[] getPresetList() {
        return this.BREAST_PRESETS;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.parent.width / 2 + 181;
    }

    public void refreshList() {
        this.clearEntries(); // method_25339
        BreastPresetConfiguration[] configs = BreastPresetConfiguration.getBreastPresetConfigurationFiles();
        ArrayList<BreastPresetListEntry> tmpPresets = new ArrayList<>();

        for (BreastPresetConfiguration presetCfg : configs) {
            String name = (String) presetCfg.get(BreastPresetConfiguration.PRESET_NAME);
            tmpPresets.add(new BreastPresetListEntry(name, presetCfg));
        }

        this.BREAST_PRESETS = tmpPresets.toArray(new BreastPresetListEntry[0]);

        if (Minecraft.getInstance().level != null && Minecraft.getInstance().player != null) {
            for (BreastPresetListEntry breastPreset : this.BREAST_PRESETS) {
                this.addEntry(new Entry(breastPreset));
            }
        }
    }

    // --- CLASES INTERNAS ---

    public static class BreastPresetListEntry {
        public ResourceLocation ident;
        public String name;
        private final BreastPresetConfiguration data;

        public BreastPresetListEntry(String name, BreastPresetConfiguration data) {
            this.name = name;
            this.data = data;
            this.ident = ResourceLocation.fromNamespaceAndPath("wildfire_gender", "textures/presets/iknowthisisnull.png");
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final ResourceLocation thumbnail;
        public final BreastPresetListEntry nInfo;
        private final WildfireButton btnOpenGUI;

        private Entry(final BreastPresetListEntry nInfo) {
            this.nInfo = nInfo;
            this.thumbnail = nInfo.ident;

            // Creamos el botón invisible que cubre la entrada de la lista
            this.btnOpenGUI = new WildfireButton(0, 0, WildfireBreastPresetList.this.getRowWidth() - 6, 32, Component.empty(), (button) -> {
                PlayerConfig plr = Objects.requireNonNull(WildfireBreastPresetList.this.parent.getPlayer(), "getPlayer()");
                plr.updateBustSize((Float) nInfo.data.get(BreastPresetConfiguration.BUST_SIZE));
                plr.getBreasts().updateXOffset((Float) nInfo.data.get(BreastPresetConfiguration.BREASTS_OFFSET_X));
                plr.getBreasts().updateYOffset((Float) nInfo.data.get(BreastPresetConfiguration.BREASTS_OFFSET_Y));
                plr.getBreasts().updateZOffset((Float) nInfo.data.get(BreastPresetConfiguration.BREASTS_OFFSET_Z));
                plr.getBreasts().updateCleavage((Float) nInfo.data.get(BreastPresetConfiguration.BREASTS_CLEAVAGE));
                plr.getBreasts().updateUniboob((Boolean) nInfo.data.get(BreastPresetConfiguration.BREASTS_UNIBOOB));
                PlayerConfig.saveGenderInfo(plr);
            });
        }

        @Override
        public void render(GuiGraphics ctx, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            if (WildfireBreastPresetList.this.visible) {
                this.btnOpenGUI.active = WildfireBreastPresetList.this.active;
                Font font = Minecraft.getInstance().font;

                // Dibujar miniatura
                ctx.blit(this.thumbnail, x + 2, y + 2, 0.0F, 0.0F, 28, 28, 28, 28);

                // Dibujar nombre del preset
                ctx.drawString(font, Component.literal(this.nInfo.name), x + 34, y + 4, -1, false);

                // Posicionar y renderizar el botón de la entrada
                this.btnOpenGUI.setX(x);
                this.btnOpenGUI.setY(y);
                this.btnOpenGUI.render(ctx, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (WildfireBreastPresetList.this.active && WildfireBreastPresetList.this.visible) {
                return this.btnOpenGUI.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal(this.nInfo.name);
        }
    }
}
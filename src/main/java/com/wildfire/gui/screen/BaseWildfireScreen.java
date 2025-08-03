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

import com.wildfire.gui.WildfireButton;
import com.wildfire.gui.WildfireSlider;
import com.wildfire.gui.GuiUtils;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.WildfireGender;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class BaseWildfireScreen extends Screen {

    protected final UUID playerUUID;
    protected final Screen parent;

    //Keira Emberlyn - The Mod's New Mascot
    protected static final Identifier KEIRA_LOOK = Identifier.of(WildfireGender.MODID, "textures/gui/mascot/keira_look.png");
    protected static final Identifier KEIRA_WAVE = Identifier.of(WildfireGender.MODID, "textures/gui/mascot/keira_wave.png");
    protected static final Identifier KEIRA_LEATHER = Identifier.of(WildfireGender.MODID, "textures/gui/mascot/keira_leather.png");
    protected static final Identifier KEIRA_NETHERITE = Identifier.of(WildfireGender.MODID, "textures/gui/mascot/keira_netherite.png");
    protected static final int KEIRA_WIDTH = 610;
    protected static final int KEIRA_HEIGHT = 736;
    //Keira test ctx.drawTexture(RenderPipelines.GUI_TEXTURED, KEIRA_LOOK, x, y, 0, 0, 26, 26, KEIRA_WIDTH, KEIRA_HEIGHT, KEIRA_WIDTH, KEIRA_HEIGHT);

    protected BaseWildfireScreen(Text title, Screen parent, UUID uuid) {
        super(title);
        this.parent = parent;
        this.playerUUID = uuid;
    }

    protected WildfireButton addButton(Consumer<WildfireButton.Builder> builder) {
        var buttonBuilder = new WildfireButton.Builder();
        builder.accept(buttonBuilder);
        return addDrawableChild(buttonBuilder.build());
    }

    protected WildfireSlider addSlider(Consumer<WildfireSlider.Builder> builder) {
        var sliderBuilder = new WildfireSlider.Builder();
        sliderBuilder.save(ignored -> Objects.requireNonNull(getPlayer(), "getPlayer()").save());
        builder.accept(sliderBuilder);
        return addDrawableChild(sliderBuilder.build());
    }

    public @Nullable PlayerConfig getPlayer() {
        return WildfireGender.getPlayerById(this.playerUUID);
    }

    protected void renderPlayerInFrame(DrawContext ctx, int xP, int yP, int mouseX, int mouseY) {
        var player = Objects.requireNonNull(client).player;
        if(player == null) return;
        // This sucks. In order to position the player properly, we need to trick the player renderer into
        // thinking the area the player should be rendered is much taller than it actually is.
        ctx.enableScissor(xP - 38, yP - 79, xP + 38, yP + 9);
        GuiUtils.drawEntityOnScreenNoScissor(ctx, xP - 38, yP - 79, xP + 38, yP + 69, 70, mouseX, mouseY + 35, player);
        ctx.disableScissor();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        Objects.requireNonNull(client).setScreen(parent);
    }
}

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

package com.wildfire.client.gui.screen;

import com.wildfire.client.gui.IFancyFontRenderer;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.WildfireGender;
import java.util.UUID;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseWildfireScreen extends Screen implements IFancyFontRenderer {

    //Keira Emberlyn - The Mod's New Mascot
    protected static final ResourceLocation KEIRA_LOOK = WildfireGender.rl("textures/gui/mascot/keira_look.png");
    protected static final ResourceLocation KEIRA_WAVE = WildfireGender.rl("textures/gui/mascot/keira_wave.png");
    protected static final ResourceLocation KEIRA_LEATHER = WildfireGender.rl("textures/gui/mascot/keira_leather.png");
    protected static final ResourceLocation KEIRA_NETHERITE = WildfireGender.rl("textures/gui/mascot/keira_netherite.png");
    protected static final int KEIRA_WIDTH = 610;
    protected static final int KEIRA_HEIGHT = 736;

    protected final UUID playerUUID;
    protected final Screen parent;

    protected BaseWildfireScreen(Component title, Screen parent, UUID uuid) {
        super(title);
        this.parent = parent;
        this.playerUUID = uuid;
    }

    public PlayerConfig getPlayer() {
        return WildfireGender.getPlayerById(this.playerUUID);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        //TODO - 1.21: Re-evaluate this
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public Font font() {
        return font;
    }
}
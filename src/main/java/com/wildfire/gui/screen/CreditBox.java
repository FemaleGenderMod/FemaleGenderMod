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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.wildfire.main.config.ClientConfig;
import com.wildfire.main.entitydata.PlayerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;

import java.util.UUID;

public class CreditBox {

    private String name;
    private UUID uuid;

    private PlayerConfig plrCfg;

    private AbstractClientPlayerEntity entity;
    public CreditBox(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;

        if(MinecraftClient.getInstance().world == null) return;

        // Create a fake profile
        MinecraftSessionService sessionService = MinecraftClient.getInstance().getApiServices().sessionService();
        PlayerSkinProvider skinProvider = MinecraftClient.getInstance().getSkinProvider();

        GameProfile filledProfile = sessionService.fetchProfile(uuid, true).profile();

        //Get the skin from Mojang
        skinProvider.fetchSkinTextures(filledProfile).thenAccept(skin -> {
            skin.ifPresent(skinTex -> {
                boolean slim = skinTex.model() == PlayerSkinType.SLIM;

                //Create the fake player entity
                entity = new AbstractClientPlayerEntity(MinecraftClient.getInstance().world, filledProfile) {
                    @Override
                    public SkinTextures getSkin() {
                        return skinTex;
                    }
                    @Override
                    public boolean isModelPartVisible(PlayerModelPart part) {
                        return true;
                    }
                };
            });
        });
    }

    public AbstractClientPlayerEntity getEntity() {
        return entity;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }
}

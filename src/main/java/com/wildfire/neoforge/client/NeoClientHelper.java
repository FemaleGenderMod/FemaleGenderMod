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

package com.wildfire.neoforge.client;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.wildfire.api.WildfireAPI;
import com.wildfire.client.ClientHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

public class NeoClientHelper implements ClientHelper {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, WildfireAPI.MODID);

    private static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_HURT = SOUND_EVENTS.register("female_hurt", SoundEvent::createVariableRangeEvent);

    @Override
    public Holder<SoundEvent> femaleHurt() {
        return FEMALE_HURT;
    }

    @Override
    public String getSessionUrl(final MinecraftSessionService service) {
        String baseUrl;
        //Note: We need to use reflection here as Neo protects certain packages from coremods
        baseUrl = ObfuscationReflectionHelper.getPrivateValue(
            YggdrasilMinecraftSessionService.class,
            (YggdrasilMinecraftSessionService) service,
            "baseUrl"
        );
        return baseUrl;
    }

    @Override
    public boolean shouldOverlayHurtSound(@Nullable Player player) {
        //Note: We handle all hurt sounds ourselves via the PlayLevelSoundEvent.AtEntity event on NeoForge,
        // so we never want to additionally play our own hurt sound on top of the vanilla one
        return false;
    }
}

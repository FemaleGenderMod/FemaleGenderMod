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

package com.wildfire.client;

import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.wildfire.common.WildfireGender;
import com.wildfire.client.render.GenderRenderState;
import java.util.Objects;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.context.ContextKey;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

public class NeoClientHelper implements ClientHelper {

    public static final ContextKey<GenderRenderState> STATE = new ContextKey<>(WildfireGender.id("gender_state"));
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, WildfireGender.MODID);

    private static final DeferredHolder<SoundEvent, SoundEvent> FEMALE_HURT = SOUND_EVENTS.register("female_hurt", SoundEvent::createVariableRangeEvent);

    @Override
    public SoundEvent femaleHurt() {
        return FEMALE_HURT.get();
    }

    @Nullable
    @Override
    public GenderRenderState getRenderState(HumanoidRenderState state) {
        return state.getRenderData(STATE);
    }

    @Override
    public boolean validateSessionUrl(final YggdrasilMinecraftSessionService service, final String expected) {
        //Note: We need to use reflection here as Neo protects certain packages from coremods
        String baseUrl = ObfuscationReflectionHelper.getPrivateValue(YggdrasilMinecraftSessionService.class, service, "baseUrl");
        return Objects.equals(baseUrl, expected);
    }
}

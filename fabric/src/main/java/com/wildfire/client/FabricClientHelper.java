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
import com.wildfire.mixins.accessors.YggdrasilMinecraftSessionServiceAccessor;
import com.wildfire.render.GenderRenderState;
import java.util.Objects;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.jspecify.annotations.Nullable;

public class FabricClientHelper implements ClientHelper {

    public static final RenderStateDataKey<GenderRenderState> STATE = RenderStateDataKey.create(() -> "GenderRenderState");

    @Override
    public @Nullable GenderRenderState getRenderState(HumanoidRenderState state) {
        return state.getData(STATE);
    }

    @Override
    public boolean validateSessionUrl(final YggdrasilMinecraftSessionService service, String expected) {
        var accessor = (YggdrasilMinecraftSessionServiceAccessor) service;
        return Objects.equals(accessor.getBaseUrl(), expected);
    }
}

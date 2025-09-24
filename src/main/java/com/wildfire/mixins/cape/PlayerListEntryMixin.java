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

package com.wildfire.mixins.cape;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import com.wildfire.main.cape.CapeProvider;
import com.wildfire.main.cape.SkinTexturesWildfire;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerListEntry.class)
abstract class PlayerListEntryMixin {
    @Shadow
    @Final
    private GameProfile profile;

    @SuppressWarnings("DataFlowIssue")
    @ModifyReturnValue(method = "getSkinTextures", at = @At("RETURN"))
    public SkinTextures wildfiregender$replaceCapeTexture(SkinTextures original) {
        var cape = CapeProvider.CACHE.getUnchecked(profile);
        var duck = ((SkinTexturesWildfire)(Object)original);
        var tex = cape.getNow(null);
        duck.wildfiregender$overrideCapeTexture(tex);
        return original;
    }
}
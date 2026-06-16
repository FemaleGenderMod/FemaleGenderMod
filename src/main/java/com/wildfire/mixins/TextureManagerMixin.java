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

package com.wildfire.mixins;

import com.wildfire.render.ducks.MissingTextureLogger;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(TextureManager.class)
abstract class TextureManagerMixin implements MissingTextureLogger {
    private static final @Unique Set<Identifier> wildfire_gender$missingTextures = ObjectSets.synchronize(new ObjectOpenHashSet<>());

    // TODO there's probably a better way to do this, but this is the easy and cheap way to do this
    // this does mean there's likely going to be a frame where the texture will throw an error, but it won't
    // crash the game as we're (probably unreasonably) try/catch-ing every error in the feature renderers.
    @Inject(
            method = "loadContentsSafe",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureContents;createMissing()Lnet/minecraft/client/renderer/texture/TextureContents;")
    )
    private void wildfire_gender$logMissingTexture(Identifier textureId, ReloadableTexture texture, CallbackInfoReturnable<TextureContents> cir) {
        wildfire_gender$missingTextures.add(textureId);
    }

    @Inject(
            method = "loadContents",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureContents;createMissing()Lnet/minecraft/client/renderer/texture/TextureContents;")
    )
    private static void wildfire_gender$logMissingTexture(ResourceManager manager, Identifier location, ReloadableTexture texture, CallbackInfoReturnable<TextureContents> cir) {
        wildfire_gender$missingTextures.add(location);
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void wildfire_gender$clearMissingTextures(CallbackInfo ci) {
        wildfire_gender$missingTextures.clear();
    }

    @Inject(method = "lambda$scheduleLoad$0", at = @At("HEAD"))
    private static void wildfire_gender$removeOnReload(ResourceManager manager, Identifier location, ReloadableTexture texture, CallbackInfoReturnable<TextureContents> cir) {
        wildfire_gender$missingTextures.remove(location);
    }

    @Override
    public Set<Identifier> wildfire_gender$missingTextures() {
        return wildfire_gender$missingTextures;
    }
}

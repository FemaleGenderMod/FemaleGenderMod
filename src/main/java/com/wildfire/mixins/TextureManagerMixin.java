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
import net.minecraft.resources.ResourceLocation;
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
	private static final @Unique Set<ResourceLocation> wildfire_gender$missingTextures = ObjectSets.synchronize(new ObjectOpenHashSet<>());

	@Inject(
			method = "loadContentsSafe",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureContents;createMissing()Lnet/minecraft/client/renderer/texture/TextureContents;")
	)
	private void wildfire_gender$logMissingTexture(ResourceLocation id, ReloadableTexture texture, CallbackInfoReturnable<TextureContents> cir) {
		wildfire_gender$missingTextures.add(id);
	}

	@Inject(
			method = "loadContents",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureContents;createMissing()Lnet/minecraft/client/renderer/texture/TextureContents;")
	)
	private static void wildfire_gender$logMissingTexture(ResourceManager resourceManager, ResourceLocation id, ReloadableTexture texture, CallbackInfoReturnable<TextureContents> cir) {
		wildfire_gender$missingTextures.add(id);
	}

	@Inject(method = "close", at = @At("TAIL"))
	private void wildfire_gender$clearMissingTextures(CallbackInfo ci) {
		wildfire_gender$missingTextures.clear();
	}

	// targets the CompletableFuture.supplyAsync() lambda in #scheduleLoad
	@Inject(method = "method_65881", at = @At("HEAD"))
	private static void wildfire_gender$removeOnReload(ResourceManager resourceManager, ResourceLocation textureId, ReloadableTexture reloadableTexture, CallbackInfoReturnable<TextureContents> cir) {
		wildfire_gender$missingTextures.remove(textureId);
	}

	@Override
	public Set<ResourceLocation> wildfire_gender$missingTextures() {
		return wildfire_gender$missingTextures;
	}
}

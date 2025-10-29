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

package com.wildfire.main.cape;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import com.wildfire.main.WildfireGender;
import org.jetbrains.annotations.Nullable;
import java.io.FileNotFoundException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.ResourceLocation;

/**
 * Based on <a href="https://git.celestialfault.dev/celeste/kappa">celeste's fork</a>
 * of the <a href="https://modrinth.com/mod/kappa">Kappa mod</a>
 */
public class CapeProvider {
    private static final Duration CACHE_DURATION = Util.make(() -> {
        if(Boolean.getBoolean("wildfiregender.capes.cache.debug")) {
            return Duration.ofSeconds(5);
        }
        return Duration.ofMinutes(30);
    });

    public static final LoadingCache<GameProfile, CompletableFuture<ClientAsset.Texture>> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(CACHE_DURATION)
            .removalListener(CapeProvider::remove)
            .build(CacheLoader.from(CapeProvider::loadCape));

    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9_]{1,16}$", Pattern.CASE_INSENSITIVE);
    private static final String CAPE_URL = "https://femalegendermod.net/capes/{uuid}.png";

    private static void remove(RemovalNotification<GameProfile, CompletableFuture<ClientAsset.Texture>> entry) {
        var future = entry.getValue();
        if(future == null) {
            WildfireGender.LOGGER.warn("Got a null value for removed cache entry with key {}; this shouldn't happen!", entry.getKey());
            return;
        }

        var tex = future.getNow(null);
        if(tex != null) {
            var id = tex.texturePath();
            WildfireGender.LOGGER.debug("Destroying texture {}", id);
            Minecraft.getInstance().getTextureManager().release(id);
        }
    }

    // This loads the cape for one player, doesn't matter if it's the player or not.
    private static CompletableFuture<ClientAsset.Texture> loadCape(GameProfile player) {
        return CompletableFuture.supplyAsync(() -> {
            // immediately ignore any profiles that look obviously invalid; while it's possible that this
            // could ignore valid profiles (illegal characters in usernames have been known to exist),
            // odds are they're infinitely more likely to be NPCs than real players.
            if(player.id().version() != 4 || !USERNAME.matcher(player.name()).matches()) {
                return null;
            }

            return tryUrl(player, CAPE_URL.replace("{uuid}", player.id().toString()));
        }, Util.ioPool());
    }

    // This is a provider specific implementation.
    // Images are usually 46x22 or 92x44, and these work as expected (64x32, 128x64).
    // There are edge cages with sizes 184x88, 1024x512 and 2048x1024,
    // but these should work alright.
    private static NativeImage uncrop(NativeImage in) {
        int srcHeight = in.getHeight(), srcWidth = in.getWidth();
        int zoom = (int) Math.ceil(in.getHeight() / 32f);
        NativeImage out = new NativeImage(64 * zoom, 32 * zoom, true);
        for (int x = 0; x < srcWidth; x++) {
            for (int y = 0; y < srcHeight; y++) {
                out.setPixel(x, y, in.getPixel(x, y));
            }
        }
        return out;
    }

    // Try to load a cape from a URL.
    // If this fails, it'll return null, and let us try another url.
    private static @Nullable ClientAsset.Texture tryUrl(GameProfile player, String urlFrom) {
        try {
            WildfireGender.LOGGER.debug("Attempting to fetch cape from {}", urlFrom);
            var url = URI.create(urlFrom).toURL();
            var image = uncrop(NativeImage.read(url.openStream()));
            WildfireGender.LOGGER.debug("Got cape texture");

            var id = ResourceLocation.fromNamespaceAndPath(WildfireGender.MODID, "cape/" + player.id().toString().replace("-", ""));
            register(id, image).join();

            return new ClientAsset.DownloadedTexture(id, urlFrom);
        } catch(FileNotFoundException e) {
            // Getting the cape was successful! But there's no cape, so don't retry.
            WildfireGender.LOGGER.debug("No cape texture found");
            return null;
        } catch(Exception e) {
            WildfireGender.LOGGER.error("Failed to fetch cape texture", e);
            return null;
        }
    }

    private static CompletableFuture<Void> register(final ResourceLocation id, final NativeImage image) {
        return Minecraft.getInstance().submit(() -> {
            var texture = new DynamicTexture(id::toString, image);
            Minecraft.getInstance().getTextureManager().register(id, texture);
        });
    }

    private CapeProvider() {
        throw new UnsupportedOperationException();
    }
}

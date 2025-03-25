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
import com.wildfire.main.WildfireGender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.FileNotFoundException;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Based on <a href="https://git.celestialfault.dev/celeste/kappa">celeste's fork</a>
 * of the <a href="https://modrinth.com/mod/kappa">Kappa mod</a>
 */
public class CapeProvider {
    /**
     * Sentinel {@link Identifier} returned if the player's cape was obtained successfully, but they have no cape to display
     */
    public static final Identifier NO_CAPE = Identifier.of(WildfireGender.MODID, "no_cape");

    private static final Duration CACHE_DURATION = Util.make(() -> {
        if(Objects.equals(System.getProperty("wildfiregender.capes.cache.debug"), "true")) {
            return Duration.ofSeconds(5);
        }
        return Duration.ofMinutes(30);
    });

    public static final LoadingCache<GameProfile, CompletableFuture<Identifier>> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(CACHE_DURATION)
            .removalListener(CapeProvider::remove)
            .build(new CacheLoader<>() {
                @NotNull
                @Override
                public CompletableFuture<@Nullable Identifier> load(@NotNull GameProfile key) {
                    return loadCape(key);
                }
            });

    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9_]{1,16}$", Pattern.CASE_INSENSITIVE);
    private static final String CAPE_URL = "https://femalegendermod.net/capes/{uuid}.png";

    private static void remove(RemovalNotification<GameProfile, CompletableFuture<@Nullable Identifier>> entry) {
        var future = entry.getValue();
        if(future == null) {
            WildfireGender.LOGGER.warn("Got a null value for removed cache entry with key {}; this shouldn't happen!", entry.getKey());
            return;
        }

        var id = future.getNow(null);
        if(id != null) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(id); //destroy texture immediately
        }
    }

    // This loads the cape for one player, doesn't matter if it's the player or not.
    private static CompletableFuture<@Nullable Identifier> loadCape(GameProfile player) {
        return CompletableFuture.supplyAsync(() -> {
            // immediately ignore any profiles that look obviously invalid; while it's possible that this
            // could ignore valid profiles (illegal characters in usernames have been known to exist),
            // odds are they're infinitely more likely to be NPCs than real players.
            if(player.getId().version() != 4 || !USERNAME.matcher(player.getName()).matches()) {
                return null;
            }

            /*if(texture == null) { //fallback url if existed, which it doesn't.
                texture = tryUrl(player, FALLBACK_CAPE_URL.replace("{uuid}", player.getId().toString()));
            }*/
            return tryUrl(player, CAPE_URL.replace("{uuid}", player.getId().toString()));
        }, Util.getIoWorkerExecutor());
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
                out.setColorArgb(x, y, in.getColorArgb(x, y));
            }
        }
        return out;
    }

    // Try to load a cape from an URL.
    // If this fails, it'll return null, and let us try another url.
    private static @Nullable Identifier tryUrl(GameProfile player, String urlFrom) {
        try {
            WildfireGender.LOGGER.debug("Attempting to fetch cape from {}", urlFrom);
            var url = URI.create(urlFrom).toURL();
            var image = uncrop(NativeImage.read(url.openStream()));
            WildfireGender.LOGGER.debug("Got cape texture");

            var id = Identifier.of(WildfireGender.MODID, "cape/" + player.getId().toString().replace("-", ""));
            var texture = new NativeImageBackedTexture(id::toString, image);
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);

            return id;
        } catch(FileNotFoundException e) {
            // Getting the cape was successful! But there's no cape, so don't retry.
            WildfireGender.LOGGER.debug("No cape texture found");
            // TODO this feels janky, but would probably require a special Optional-like class
            //      with a dedicated missing state to properly work around this
            return NO_CAPE;
        } catch(Exception e) {
            WildfireGender.LOGGER.error("Failed to fetch cape texture", e);
            return null;
        }
    }

    private CapeProvider() {
        throw new UnsupportedOperationException();
    }
}

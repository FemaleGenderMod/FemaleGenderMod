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

package com.wildfire.client.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.wildfire.api.IGenderArmor;
import com.wildfire.api.data.GenderArmorProvider;
import com.wildfire.common.WildfireGender;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

/// @apiNote Only use this on the client side
public final class GenderArmorResourceManager extends SimpleJsonResourceReloadListener {

    public static final ResourceLocation ID = WildfireGender.id("armor_data");
    public static final GenderArmorResourceManager INSTANCE = new GenderArmorResourceManager();
    @Deprecated(forRemoval = true)
    private static final GenderArmorResourceManager LEGACY = new GenderArmorResourceManager("wildfire_gender_data", true);

    @Deprecated(forRemoval = true)
    private final boolean isLegacy;
    private @Unmodifiable Map<ResourceLocation, IGenderArmor> configs = Map.of();

    private GenderArmorResourceManager() {
        this(GenderArmorProvider.PREFIX, false);
    }

    private GenderArmorResourceManager(String path, boolean isLegacy) {
        super(new Gson(), path);
        this.isLegacy = isLegacy;
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        if (isLegacy) {
            return super.prepare(manager, profiler);
        }
        Map<ResourceLocation, JsonElement> result = new HashMap<>(LEGACY.prepare(manager, profiler));
        for (final ResourceLocation key : result.keySet()) {
            WildfireGender.LOGGER.warn("Gender Armor config: '{}' should be moved to the new folder path: '{}'", key, GenderArmorProvider.PREFIX);
        }
        result.putAll(super.prepare(manager, profiler));
        return result;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, IGenderArmor> parsed = new HashMap<>();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : prepared.entrySet()) {
            IGenderArmor.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, entry.getValue())
                .resultOrPartial(error -> WildfireGender.LOGGER.error("Failed to parse gender armor config '{}': {}", entry.getKey(), error))
                .ifPresent(armor -> parsed.put(entry.getKey(), armor));
        }
        this.configs = Collections.unmodifiableMap(parsed);
        WildfireGender.LOGGER.info("Loaded {} gender armor configs", parsed.size());
    }

    public static @Nullable IGenderArmor get(ResourceLocation model) {
        return INSTANCE.configs.get(model);
    }

    public static Optional<IGenderArmor> get(ItemStack item) {
        if (item.getItem() instanceof ArmorItem armorItem) {
            return Optional.ofNullable(armorItem.getMaterial().unwrapKey()
                .map(key -> get(key.location()))
                .orElse(null));
        }
        return Optional.empty();
    }
}

package com.wildfire.client.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.wildfire.api.IGenderArmor;
import com.wildfire.main.WildfireGender;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public class GenderArmorResourceManager extends SimpleJsonResourceReloadListener {

    public static final GenderArmorResourceManager INSTANCE = new GenderArmorResourceManager();

    @Unmodifiable
    private Map<ResourceLocation, IGenderArmor> configs = Collections.emptyMap();

    private GenderArmorResourceManager() {
        //super(IGenderArmor.CODEC, FiledToIdConverter.json("wildfire_gender_data"));
        super(new Gson(), "wildfire_gender_data");
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> prepared, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, IGenderArmor> built = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : prepared.entrySet()) {
            DataResult<IGenderArmor> decode = IGenderArmor.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if (decode.isSuccess()) {
                built.put(entry.getKey(), decode.getOrThrow());
            } else {
                decode.ifError(error -> WildfireGender.LOGGER.error("Failed to load gender armor {}. {}", entry.getKey(), error.message()));
            }
        }
        this.configs = Collections.unmodifiableMap(built);
    }

    @Nullable
    public static IGenderArmor get(ResourceLocation model) {
        return INSTANCE.configs.get(model);
    }

    @Nullable
    public static IGenderArmor get(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            ResourceKey<ArmorMaterial> key = armorItem.getMaterial().getKey();
            if (key != null) {
                return get(key.location());
            }
        }
        ResourceLocation name = BuiltInRegistries.ITEM.getKeyOrNull(stack.getItem());
        if (name != null) {
            return get(name);
        }
        return null;
        //TODO - 1.21.4: Switch to this
        /*return Optional.ofNullable(stack.get(DataComponents.EQUIPPABLE))
              .flatMap(Equippable::assetId)
              .map(ResourceKey::location)
              .map(GenderArmorResourceManager::get);*/
    }
}

package com.wildfire.client.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.wildfire.api.IGenderArmor;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
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
        //TODO - 1.21: Figure this out
        //this.configs = Collections.unmodifiableMap(prepared);
    }

    @Nullable
    public static IGenderArmor get(ResourceLocation model) {
        return INSTANCE.configs.get(model);
    }

    /*public static Optional<IGenderArmor> get(ItemStack stack) {
        //TODO - 1.21.4: Switch to this
        return Optional.ofNullable(stack.get(DataComponents.EQUIPPABLE))
              .flatMap(Equippable::assetId)
              .map(ResourceKey::location)
              .map(GenderArmorResourceManager::get);
    }*/
}

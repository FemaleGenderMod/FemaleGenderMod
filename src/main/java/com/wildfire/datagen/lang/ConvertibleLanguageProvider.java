package com.wildfire.datagen.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

/// [From Mekanism](https://github.com/mekanism/Mekanism/blob/26.2/src/datagen/main/java/mekanism/client/lang/ConvertibleLanguageProvider.java) but adjusted to work with
/// fabric datagen
public abstract class ConvertibleLanguageProvider {

    private final Map<String, String> data = new TreeMap<>();
    private final String locale;

    public ConvertibleLanguageProvider(String locale) {
        this.locale = locale;
    }

    public abstract void convert(String key, String raw, List<FormatSplitter.Component> splitEnglish);

    public CompletableFuture<?> save(CachedOutput cache, Function<String, Path> pathCreator) {
        final JsonElement json = Codec.unboundedMap(Codec.STRING, Codec.STRING).encode(this.data, JsonOps.INSTANCE, new JsonObject()).getOrThrow();
        return DataProvider.saveStable(cache, json, pathCreator.apply(locale));
    }

    protected void add(String key, String translation) {
        data.put(key, translation);
    }
}

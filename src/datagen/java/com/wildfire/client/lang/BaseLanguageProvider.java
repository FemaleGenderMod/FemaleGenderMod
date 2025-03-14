package com.wildfire.client.lang;

import com.wildfire.client.lang.FormatSplitter.Component;
import com.wildfire.main.text.IHasTranslationKey;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;

/**
 * From <a href="https://github.com/mekanism/Mekanism/blob/1.21.x/src/datagen/main/java/mekanism/client/lang/BaseLanguageProvider.java">Mekanism</a>
 */
public abstract class BaseLanguageProvider extends LanguageProvider {

    private final ConvertibleLanguageProvider[] altProviders;
    protected final String modName;
    protected final String basicModName;
    private final String modid;

    protected BaseLanguageProvider(PackOutput output, String modid, String modName) {
        super(output, modid, "en_us");
        this.modid = modid;
        this.modName = modName;
        this.basicModName = modName.replaceAll(":", "");
        altProviders = new ConvertibleLanguageProvider[]{
              new UpsideDownLanguageProvider(output, modid),
              new NonAmericanLanguageProvider(output, modid, "en_au"),
              new NonAmericanLanguageProvider(output, modid, "en_gb")
        };
    }

    protected void addPackData(IHasTranslationKey name, IHasTranslationKey packDescription) {
        add(name, modName);
        add(packDescription, "Resources used for " + modName);
    }

    protected void addModInfo(String description) {
        add("fml.menu.mods.info.description." + modid, description);
    }

    protected void addHolder(Holder<? extends IHasTranslationKey> key, String value) {
        add(key.value(), value);
    }

    protected void add(IHasTranslationKey key, String value) {
        add(key.getTranslationKey(), value);
    }

    //TODO - 1.21: Make it so that we can translate the global config and have it show up in the config gui
    /*private String getConfigSectionTranslationPath(IMekanismConfig config) {
        String baseConfigFolder = Mekanism.MOD_NAME.toLowerCase(Locale.ROOT);
        String fileName = config.getFileName().replaceAll("[^a-zA-Z0-9]+", ".").toLowerCase(Locale.ROOT);
        return modid + ".configuration.section." + baseConfigFolder + "." + fileName + ".toml";
    }

    protected void addConfigs(Collection<IMekanismConfig> configs) {
        add(modid + ".configuration.title", modName + " Config");
        for (IMekanismConfig config : configs) {
            String key = getConfigSectionTranslationPath(config);
            add(key, config.getTranslation());
            add(key + ".title", modName + " - " + config.getTranslation());
        }
    }

    protected void addConfigs(IConfigTranslation... translations) {
        for (IConfigTranslation translation : translations) {
            add(translation, translation.title());
            add(translation.getTranslationKey() + ".tooltip", translation.tooltip());
            String button = translation.button();
            if (button != null) {
                add(translation.getTranslationKey() + ".button", button);
            }
        }
    }*/

    @Override
    public void add(@NotNull String key, @NotNull String value) {
        if (value.contains("%s")) {
            throw new IllegalArgumentException("Values containing substitutions should use explicit numbered indices: " + key + " - " + value);
        }
        super.add(key, value);
        if (altProviders.length > 0) {
            List<Component> splitEnglish = FormatSplitter.split(value);
            for (ConvertibleLanguageProvider provider : altProviders) {
                provider.convert(key, value, splitEnglish);
            }
        }
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull CachedOutput cache) {
        CompletableFuture<?> future = super.run(cache);
        if (altProviders.length > 0) {
            CompletableFuture<?>[] futures = new CompletableFuture[altProviders.length + 1];
            futures[0] = future;
            for (int i = 0; i < altProviders.length; i++) {
                futures[i + 1] = altProviders[i].run(cache);
            }
            return CompletableFuture.allOf(futures);
        }
        return future;
    }
}

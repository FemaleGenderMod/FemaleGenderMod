package com.wildfire.client.lang;

import com.wildfire.client.lang.FormatSplitter.Component;
import java.util.List;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * From <a href="https://github.com/mekanism/Mekanism/blob/1.21.x/src/datagen/main/java/mekanism/client/lang/ConvertibleLanguageProvider.java">Mekanism</a>
 */
public abstract class ConvertibleLanguageProvider extends LanguageProvider {

    public ConvertibleLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    public abstract void convert(String key, String raw, List<Component> splitEnglish);

    @Override
    protected void addTranslations() {
    }
}
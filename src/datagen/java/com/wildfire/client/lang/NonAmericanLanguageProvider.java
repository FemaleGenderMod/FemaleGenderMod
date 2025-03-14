package com.wildfire.client.lang;

import com.wildfire.client.lang.FormatSplitter.Component;
import com.wildfire.client.lang.FormatSplitter.FormatComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.Util;
import net.minecraft.data.PackOutput;

/**
 * From <a href="https://github.com/mekanism/Mekanism/blob/1.21.x/src/datagen/main/java/mekanism/client/lang/NonAmericanLanguageProvider.java">Mekanism</a>
 */
public class NonAmericanLanguageProvider extends ConvertibleLanguageProvider {

    private static final List<WordConversion> CONVERSIONS = Util.make(new HashMap<String, String>(), map -> {
        //TODO - 1.21: Define the proper entries for things we need to convert
        addEntry(map, "Armor", "Armour");
    }).entrySet().stream().map(entry -> new WordConversion(entry.getKey(), entry.getValue())).toList();

    private static void addEntry(Map<String, String> map, String key, String value) {
        map.put(key, value);
        map.put(key.toLowerCase(Locale.ROOT), value.toLowerCase(Locale.ROOT));
    }

    public NonAmericanLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    public void convert(String key, String raw, List<Component> splitEnglish) {
        StringBuilder builder = new StringBuilder();
        boolean foundMatch = false;
        for (Component component : splitEnglish) {
            if (component instanceof FormatComponent) {
                builder.append(component.contents());
            } else {
                String contents = component.contents();
                List<WordConversion> matched = new ArrayList<>();
                for (WordConversion conversion : CONVERSIONS) {
                    if (conversion.match(contents).find()) {
                        matched.add(conversion);
                    }
                }
                if (!matched.isEmpty()) {
                    foundMatch = true;
                    for (WordConversion conversion : matched) {
                        contents = conversion.replace(contents);
                    }
                }
                builder.append(contents);
            }
        }
        if (foundMatch) {
            add(key, builder.toString());
        }
    }

    private record WordConversion(Pattern matcher, String replacement) {

        private WordConversion(String toReplace, String replacement) {
            this(Pattern.compile("\\b" + toReplace + "\\b"), replacement);
        }

        public Matcher match(String contents) {
            return matcher.matcher(contents);
        }

        public String replace(String contents) {
            return match(contents).replaceAll(replacement);
        }
    }
}

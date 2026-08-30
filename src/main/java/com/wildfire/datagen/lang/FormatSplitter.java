package com.wildfire.datagen.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// A reduced version of a [class from Mekanism](https://github.com/mekanism/Mekanism/blob/26.2/src/datagen/main/java/mekanism/client/lang/FormatSplitter.java)
public class FormatSplitter {

    //Pattern from Formatter: %[argument_index$][flags][width][.precision][t]conversion
    // Note: This probably supports more formats than MC's formatter does, except things like local translation for I18n seems to
    // go through String.format which would end up using this. So I believe these are technically supported
    // The string pattern from the Formatter is: "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])"
    // we modify it to remove the trailing % as MC declares things like %% as invalid
    private static final Pattern fsPattern = Pattern.compile("%(\\d+\\$)?([-#+ 0,(<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z])");

    public static List<Component> split(String text) {
        Matcher matcher = fsPattern.matcher(text);
        List<Component> components = new ArrayList<>();
        int start = 0;
        while (matcher.find()) {
            int curStart = matcher.start();
            if (curStart > start) {
                //There is a gap, so we need to grab the piece in between
                components.add(new TextComponent(text.substring(start, curStart)));
            }
            String piece = matcher.group();
            components.add(new FormatComponent(piece));
            start = matcher.end();
        }
        if (start < text.length()) {
            components.add(new TextComponent(text.substring(start)));
        }
        return List.copyOf(components);
    }

    public interface Component {

        String contents();
    }

    private record TextComponent(String contents) implements Component {
    }

    public static class FormatComponent implements Component {

        private final String formattingCode;

        private FormatComponent(String formattingCode) {
            this.formattingCode = formattingCode;
        }

        @Override
        public String contents() {
            return formattingCode;
        }
    }
}

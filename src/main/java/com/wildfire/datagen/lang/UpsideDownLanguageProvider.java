package com.wildfire.datagen.lang;

import java.util.List;

/// [From Mekanism](https://github.com/mekanism/Mekanism/blob/26.2/src/datagen/main/java/mekanism/client/lang/UpsideDownLanguageProvider.java)
public class UpsideDownLanguageProvider extends ConvertibleLanguageProvider {

    public UpsideDownLanguageProvider() {
        super("en_ud");
        //Note: This technically is supposed to be upside down british english, but we are doing it as upside down US english
    }

    @Override
    public void convert(String key, String raw, List<FormatSplitter.Component> splitEnglish) {
        String upsideDown = convertComponents(splitEnglish);
        if (!raw.equals(upsideDown)) {
            add(key, upsideDown);
        }
    }

    private static final String normal = "abcdefghijklmnopqrstuvwxyz" +
                                         "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                                         "0123456789" +
                                         ",.?!;\"'`&_^()[]{}<>≤≥";
    private static final char[] upside_down = ("ɐqɔpǝɟᵷɥᴉɾʞꞁɯuodbɹsʇnʌʍxʎz" +
                                               "ⱯᗺƆᗡƎℲ⅁HIՐꞰꞀWNOԀꝹᴚS⟘∩ΛMX⅄Z" +
                                               "0⥝ᘔƐ߈ϛ9ㄥ86" +
                                               "'˙¿¡؛„,,⅋‾v)(][}{><⪖⪕").toCharArray();

    private static char flip(char c) {
        int index = normal.indexOf(c);
        return index == -1 ? c : upside_down[index];
    }

    private static String convertFormattingComponent(FormatSplitter.FormatComponent component, int curIndex, int numArguments) {
        String formattingCode = component.contents();
        //Convert a % styled formatting code
        String ending;
        int storedIndex = curIndex;
        //A formatting code can have at most one $ and if it has one then it is the first "argument" after the %
        String[] split = formattingCode.split("\\$");
        if (split.length == 2) {
            //It already has an index, so read that as the stored index
            ending = split[1];
            storedIndex = Integer.parseInt(split[0].substring(1));
        } else {
            //No index stored in the formatting code
            ending = formattingCode.substring(1);
        }
        //Compare the index the argument currently has with the index it will have afterwards
        // If they are the same we don't need to include the index argument
        if (storedIndex == numArguments - curIndex + 1) {
            return "%" + ending;
        }
        return "%" + storedIndex + "$" + ending;
    }

    private static String convertComponents(List<FormatSplitter.Component> splitText) {
        int numArguments = (int) splitText.stream().filter(component -> component instanceof FormatSplitter.FormatComponent).count();
        StringBuilder converted = new StringBuilder();
        int curIndex = numArguments;
        for (int i = splitText.size() - 1; i >= 0; i--) {
            FormatSplitter.Component component = splitText.get(i);
            if (component instanceof FormatSplitter.FormatComponent formatComponent) {
                //Insert the full code directly
                converted.append(convertFormattingComponent(formatComponent, curIndex--, numArguments));
            } else {
                //Convert each character to being upside down and then insert at end
                char[] toConvertArr = component.contents().toCharArray();
                for (int j = toConvertArr.length - 1; j >= 0; j--) {
                    converted.append(flip(toConvertArr[j]));
                }
            }
        }
        return new String(converted);
    }
}

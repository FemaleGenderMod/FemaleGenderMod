package com.wildfire.main.text;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

/**
 * Helper interface for creating formatted translations in our lang enums
 * From <a href="https://github.com/mekanism/Mekanism/blob/1.21.x/src/api/java/mekanism/api/text/ILangEntry.java">Mekanism</a>
 */
@MethodsReturnNonnullByDefault
public interface ILangEntry extends IHasTranslationKey {

    /**
     * Translates this {@link ILangEntry} using a "smart" replacement scheme to allow for automatic replacements, and coloring to take place.
     */
    default MutableComponent translate(Object... args) {
        return TextComponentUtil.smartTranslate(getTranslationKey(), args);
    }

    /**
     * Translates this {@link ILangEntry} using a "smart" replacement scheme to allow for automatic replacements, and coloring to take place.
     */
    default MutableComponent translate() {
        return TextComponentUtil.translate(getTranslationKey());
    }

    /**
     * Translates this {@link ILangEntry} and applies the {@link ChatFormatting color} to the {@link Component}.
     */
    default MutableComponent translateColored(ChatFormatting color, Object... args) {
        return translate(args).withStyle(color);
    }

    /**
     * Translates this {@link ILangEntry} and applies the {@link ChatFormatting color} to the {@link Component}.
     */
    default MutableComponent translateColored(ChatFormatting color) {
        return translate().withStyle(color);
    }

    /**
     * Translates this {@link ILangEntry} and applies the {@link TextColor} to the {@link Component}.
     *
     * @since 10.4.0
     */
    default MutableComponent translateColored(TextColor color, Object... args) {
        return TextComponentUtil.build(color, translate(args));
    }

    /**
     * Translates this {@link ILangEntry} and applies the {@link TextColor} to the {@link Component}.
     *
     * @since 10.4.0
     */
    default MutableComponent translateColored(TextColor color) {
        return TextComponentUtil.build(color, translate());
    }
}
package com.wildfire.main.text;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.jetbrains.annotations.NotNull;

/**
 * From <a href="https://github.com/mekanism/Mekanism/blob/1.21.x/src/api/java/mekanism/api/text/IHasTranslationKey.java">Mekanism</a>
 */
@MethodsReturnNonnullByDefault
public interface IHasTranslationKey {

    /**
     * Gets the translation key for this object.
     */
    String getTranslationKey();

    /**
     * Helper interface that also implements Neo's TranslatableEnum interface
     */
    interface IHasEnumNameTranslationKey extends IHasTranslationKey, TranslatableEnum {

        @NotNull
        @Override
        default Component getTranslatedName() {
            return TextComponentUtil.translate(getTranslationKey());
        }
    }
}
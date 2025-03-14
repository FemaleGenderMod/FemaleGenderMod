package com.wildfire.main.text;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.jetbrains.annotations.NotNull;

/**
 * From <a href="https://github.com/mekanism/Mekanism/blob/1.21.x/src/api/java/mekanism/api/text/IHasTextComponent.java">Mekanism</a>
 */
@MethodsReturnNonnullByDefault
public interface IHasTextComponent {

    /**
     * Gets the text component that represents this object.
     */
    Component getTextComponent();

    /**
     * Helper interface that also implements Neo's TranslatableEnum interface
     */
    interface IHasEnumNameTextComponent extends IHasTextComponent, TranslatableEnum {

        @NotNull
        @Override
        default Component getTranslatedName() {
            return getTextComponent();
        }
    }
}
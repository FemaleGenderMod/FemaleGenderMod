package com.wildfire.main;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public enum Gender {
    // Definición de los géneros con sus traducciones, colores y sonidos
    FEMALE(
            Component.translatable("wildfire_gender.label.female").withStyle(ChatFormatting.LIGHT_PURPLE),
            true,
            WildfireSounds.FEMALE_HURT
    ),
    MALE(
            Component.translatable("wildfire_gender.label.male").withStyle(ChatFormatting.AQUA),
            false,
            null
    ),
    OTHER(
            Component.translatable("wildfire_gender.label.other").withStyle(ChatFormatting.GREEN),
            true,
            WildfireSounds.FEMALE_HURT
    );

    private final Component name;
    private final boolean canHaveBreasts;
    // Usamos Supplier o RegistryObject para evitar problemas de carga de sonidos
    private final @Nullable Supplier<SoundEvent> hurtSoundSupplier;

    private Gender(Component name, boolean canHaveBreasts, @Nullable Supplier<SoundEvent> hurtSoundSupplier) {
        this.name = name;
        this.canHaveBreasts = canHaveBreasts;
        this.hurtSoundSupplier = hurtSoundSupplier;
    }

    public Component getDisplayName() {
        return this.name;
    }

    @Nullable
    public SoundEvent getHurtSound() {
        // Obtenemos el sonido del RegistryObject si existe
        return this.hurtSoundSupplier != null ? this.hurtSoundSupplier.get() : null;
    }

    public boolean canHaveBreasts() {
        return this.canHaveBreasts;
    }
}
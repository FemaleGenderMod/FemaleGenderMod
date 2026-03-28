package com.wildfire.main;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class WildfireSounds {
    // 1. En NeoForge usamos Registries.SOUND_EVENT para el DeferredRegister
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, WildfireGender.MODID);

    // 2. Guardamos el sonido como un Supplier<SoundEvent> (internamente es un DeferredHolder)
    public static final Supplier<SoundEvent> FEMALE_HURT = SOUNDS.register("female_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(WildfireGender.MODID, "female_hurt")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
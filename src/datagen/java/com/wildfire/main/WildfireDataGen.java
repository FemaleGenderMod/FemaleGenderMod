package com.wildfire.main;

import com.wildfire.client.WildfireGenderArmorProvider;
import com.wildfire.client.WildfireSoundProvider;
import com.wildfire.client.lang.WildfireLangProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = WildfireGender.MODID, bus = EventBusSubscriber.Bus.MOD)
public class WildfireDataGen {

    private WildfireDataGen() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        gen.addProvider(true, new BasePackMetadataGenerator(output, WildfireLang.PACK_DESCRIPTION));
        gen.addProvider(event.includeClient(), new WildfireLangProvider(output));
        gen.addProvider(event.includeClient(), new WildfireSoundProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new WildfireGenderArmorProvider(output, event.getLookupProvider()));
    }
}
package com.wildfire.data;

import com.wildfire.main.WildfireGender;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = WildfireGender.MODID, bus = EventBusSubscriber.Bus.MOD)
public class WildfireDataGen {

    private WildfireDataGen() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {

    }
}
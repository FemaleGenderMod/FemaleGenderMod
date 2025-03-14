package com.wildfire.client;

import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireLang;
import com.wildfire.main.WildfireSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class WildfireSoundProvider extends SoundDefinitionsProvider {

    public WildfireSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, WildfireGender.MODID, helper);
    }

    @Override
    public void registerSounds() {
        add(WildfireSounds.FEMALE_HURT, definition()
              .subtitle(WildfireLang.HURT_SOUND_SUBTITLE.getTranslationKey())
              .with(
                    sound(WildfireGender.rl("female_damage")),
                    sound(WildfireGender.rl("female_damage2"))
              )
        );
    }
}
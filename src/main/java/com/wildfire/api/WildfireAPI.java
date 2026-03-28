package com.wildfire.api;

import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.wildfire.main.Gender;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireGenderClient;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.entitydata.PlayerConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;

public class WildfireAPI {
    private static final Map<Item, IGenderArmor> GENDER_ARMORS = new HashMap<>();

    public WildfireAPI() {}

    public static void addGenderArmor(Item item, IGenderArmor genderArmor) {
        GENDER_ARMORS.put(item, genderArmor);
    }

    public static @NotNull Gender getPlayerGender(UUID uuid) {
        PlayerConfig cfg = WildfireGender.getPlayerById(uuid);
        return cfg == null ? (Gender) Configuration.GENDER.getDefault() : cfg.getGender();
    }

    @OnlyIn(Dist.CLIENT)
    public static Future<Optional<PlayerConfig>> loadGenderInfo(UUID uuid, boolean markForSync) {
        return WildfireGenderClient.loadGenderInfo(uuid, markForSync);
    }

    public static Map<Item, IGenderArmor>  getGenderArmors() {
        return GENDER_ARMORS;
    }
}

package com.wildfire.main;

import com.wildfire.api.IGenderArmor;
import com.wildfire.api.WildfireAPI;
import com.wildfire.main.config.FloatConfigKey;
import com.wildfire.render.armor.EmptyGenderArmor;
import com.wildfire.render.armor.SimpleGenderArmor;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public final class WildfireHelper {

    private WildfireHelper() {
        throw new UnsupportedOperationException();
    }

    public static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static float randFloat(float min, float max) {
        return (float) ThreadLocalRandom.current().nextDouble(min, max + 1.0F);
    }

    /**
     * Determina la configuración de género de una armadura específica.
     */
    public static IGenderArmor getArmorConfig(ItemStack stack) {
        if (stack.isEmpty()) {
            return EmptyGenderArmor.INSTANCE;
        }

        Item item = stack.getItem();

        // Primero revisamos si hay algo registrado en la API (para compatibilidad con otros mods)
        IGenderArmor apiConfig = WildfireAPI.getGenderArmors().get(item);
        if (apiConfig != null) {
            return apiConfig;
        }

        // Si es una armadura de pecho (Chestplate) de Vanilla, asignamos el modelo predefinido
        if (item instanceof ArmorItem armorItem) {
            if (armorItem.getEquipmentSlot() == EquipmentSlot.CHEST) {
                Holder<ArmorMaterial> material = armorItem.getMaterial();

                if (material.is(ArmorMaterials.LEATHER)) return SimpleGenderArmor.LEATHER;
                if (material.is(ArmorMaterials.CHAIN)) return SimpleGenderArmor.CHAIN_MAIL;
                if (material.is(ArmorMaterials.GOLD)) return SimpleGenderArmor.GOLD;
                if (material.is(ArmorMaterials.IRON)) return SimpleGenderArmor.IRON;
                if (material.is(ArmorMaterials.DIAMOND)) return SimpleGenderArmor.DIAMOND;
                if (material.is(ArmorMaterials.NETHERITE)) return SimpleGenderArmor.NETHERITE;

                return SimpleGenderArmor.FALLBACK;
            }
        }

        return EmptyGenderArmor.INSTANCE;
    }

    /**
     * Utilidad para leer NBT de forma segura devolviendo un Optional.
     */
    public static <T> Optional<T> readNbt(CompoundTag compound, String key, Function<String, T> reader) {
        // En Mojang: contains() reemplaza a method_10545()
        return !compound.contains(key) ? Optional.empty() : Optional.of(reader.apply(key));
    }

    /**
     * Lee un valor flotante del NBT y lo limita (clamp) según las reglas de configuración.
     */
    public static Optional<Float> readNbt(CompoundTag compound, String key, FloatConfigKey configKey) {
        Objects.requireNonNull(compound);
        // compound::getFloat reemplaza a compound::method_10583
        return readNbt(compound, key, compound::getFloat)
                .map((v) -> Mth.clamp(v, configKey.getMinInclusive(), configKey.getMaxInclusive()));
    }
}
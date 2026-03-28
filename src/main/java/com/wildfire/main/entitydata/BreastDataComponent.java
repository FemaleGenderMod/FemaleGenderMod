package com.wildfire.main.entitydata;

import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.Configuration;
import com.wildfire.main.config.FloatConfigKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Optional;

public record BreastDataComponent(float breastSize, float cleavage, Vector3f offsets, boolean jacket, @Nullable CustomData nbtComponent) {

    public static @Nullable BreastDataComponent fromPlayer(@NotNull Player player, @NotNull PlayerConfig config) {
        // Verificamos si el género permite pechos y si la configuración permite mostrarlos en armadura
        if (config.getGender().canHaveBreasts() && config.showBreastsInArmor()) {
            return new BreastDataComponent(
                    config.getBustSize(),
                    config.getBreasts().getCleavage(),
                    config.getBreasts().getOffsets(),
                    player.isModelPartShown(PlayerModelPart.JACKET), // method_7348 y field_7564
                    null
            );
        }
        return null;
    }

    public static @Nullable BreastDataComponent fromComponent(@Nullable CustomData component) {
        if (component == null) {
            return null;
        } else {
            // Obtenemos el NBT del componente de datos personalizados
            CompoundTag root = component.copyTag(); // method_57463

            // Verificamos si contiene la llave del mod (10 es el ID de TAG_COMPOUND)
            if (!root.contains("WildfireGender", 10)) {
                return null;
            } else {
                CompoundTag nbt = root.getCompound("WildfireGender");

                // Leemos los valores usando WildfireHelper (asegúrate de portar esa clase también)
                float breastSize = WildfireHelper.readNbt(nbt, "BreastSize", Configuration.BUST_SIZE).orElse(0.0F);

                FloatConfigKey cleavageKey = Configuration.BREASTS_CLEAVAGE;
                float cleavage = WildfireHelper.readNbt(nbt, "Cleavage", cleavageKey).orElse(cleavageKey.getDefault());

                // Leemos si la chaqueta (jacket) está activa, por defecto true
                boolean jacket = WildfireHelper.readNbt(nbt, "Jacket", nbt::getBoolean).orElse(true);

                Vector3f offsets = new Vector3f(
                        WildfireHelper.readNbt(nbt, "XOffset", Configuration.BREASTS_OFFSET_X).orElse(0.0F),
                        WildfireHelper.readNbt(nbt, "YOffset", Configuration.BREASTS_OFFSET_Y).orElse(0.0F),
                        WildfireHelper.readNbt(nbt, "ZOffset", Configuration.BREASTS_OFFSET_Z).orElse(0.0F)
                );

                return new BreastDataComponent(breastSize, cleavage, offsets, jacket, component);
            }
        }
    }

    public void write(ItemStack stack) {
        if (stack.isEmpty()) { // method_7960
            throw new IllegalArgumentException("The provided ItemStack must not be empty");
        } else {
            // Creamos un nuevo tag con la información de los pechos
            CompoundTag nbt = new CompoundTag();
            nbt.putFloat("BreastSize", this.breastSize);
            nbt.putFloat("Cleavage", this.cleavage);
            nbt.putFloat("XOffset", this.offsets.x);
            nbt.putFloat("YOffset", this.offsets.y);
            nbt.putFloat("ZOffset", this.offsets.z);
            nbt.putBoolean("Jacket", this.jacket);

            // En 1.21.1 usamos el sistema de DataComponents para guardar el NBT en el objeto
            CustomData.update(DataComponents.CUSTOM_DATA, stack, (tag) -> {
                tag.put("WildfireGender", nbt);
            });
        }
    }
}
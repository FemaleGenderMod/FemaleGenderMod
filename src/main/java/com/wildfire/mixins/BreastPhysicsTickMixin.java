package com.wildfire.mixins;

import com.wildfire.main.entitydata.EntityConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin({ArmorStand.class, Player.class})
public abstract class BreastPhysicsTickMixin {

    @Inject(
            at = @At("TAIL"),
            method = "tick"
    )
    public void wildfiregender$tickBreastPhysics(CallbackInfo info) {
        // En un Mixin, 'this' es la instancia de la clase objetivo (ArmorStand o Player)
        // Como ambas heredan de LivingEntity, podemos castear con seguridad.
        LivingEntity entity = (LivingEntity) (Object) this;

        // Solo procesamos físicas en el lado del cliente
        if (entity.level().isClientSide) {
            EntityConfig cfg = EntityConfig.getEntity(entity);

            if (cfg != null) {
                // Si es un Armor Stand, leemos la pechera para actualizar el tamaño/género visual
                if (entity instanceof ArmorStand) {
                    cfg.readFromStack(entity.getItemBySlot(EquipmentSlot.CHEST));
                }

                // Ejecutamos el cálculo de físicas (rebote, inercia, etc.)
                cfg.tickBreastPhysics(entity);
            }
        }
    }
}
package com.wildfire.mixins;

import com.wildfire.main.WildfireGender;
import com.wildfire.main.entitydata.PlayerConfig;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * Inyectamos en getHurtSound. Si devolvemos un sonido personalizado,
     * el juego base se encargará automáticamente de reproducirlo con su propio pitch y volumen,
     * evitando que suenen dos voces al mismo tiempo.
     */
    @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
    protected void wildfiregender$overrideHurtSound(DamageSource damageSource, CallbackInfoReturnable<SoundEvent> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Solo actuamos si la entidad es un jugador y estamos en el lado del cliente
        if (entity instanceof Player player && player.level().isClientSide) {

            PlayerConfig genderPlayer = WildfireGender.getPlayerById(player.getUUID());

            // Si hay configuración y los sonidos de daño están activados
            if (genderPlayer != null && genderPlayer.hasHurtSounds()) {
                SoundEvent hurtSound = genderPlayer.getGender().getHurtSound();

                if (hurtSound != null) {
                    // Reemplazamos el sonido de daño original por el nuestro
                    cir.setReturnValue(hurtSound);
                }
            }
        }
    }
}
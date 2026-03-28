package com.wildfire.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.wildfire.api.IGenderArmor;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.entitydata.BreastDataComponent;
import com.wildfire.main.entitydata.PlayerConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ArmorStand.class)
public abstract class ArmorStandEntityMixin extends LivingEntity {

    protected ArmorStandEntityMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Elimina los datos del mod de un ItemStack usando el sistema de DataComponents de la 1.21.
     */
    @Unique
    private void wildfiregender$removeBreastDataFromStack(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains("WildfireGender")) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, (tag) -> {
                tag.remove("WildfireGender");
            });
        }
    }

    /**
     * Inyecta los datos del jugador en la armadura cuando se equipa en el Armor Stand.
     */
    @ModifyArg(
            method = "swapItem", // En 1.21.1 el método sigue llamándose equip o se mapea a setItemSlot
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/decoration/ArmorStand;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"
            ),
            index = 1
    )
    public ItemStack wildfiregender$attachBreastData(ItemStack stack, @Local(argsOnly = true) EquipmentSlot slot, @Local(argsOnly = true) Player player) {
        // Solo actuamos en el lado del servidor para persistir los datos y si es el slot del pecho
        if (player != null && !player.level().isClientSide && slot == EquipmentSlot.CHEST) {
            PlayerConfig playerConfig = WildfireGender.getPlayerById(player.getUUID());

            if (playerConfig == null) {
                this.wildfiregender$removeBreastDataFromStack(stack);
                return stack;
            } else {
                IGenderArmor armorConfig = WildfireHelper.getArmorConfig(stack);
                // Si la armadura permite copiar ajustes a los Armor Stands
                if (armorConfig.armorStandsCopySettings()) {
                    BreastDataComponent component = BreastDataComponent.fromPlayer(player, playerConfig);
                    if (component != null) {
                        component.write(stack);
                    }
                }
                return stack;
            }
        }
        return stack;
    }

    /**
     * Limpia los datos cuando el jugador recupera la armadura (intercambio).
     */
    @ModifyArg(
            method = "swapItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"
            ),
            index = 1
    )
    public ItemStack wildfiregender$removeBreastDataOnReplace(ItemStack stack, @Local(argsOnly = true) Player player) {
        if (!player.level().isClientSide) {
            this.wildfiregender$removeBreastDataFromStack(stack);
        }
        return stack;
    }

    /**
     * Limpia los datos cuando el Armor Stand se rompe y suelta los ítems.
     */
    @ModifyArg(
            method = "brokenByAnything",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"
            ),
            index = 2
    )
    public ItemStack wildfiregender$removeBreastDataOnBreak(ItemStack stack) {
        if (!this.level().isClientSide) {
            this.wildfiregender$removeBreastDataFromStack(stack);
        }
        return stack;
    }
}
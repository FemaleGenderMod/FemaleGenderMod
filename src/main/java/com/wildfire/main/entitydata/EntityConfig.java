package com.wildfire.main.entitydata;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.Gender;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.Configuration;
import com.wildfire.physics.BreastPhysics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityConfig {
    public static final Map<UUID, EntityConfig> ENTITY_CACHE = new ConcurrentHashMap<>();

    public final UUID uuid;
    protected Gender gender;
    protected float pBustSize;
    protected boolean breastPhysics;
    protected float bounceMultiplier;
    protected float floppyMultiplier;
    protected final BreastPhysics lBreastPhysics;
    protected final BreastPhysics rBreastPhysics;
    protected final Breasts breasts;
    protected boolean jacketLayer;
    protected @Nullable BreastDataComponent fromComponent;

    protected EntityConfig(UUID uuid) {
        // Cargamos los valores por defecto desde la configuración
        this.gender = (Gender) Configuration.GENDER.getDefault();
        this.pBustSize = (Float) Configuration.BUST_SIZE.getDefault();
        this.breastPhysics = (Boolean) Configuration.BREAST_PHYSICS.getDefault();
        this.bounceMultiplier = (Float) Configuration.BOUNCE_MULTIPLIER.getDefault();
        this.floppyMultiplier = (Float) Configuration.FLOPPY_MULTIPLIER.getDefault();
        this.jacketLayer = true;
        this.uuid = uuid;
        this.breasts = new Breasts();
        this.lBreastPhysics = new BreastPhysics(this);
        this.rBreastPhysics = new BreastPhysics(this);
    }

    public void readFromStack(@NotNull ItemStack chestplate) {
        // En 1.21.1 extraemos el CustomData del componente CUSTOM_DATA
        CustomData component = chestplate.get(DataComponents.CUSTOM_DATA);

        if (!chestplate.isEmpty() && component != null) {
            // Verificamos si los datos han cambiado para no recalcular innecesariamente
            if (this.fromComponent == null || !Objects.equals(component, this.fromComponent.nbtComponent())) {
                this.fromComponent = BreastDataComponent.fromComponent(component);

                if (this.fromComponent == null) {
                    this.gender = Gender.MALE;
                } else {
                    // Si el ítem tiene datos, los aplicamos a la entidad (ej. Armor Stand)
                    this.breastPhysics = false; // Las armaduras estáticas no suelen tener físicas activas
                    this.pBustSize = this.fromComponent.breastSize();
                    this.gender = this.pBustSize >= 0.02F ? Gender.FEMALE : Gender.MALE;
                    this.breasts.updateCleavage(this.fromComponent.cleavage());
                    this.breasts.updateOffsets(this.fromComponent.offsets());
                    this.jacketLayer = this.fromComponent.jacket();
                }
            }
        } else {
            this.fromComponent = null;
            this.gender = Gender.MALE;
        }
    }

    public static @Nullable EntityConfig getEntity(@NotNull LivingEntity entity) {
        // Si es un jugador, usamos el caché de jugadores, si no, el caché de entidades
        if (entity instanceof Player player) {
            return WildfireGender.getPlayerById(player.getUUID());
        }
        return ENTITY_CACHE.computeIfAbsent(entity.getUUID(), EntityConfig::new);
    }

    // --- Getters ---
    public @NotNull Gender getGender() { return this.gender; }
    public @NotNull Breasts getBreasts() { return this.breasts; }
    public float getBustSize() { return this.pBustSize; }
    public boolean hasBreastPhysics() { return this.breastPhysics; }
    public boolean getArmorPhysicsOverride() { return false; }
    public boolean showBreastsInArmor() { return true; }
    public float getBounceMultiplier() { return this.bounceMultiplier; }
    public float getFloppiness() { return this.floppyMultiplier; }
    public @NotNull BreastPhysics getLeftBreastPhysics() { return this.lBreastPhysics; }
    public @NotNull BreastPhysics getRightBreastPhysics() { return this.rBreastPhysics; }
    public boolean hasJacketLayer() { return this.jacketLayer; }

    @OnlyIn(Dist.CLIENT)
    public void tickBreastPhysics(@NotNull LivingEntity entity) {
        // Obtenemos la armadura en el slot del pecho
        ItemStack chestItem = entity.getItemBySlot(EquipmentSlot.CHEST);
        IGenderArmor armor = WildfireHelper.getArmorConfig(chestItem);

        // Actualizamos las físicas de cada lado
        this.getLeftBreastPhysics().update(entity, armor);
        this.getRightBreastPhysics().update(entity, armor);
    }

    @Override
    public String toString() {
        return "%s(uuid=%s, gender=%s)".formatted(this.getClass().getCanonicalName(), this.uuid, this.gender);
    }
}
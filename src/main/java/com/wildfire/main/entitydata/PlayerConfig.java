package com.wildfire.main.entitydata;

import com.google.gson.JsonObject;
import com.wildfire.main.Gender;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.ConfigKey;
import com.wildfire.main.config.Configuration;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerConfig extends EntityConfig {
    public boolean needsSync;
    public SyncStatus syncStatus;
    private final Configuration cfg;
    protected boolean hurtSounds;
    protected boolean armorPhysOverride;
    protected boolean showBreastsInArmor;

    /** @deprecated Usar el constructor de un solo parámetro siempre que sea posible */
    @Deprecated
    public PlayerConfig(UUID uuid, Gender gender) {
        this(uuid);
        this.updateGender(gender);
    }

    public PlayerConfig(UUID uuid) {
        super(uuid);
        this.syncStatus = SyncStatus.UNKNOWN;

        // Inicializamos con los valores por defecto de la configuración global
        this.hurtSounds = (Boolean) Configuration.HURT_SOUNDS.getDefault();
        this.armorPhysOverride = (Boolean) Configuration.ARMOR_PHYSICS_OVERRIDE.getDefault();
        this.showBreastsInArmor = (Boolean) Configuration.SHOW_IN_ARMOR.getDefault();

        // Creamos un archivo de configuración individual basado en el UUID del jugador
        this.cfg = new Configuration(this.uuid.toString());
        this.cfg.set(Configuration.USERNAME, this.uuid);

        // Establecemos los valores por defecto en el archivo si no existen
        this.cfg.setDefault(Configuration.GENDER);
        this.cfg.setDefault(Configuration.BUST_SIZE);
        this.cfg.setDefault(Configuration.HURT_SOUNDS);
        this.cfg.setDefault(Configuration.BREASTS_OFFSET_X);
        this.cfg.setDefault(Configuration.BREASTS_OFFSET_Y);
        this.cfg.setDefault(Configuration.BREASTS_OFFSET_Z);
        this.cfg.setDefault(Configuration.BREASTS_UNIBOOB);
        this.cfg.setDefault(Configuration.BREASTS_CLEAVAGE);
        this.cfg.setDefault(Configuration.BREAST_PHYSICS);
        this.cfg.setDefault(Configuration.ARMOR_PHYSICS_OVERRIDE);
        this.cfg.setDefault(Configuration.SHOW_IN_ARMOR);
        this.cfg.setDefault(Configuration.BOUNCE_MULTIPLIER);
        this.cfg.setDefault(Configuration.FLOPPY_MULTIPLIER);
    }

    @Override
    public void readFromStack(@NotNull ItemStack chestplate) {
        // Los jugadores cargan su config desde archivo o red, no desde el ítem directamente
    }

    public Configuration getConfig() {
        return this.cfg;
    }

    /**
     * Helper para actualizar valores validando contra la configuración
     */
    private <VALUE> boolean updateValue(ConfigKey<VALUE> key, VALUE value, Consumer<VALUE> setter) {
        if (key.validate(value)) {
            setter.accept(value);
            return true;
        }
        return false;
    }

    public boolean updateGender(Gender value) {
        return this.updateValue(Configuration.GENDER, value, (v) -> this.gender = v);
    }

    public boolean updateBustSize(float value) {
        return this.updateValue(Configuration.BUST_SIZE, value, (v) -> this.pBustSize = v);
    }

    public boolean hasHurtSounds() {
        return this.hurtSounds;
    }

    public boolean updateHurtSounds(boolean value) {
        return this.updateValue(Configuration.HURT_SOUNDS, value, (v) -> this.hurtSounds = v);
    }

    public boolean updateBreastPhysics(boolean value) {
        return this.updateValue(Configuration.BREAST_PHYSICS, value, (v) -> this.breastPhysics = v);
    }

    @Override
    public boolean getArmorPhysicsOverride() {
        return this.armorPhysOverride;
    }

    public boolean updateArmorPhysicsOverride(boolean value) {
        return this.updateValue(Configuration.ARMOR_PHYSICS_OVERRIDE, value, (v) -> this.armorPhysOverride = v);
    }

    @Override
    public boolean showBreastsInArmor() {
        return this.showBreastsInArmor;
    }

    public boolean updateShowBreastsInArmor(boolean value) {
        return this.updateValue(Configuration.SHOW_IN_ARMOR, value, (v) -> this.showBreastsInArmor = v);
    }

    public boolean updateBounceMultiplier(float value) {
        return this.updateValue(Configuration.BOUNCE_MULTIPLIER, value, (v) -> this.bounceMultiplier = v);
    }

    public boolean updateFloppiness(float value) {
        return this.updateValue(Configuration.FLOPPY_MULTIPLIER, value, (v) -> this.floppyMultiplier = v);
    }

    public SyncStatus getSyncStatus() {
        return this.syncStatus;
    }

    /**
     * Convierte la configuración del jugador a JSON para el sistema de red
     */
    public static JsonObject toJsonObject(PlayerConfig plr) {
        JsonObject obj = new JsonObject();
        Configuration.USERNAME.save(obj, plr.uuid);
        Configuration.GENDER.save(obj, plr.getGender());
        Configuration.BUST_SIZE.save(obj, plr.getBustSize());
        Configuration.HURT_SOUNDS.save(obj, plr.hasHurtSounds());
        Configuration.BREAST_PHYSICS.save(obj, plr.hasBreastPhysics());
        Configuration.SHOW_IN_ARMOR.save(obj, plr.showBreastsInArmor());
        Configuration.ARMOR_PHYSICS_OVERRIDE.save(obj, plr.getArmorPhysicsOverride());
        Configuration.BOUNCE_MULTIPLIER.save(obj, plr.getBounceMultiplier());
        Configuration.FLOPPY_MULTIPLIER.save(obj, plr.getFloppiness());

        Breasts breasts = plr.getBreasts();
        Configuration.BREASTS_OFFSET_X.save(obj, breasts.getXOffset());
        Configuration.BREASTS_OFFSET_Y.save(obj, breasts.getYOffset());
        Configuration.BREASTS_OFFSET_Z.save(obj, breasts.getZOffset());
        Configuration.BREASTS_UNIBOOB.save(obj, breasts.isUniboob());
        Configuration.BREASTS_CLEAVAGE.save(obj, breasts.getCleavage());

        return obj;
    }

    /**
     * Carga los datos guardados en disco para un jugador específico
     */
    public static PlayerConfig loadCachedPlayer(UUID uuid, boolean markForSync) {
        PlayerConfig plr = WildfireGender.getPlayerById(uuid);
        if (plr != null) {
            plr.syncStatus = SyncStatus.CACHED;
            Configuration config = plr.getConfig();
            config.load();

            // Aplicamos los valores cargados
            plr.updateGender((Gender) config.get(Configuration.GENDER));
            plr.updateBustSize((Float) config.get(Configuration.BUST_SIZE));
            plr.updateHurtSounds((Boolean) config.get(Configuration.HURT_SOUNDS));
            plr.updateBreastPhysics((Boolean) config.get(Configuration.BREAST_PHYSICS));
            plr.updateShowBreastsInArmor((Boolean) config.get(Configuration.SHOW_IN_ARMOR));
            plr.updateArmorPhysicsOverride((Boolean) config.get(Configuration.ARMOR_PHYSICS_OVERRIDE));
            plr.updateBounceMultiplier((Float) config.get(Configuration.BOUNCE_MULTIPLIER));
            plr.updateFloppiness((Float) config.get(Configuration.FLOPPY_MULTIPLIER));

            Breasts breasts = plr.getBreasts();
            breasts.updateXOffset((Float) config.get(Configuration.BREASTS_OFFSET_X));
            breasts.updateYOffset((Float) config.get(Configuration.BREASTS_OFFSET_Y));
            breasts.updateZOffset((Float) config.get(Configuration.BREASTS_OFFSET_Z));
            breasts.updateUniboob((Boolean) config.get(Configuration.BREASTS_UNIBOOB));
            breasts.updateCleavage((Float) config.get(Configuration.BREASTS_CLEAVAGE));

            if (markForSync) {
                plr.needsSync = true;
            }
            return plr;
        }
        return null;
    }

    /**
     * Guarda la configuración actual del jugador en su archivo JSON
     */
    public static void saveGenderInfo(PlayerConfig plr) {
        Configuration config = plr.getConfig();
        config.set(Configuration.USERNAME, plr.uuid);
        config.set(Configuration.GENDER, plr.getGender());
        config.set(Configuration.BUST_SIZE, plr.getBustSize());
        config.set(Configuration.HURT_SOUNDS, plr.hasHurtSounds());
        config.set(Configuration.BREAST_PHYSICS, plr.hasBreastPhysics());
        config.set(Configuration.SHOW_IN_ARMOR, plr.showBreastsInArmor());
        config.set(Configuration.ARMOR_PHYSICS_OVERRIDE, plr.getArmorPhysicsOverride());
        config.set(Configuration.BOUNCE_MULTIPLIER, plr.getBounceMultiplier());
        config.set(Configuration.FLOPPY_MULTIPLIER, plr.getFloppiness());

        Breasts breasts = plr.getBreasts();
        config.set(Configuration.BREASTS_OFFSET_X, breasts.getXOffset());
        config.set(Configuration.BREASTS_OFFSET_Y, breasts.getYOffset());
        config.set(Configuration.BREASTS_OFFSET_Z, breasts.getZOffset());
        config.set(Configuration.BREASTS_UNIBOOB, breasts.isUniboob());
        config.set(Configuration.BREASTS_CLEAVAGE, breasts.getCleavage());

        config.save();
        plr.needsSync = true;
    }

    @Override
    public boolean hasJacketLayer() {
        // En jugadores, Minecraft maneja esto de forma nativa.
        throw new UnsupportedOperationException("Usar isModelPartShown(PlayerModelPart.JACKET) en el objeto Player de Minecraft");
    }

    public enum SyncStatus {
        CACHED,
        SYNCED,
        UNKNOWN
    }
}
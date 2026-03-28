package com.wildfire.render.armor;

import com.wildfire.api.IGenderArmor;

/**
 * Implementación por defecto para cuando no hay armadura equipada.
 * Se usa el patrón Singleton (INSTANCE).
 */
public class EmptyGenderArmor implements IGenderArmor {
    public static final EmptyGenderArmor INSTANCE = new EmptyGenderArmor();

    private EmptyGenderArmor() {
        // Constructor privado para el patrón Singleton
    }

    @Override
    public boolean coversBreasts() {
        return false;
    }

    @Override
    public boolean armorStandsCopySettings() {
        return false;
    }

    /**
     * Devuelve 0.0 porque una "armadura vacía" no debería apretar el modelo.
     */
    @Override
    public float tightness() {
        return 0.0F;
    }

    /**
     * Devuelve 0.0 porque sin armadura no hay resistencia a las físicas de rebote.
     */
    @Override
    public float physicsResistance() {
        return 0.0F;
    }
}
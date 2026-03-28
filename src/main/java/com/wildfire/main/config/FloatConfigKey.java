package com.wildfire.main.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.Mth;

public class FloatConfigKey extends NumberConfigKey<Float> {

    public FloatConfigKey(String key, Float defaultValue) {
        super(key, defaultValue);
    }

    public FloatConfigKey(String key, float defaultValue, float minInclusive, float maxInclusive) {
        super(key, defaultValue, minInclusive, maxInclusive);
    }

    @Override
    protected Float read(JsonElement element) {
        // En Mojang Mappings: Mth.clamp reemplaza a class_3532.method_15363
        float value = super.read(element);
        return Mth.clamp(value, this.getMinInclusive(), this.getMaxInclusive());
    }

    @Override
    protected Float fromPrimitive(JsonPrimitive primitive) {
        return primitive.getAsFloat();
    }

    public float getMinInclusive() {
        return this.minInclusive == null ? -Float.MAX_VALUE : this.minInclusive;
    }

    public float getMaxInclusive() {
        return this.maxInclusive == null ? Float.MAX_VALUE : this.maxInclusive;
    }
}
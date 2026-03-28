package com.wildfire.main.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class BooleanConfigKey extends ConfigKey<Boolean> {
    public BooleanConfigKey(String key, boolean defaultValue) {
        super(key, defaultValue);
    }

    protected Boolean read(JsonElement element) {
        return element.isJsonPrimitive() ? element.getAsJsonPrimitive().getAsBoolean() : (Boolean)this.defaultValue;
    }

    public void save(JsonObject object, Boolean value) {
        object.addProperty(this.key, value);
    }
}

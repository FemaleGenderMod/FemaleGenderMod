package com.wildfire.main.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.UUID;

public class UUIDConfigKey extends ConfigKey<UUID> {
    public UUIDConfigKey(String key, UUID defaultValue) {
        super(key, defaultValue);
    }

    protected UUID read(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                try {
                    return UUID.fromString(primitive.getAsString());
                } catch (Exception var4) {
                }
            }
        }

        return (UUID)this.defaultValue;
    }

    public void save(JsonObject object, UUID value) {
        object.addProperty(this.key, value.toString());
    }
}

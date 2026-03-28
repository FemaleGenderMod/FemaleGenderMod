package com.wildfire.main.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class StringConfigKey extends ConfigKey<String> {
    public StringConfigKey(String key, String defaultValue) {
        super(key, defaultValue);
    }

    protected String read(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return primitive.getAsString();
            }
        }

        return (String)this.defaultValue;
    }

    public void save(JsonObject object, String value) {
        object.addProperty(this.key, value);
    }
}
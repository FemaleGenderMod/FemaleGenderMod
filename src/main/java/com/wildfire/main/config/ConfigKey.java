package com.wildfire.main.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class ConfigKey<TYPE> {
    protected final String key;
    protected final TYPE defaultValue;

    protected ConfigKey(String key, TYPE defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public TYPE getDefault() {
        return this.defaultValue;
    }

    public final TYPE read(JsonObject obj) {
        JsonElement element = obj.get(this.key);
        if (element != null) {
            TYPE value = (TYPE)this.read(element);
            if (this.validate(value)) {
                return value;
            }
        }

        return this.defaultValue;
    }

    protected abstract TYPE read(JsonElement var1);

    public abstract void save(JsonObject var1, TYPE var2);

    public boolean validate(TYPE value) {
        return value != null;
    }
}

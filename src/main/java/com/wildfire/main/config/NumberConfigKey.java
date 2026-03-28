package com.wildfire.main.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

public abstract class NumberConfigKey<TYPE extends Number & Comparable<TYPE>> extends ConfigKey<TYPE> {
    protected final @Nullable TYPE minInclusive;
    protected final @Nullable TYPE maxInclusive;

    protected NumberConfigKey(String key, TYPE defaultValue) {
        this(key, defaultValue, null, null);
    }

    protected NumberConfigKey(String key, TYPE defaultValue, @Nullable TYPE minInclusive, @Nullable TYPE maxInclusive) {
        super(key, defaultValue);
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    protected abstract TYPE fromPrimitive(JsonPrimitive var1);

    protected TYPE read(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber() || primitive.isString()) {
                try {
                    return (TYPE)this.fromPrimitive(primitive);
                } catch (NumberFormatException var4) {
                }
            }
        }

        return (TYPE)(this.defaultValue);
    }

    public void save(JsonObject object, TYPE value) {
        object.addProperty(this.key, value);
    }

    public boolean validate(TYPE value) {
        if (!super.validate(value)) {
            return false;
        } else {
            return (this.minInclusive == null || ((Comparable)this.minInclusive).compareTo(value) <= 0) && (this.maxInclusive == null || ((Comparable)this.maxInclusive).compareTo(value) >= 0);
        }
    }
}

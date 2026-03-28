package com.wildfire.main.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.wildfire.main.Gender;

public class GenderConfigKey extends ConfigKey<Gender> {
    private static final Gender[] GENDERS = Gender.values();

    public GenderConfigKey(String key) {
        super(key, Gender.MALE);
    }

    protected Gender read(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (!primitive.isNumber()) {
                return primitive.getAsBoolean() ? Gender.MALE : Gender.FEMALE;
            }

            int ordinal = primitive.getAsInt();
            if (ordinal >= 0 && ordinal < GENDERS.length) {
                return GENDERS[ordinal];
            }
        }

        return (Gender)this.defaultValue;
    }

    public void save(JsonObject object, Gender value) {
        object.addProperty(this.key, value.ordinal());
    }
}

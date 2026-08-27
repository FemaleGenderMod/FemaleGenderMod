/*
 * Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
 * Copyright (C) 2023-present WildfireRomeo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wildfire.client.contributors;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.function.BiFunction;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;

public class ContributorDeserializer implements JsonDeserializer<Contributor> {

    @Override
    public Contributor deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = GsonHelper.convertToJsonObject(json, "json");
        int roles = GsonHelper.getAsInt(object, "roles");
        TextColor color = deserializeOrNull(object, "color", (element, name) -> TextColor.fromRgb(GsonHelper.convertToInt(element, name)));
        String name = deserializeOrNull(object, "name", GsonHelper::convertToString);
        boolean showInCredits = GsonHelper.getAsBoolean(object, "show_in_credits", false);
        return new Contributor(roles, color, name, showInCredits);
    }

    @Nullable
    private <TYPE> TYPE deserializeOrNull(JsonObject object, String name, BiFunction<JsonElement, String, TYPE> deserializer) {
        if (object.has(name)) {
            JsonElement element = object.get(name);
            if (!element.isJsonNull()) {
                return deserializer.apply(element, name);
            }
        }
        return null;
    }
}

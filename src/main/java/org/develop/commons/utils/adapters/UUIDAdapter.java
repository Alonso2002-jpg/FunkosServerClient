package org.develop.commons.utils.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.develop.commons.model.serverUse.Request;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.UUID;

public class UUIDAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {

    @Override
    public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String uuidString = json.getAsString();
        return UUID.fromString(uuidString);
    }

    @Override
    public JsonElement serialize(UUID src, Type typeOfSrc, JsonSerializationContext context) {
        String uuidString = src.toString();
        return new JsonPrimitive(uuidString);
    }
}

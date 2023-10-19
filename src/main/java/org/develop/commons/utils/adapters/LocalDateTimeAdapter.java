package org.develop.commons.utils.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Un adaptador de tipo personalizado para la serializacion y deserializacion de objetos
 * de tipo LocalDateTime a y desde formato JSON. Este adaptador se utiliza con la libreria Gson.
 */
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String dateTimeString = json.getAsString();
        return LocalDateTime.parse(dateTimeString, formatter);
    }

    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        String dateTimeString = src.format(formatter);
        return new JsonPrimitive(dateTimeString);
    }

}

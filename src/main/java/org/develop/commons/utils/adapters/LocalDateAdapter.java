package org.develop.commons.utils.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.develop.commons.model.serverUse.Request;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Un adaptador de tipo personalizado para la serializacion y deserializacion de objetos
 * de tipo LocalDate a y desde formato JSON. Este adaptador se utiliza con la libreria Gson.
 */
public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String dateString = json.getAsString();
        return LocalDate.parse(dateString, formatter);
    }

    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
        String dateString = src.format(formatter);
        return new JsonPrimitive(dateString);
    }
}

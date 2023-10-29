package org.develop.commons.utils.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.develop.commons.model.serverUse.Request;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * Un adaptador para la serializacion y deserializacion de objetos UUID en formato JSON utilizando la biblioteca Gson.
 */
public class UUIDAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {

    /**
     * Deserializa un objeto UUID a partir de un elemento JSON.
     *
     * @param json El elemento JSON que representa el UUID como una cadena.
     * @param typeOfT El tipo de destino.
     * @param context El contexto de deserializacion JSON.
     * @return El objeto UUID deserializado.
     * @throws JsonParseException Si ocurre un error durante la deserializacion.
     */
    @Override
    public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String uuidString = json.getAsString();
        return UUID.fromString(uuidString);
    }

    /**
     * Serializa un objeto UUID a un elemento JSON.
     *
     * @param src El objeto UUID que se va a serializar.
     * @param typeOfSrc El tipo de origen.
     * @param context El contexto de serializacion JSON.
     * @return El elemento JSON que representa el UUID serializado.
     */
    @Override
    public JsonElement serialize(UUID src, Type typeOfSrc, JsonSerializationContext context) {
        String uuidString = src.toString();
        return new JsonPrimitive(uuidString);
    }
}

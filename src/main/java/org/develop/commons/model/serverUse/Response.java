package org.develop.commons.model.serverUse;

/**
 * Un registro que representa una respuesta del servidor en la aplicacion.
 * Contiene informacion sobre el estado de la respuesta, su contenido y la marca de tiempo de creacion.
 */
public record Response(Status status, String content, String createdAt) {
    /**
     * Enumeracion que define los estados de respuesta admitidos.
     */
    public enum Status {
        OK, ERROR, BYE, TOKEN
    }
}

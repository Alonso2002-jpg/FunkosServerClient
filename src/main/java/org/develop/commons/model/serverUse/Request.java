package org.develop.commons.model.serverUse;

/**
 * Un registro que representa una solicitud enviada al servidor.
 * Contiene informacion sobre el tipo de solicitud, su contenido, un token de autenticacion y la marca de tiempo de creacion.
 */
public record Request(Type type, String content, String token, String createdAt) {

    /**
     * Enumeracion que define los tipos de solicitud compatibles.
     */
        public enum Type {
        LOGIN, SALIR, OTRO, GETALL, GETBYID, GETBYMODEL, GETBYLAUNCHDATE,POST, UPDATE, DELETE
    }
}

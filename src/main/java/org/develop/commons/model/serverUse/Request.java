package org.develop.commons.model.serverUse;

public record Request(Type type, String content, String token, String createdAt) {
        public enum Type {
        LOGIN, SALIR, OTRO, GETALL, GETBYID, GETBYMODEL, GETBYLAUNCHDATE,POST, UPDATE, DELETE
    }
}

package org.develop.commons.model.serverUse;

/**
 * Un registro que representa a un usuario en la aplicacion.
 * Contiene informacion sobre el ID de usuario, nombre de usuario, contrasena y rol del usuario.
 */
public record User(long id, String username, String password, Role role) {
    /**
     * Enumeracion que define los roles de usuario admitidos.
     */
    public enum Role{
        ADMIN, USER
    }
}

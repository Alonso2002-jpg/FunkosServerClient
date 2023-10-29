package org.develop.commons.model.serverUse;

/**
 * Un registro que representa la informacion de inicio de sesion de un usuario.
 * Contiene el nombre de usuario y la contrasena asociados con el inicio de sesion.
 */
public record Login(String username, String password) {
}

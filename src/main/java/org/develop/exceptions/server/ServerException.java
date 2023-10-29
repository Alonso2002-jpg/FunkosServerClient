package org.develop.exceptions.server;

/**
 * Una excepcion personalizada para representar errores especificos del servidor en la aplicacion.
 */
public class ServerException extends Exception {
    /**
     * Constructor de la excepcion ServerException.
     *
     * @param message Un mensaje descriptivo que explica la causa de la excepcion.
     */
    public ServerException(String message) {
        super(message);
    }
}

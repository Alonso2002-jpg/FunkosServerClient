package org.develop.exceptions.client;

/**
 * Una excepcion personalizada para representar errores especificos del cliente en la aplicacion.
 */
public class ClientException extends Exception {
    /**
     * Constructor de la excepcion ClientException.
     *
     * @param message Un mensaje descriptivo que explica la causa de la excepcion.
     */
    public ClientException(String message) {
        super(message);
    }
}

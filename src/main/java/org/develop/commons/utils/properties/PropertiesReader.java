package org.develop.commons.utils.properties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Una clase que permite leer propiedades desde un archivo de propiedades.
 *
 * Esta clase carga propiedades desde un archivo de propiedades especificado y proporciona un metodo para acceder a esas propiedades.
 */
public class PropertiesReader {
    private final String fileName;
    private final Properties properties;

    /**
     * Constructor de la clase PropertiesReader.
     *
     * @param fileName El nombre del archivo de propiedades a cargar.
     * @throws IOException Si ocurre un error al cargar el archivo de propiedades.
     */
    public PropertiesReader(String fileName) throws IOException {
        this.fileName = fileName;
        properties = new Properties();

        InputStream file = getClass().getClassLoader().getResourceAsStream(fileName);
        if (file != null) {
            properties.load(file);
        } else {
            throw new FileNotFoundException("No se encuentra el fichero " + fileName);
        }
    }

    /**
     * Obtiene el valor de una propiedad especifica a partir de su clave.
     *
     * @param key La clave de la propiedad que se desea obtener.
     * @return El valor de la propiedad correspondiente a la clave.
     * @throws FileNotFoundException Si la clave no se encuentra en el archivo de propiedades.
     */
    public String getProperty(String key) throws FileNotFoundException {
        String value = properties.getProperty(key);
        if (value != null) {
            return value;
        } else {
            throw new FileNotFoundException("No se encuentra la propiedad " + key + " en el fichero " + fileName);
        }
    }
}

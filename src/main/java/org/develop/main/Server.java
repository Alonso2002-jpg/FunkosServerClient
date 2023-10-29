package org.develop.main;

import org.develop.commons.model.mainUse.MyIDGenerator;
import org.develop.commons.utils.properties.PropertiesReader;
import org.develop.repositories.funkos.FunkoRepositoryImpl;
import org.develop.services.database.DatabaseManager;
import org.develop.services.files.BackupManagerImpl;
import org.develop.services.funkos.FunkoNotificationImpl;
import org.develop.services.funkos.FunkoService;
import org.develop.services.funkos.FunkoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * El punto de entrada principal del servidor Funko. Inicia el servidor, configura la seguridad SSL, y escucha conexiones entrantes de clientes.
 *
 */
public class Server {
    public static final String TOKEN_SECRET = "TokenSuperUltraSecretoNoLoCuentes";
    public static final long TOKEN_EXPIRATION = 10000;
    private static final AtomicLong clientNumber = new AtomicLong(0);
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int PUERTO = 3000;
    private static final FunkoServiceImpl funkoService = FunkoServiceImpl.getInstance(FunkoRepositoryImpl.getInstance(DatabaseManager.getInstance(), MyIDGenerator.getInstance()), FunkoNotificationImpl.getInstance(), BackupManagerImpl.getInstance());

    public static void main(String[] args) {
        try {
            funkoService.getNotifications().subscribe(
                notificacion -> {
                    switch (notificacion.getTipo()) {
                        case NEW:
                            logger.info("🟢 Funko insertado: " + notificacion.getContenido());
                            break;
                        case UPDATED:
                            logger.info("🟠 Funko actualizado: " + notificacion.getContenido());
                            break;
                        case DELETED:
                            logger.info("🔴 Funko eliminado: " + notificacion.getContenido());
                            break;
                    }
                },
                error -> logger.error("Se ha producido un error: " + error),
                () -> logger.info("Completado")
        );
            var myConfig = readConfigFile();

            logger.debug("Configurando TSL");

            System.setProperty("javax.net.ssl.keyStore", myConfig.get("keyFile"));
            System.setProperty("javax.net.ssl.keyStorePassword", myConfig.get("keyPassword"));

            SSLServerSocketFactory serverFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) serverFactory.createServerSocket(PUERTO);


            funkoService.imported("funkos.csv").subscribe(fk -> {
                funkoService.save(fk).subscribe();
            });

            logger.debug("🚀 Servidor escuchando en el puerto 3000");

            while (true){
                new ClientHandler(serverSocket.accept(),clientNumber.incrementAndGet(),funkoService).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lee la configuracion del servidor desde un archivo de propiedades y la devuelve en forma de mapa de cadenas.
     *
     * @return Un mapa que contiene la configuracion leida con las siguientes claves:
     *   - "keyFile": Ruta al archivo de clave utilizado para SSL.
     *   - "keyPassword": Contrasena asociada al archivo de clave.
     *   - "tokenSecret": Clave secreta para firmar y verificar tokens de autenticacion.
     *   - "tokenExpiration": Duracion de validez de los tokens de autenticacion en milisegundos.
     *
     * @throws IllegalStateException Si falta la ruta del archivo de clave o la contrasena en la configuracion.
     * @throws FileNotFoundException Si el archivo de clave no se encuentra en la ruta especificada.
     */
    public static Map<String,String> readConfigFile(){
        try{
            logger.debug("Reading config file");
            PropertiesReader propertiesReader = new PropertiesReader("server.properties");

            String keyFile= propertiesReader.getProperty("keyFile");
            String keyPassword = propertiesReader.getProperty("keyPassword");
            String tokenSecret = propertiesReader.getProperty("tokenSecret");
            String tokenExpiration = propertiesReader.getProperty("tokenExpiration");

            if (keyFile.isEmpty() || keyPassword.isEmpty()){
                throw new IllegalStateException("Missing keyFile or keyPassword");
            }

            if (!Files.exists(Path.of(keyFile))){
                throw new FileNotFoundException("No se encuentra el fichero " + keyFile);
            }

            Map<String,String> configMap = new HashMap<>();
            configMap.put("keyFile", keyFile);
            configMap.put("keyPassword", keyPassword);
            configMap.put("tokenSecret", tokenSecret);
            configMap.put("tokenExpiration", tokenExpiration);

            return configMap;
        }catch (FileNotFoundException e){
            logger.error("Error en clave: " + e.getLocalizedMessage());
            System.exit(1);
            return null;
        }
        catch (IOException e) {
          logger.error("Error al leer el fichero de propiedades: " + e.getLocalizedMessage());
            return null;
        }
    }
}

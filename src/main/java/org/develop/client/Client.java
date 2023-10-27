package org.develop.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.develop.commons.model.mainUse.Funko;
import org.develop.commons.model.mainUse.Modelo;
import org.develop.commons.model.serverUse.Login;
import org.develop.commons.model.serverUse.Request;
import org.develop.commons.model.serverUse.Response;
import org.develop.commons.utils.adapters.LocalDateAdapter;
import org.develop.commons.utils.adapters.LocalDateTimeAdapter;
import org.develop.commons.utils.adapters.UUIDAdapter;
import org.develop.commons.utils.properties.PropertiesReader;
import org.develop.exceptions.client.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Client {
 private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(UUID.class, new UUIDAdapter()).create();
    private SSLSocket socket;
    private PrintWriter out;
    private BufferedReader in;

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void start() throws IOException {
        try {
            openConnection();

            String token = sendRequestLogin();

            sendRequestGetAllFunkos(token);

            sendRequestGetFunkoById(token,"10");

            sendRequestGetFunkosByModel(token,"Marvel");

            sendRequestGetFunkosByDate(token,"2023");

            Funko fkn = Funko.builder().id(10)
                            .name("Funko Update Num 10 NUEVO")
                                    .precio(1000.1)
                                            .modelo(Modelo.OTROS)
                                                    .build();

            sendRequestUpdateFunko(token,fkn);

            sendRequestGetFunkoById(token,"10");

            sendRequestDeleteFunko(token,Funko.builder().id(19).build());

            sendRequestGetFunkoById(token,"19");

            sendRequestSalir();

        } catch (ClientException ex) {
            logger.error("Error: " + ex.getMessage());
            closeConnection();
            System.exit(1);
        }
    }
    private void closeConnection() throws IOException {
        logger.debug("Cerrando la conexión con el servidor: " + HOST + ":" + PORT);
        logger.info("🔵 Cerrando Cliente");
        if (in != null)
            in.close();
        if (out != null)
            out.close();
        if (socket != null)
            socket.close();
    }

    private void openConnection() throws IOException {
        logger.info("🔵 Iniciando Cliente");
        Map<String,String> myConfig = readConfigFile();

        logger.debug("Cargando Fichero de Propiedades");
        System.setProperty("javax.net.ssl.trustStore", myConfig.get("keyFile"));
        System.setProperty("javax.net.ssl.trustStorePassword", myConfig.get("keyPassword"));

        SSLSocketFactory clientFactory =(SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = (SSLSocket) clientFactory.createSocket(HOST, PORT);

        logger.debug("Conectando al server: " + HOST + ":" + PORT);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        logger.info("✅ Cliente conectado a " + HOST + ":" + PORT);

    }

    public static Map<String,String> readConfigFile(){
        try{
            logger.debug("Reading config file");
            PropertiesReader propertiesReader = new PropertiesReader("client.properties");

            String keyFile= propertiesReader.getProperty("keyFile");
            String keyPassword = propertiesReader.getProperty("keyPassword");

            if (keyFile.isEmpty() || keyPassword.isEmpty()){
                throw new IllegalStateException("Missing keyFile or keyPassword");
            }

            if (!Files.exists(Path.of(keyFile))){
                throw new FileNotFoundException("No se encuentra el fichero " + keyFile);
            }

            Map<String,String> configMap = new HashMap<>();
            configMap.put("keyFile", keyFile);
            configMap.put("keyPassword", keyPassword);

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

    private String sendRequestLogin() throws ClientException{
        String myToken = null;
        var loginGson = gson.toJson(new Login("pepe","pepe1234"));

        Request request = new Request(Request.Type.LOGIN, loginGson, null, LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        try {
            Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());

            logger.debug("Response Received Type: " + response.status());

            switch (response.status()){
                case TOKEN ->{
                    logger.info("🟢 Mi token es: " + response.content());
                    myToken = response.content();
                }
                default -> throw new ClientException("Unexpected response status: " + response.status());
            }
        } catch (IOException e) {
            logger.error("Error: " + e.getMessage());
        }

        return myToken;
    }

    private void sendRequestSalir() throws IOException, ClientException {
        Request request = new Request(Request.Type.SALIR, null, null, LocalDateTime.now().toString());
        logger.debug("Request Sent: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()){
            case BYE -> {
                logger.info("🟢 Adios");
                closeConnection();
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }

    private void sendRequestGetAllFunkos(String token) throws ClientException, IOException {
        Request request = new Request(Request.Type.GETALL, null, token, LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()){
            case OK -> {
                List<Funko> responseFunkos = gson.fromJson(response.content(),new TypeToken<List<Funko>>(){}.getType());
                logger.info("🟢 Los funkos son: " + responseFunkos);
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }

    private void sendRequestGetFunkoById(String token, String id) throws ClientException, IOException {
        Request request = new Request(Request.Type.GETBYID, id, token, LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()){
            case OK -> {
                Funko responseFunko = gson.fromJson(response.content(),new TypeToken<Funko>(){}.getType());
                logger.info("🟢 El Funko es: " + responseFunko);
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }

    private void sendRequestGetFunkosByModel(String token, String model) throws ClientException, IOException {
        Request request = new Request(Request.Type.GETBYMODEL, model.toUpperCase(), token, LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()) {
            case OK -> {
                List<Funko> responseFunkos = gson.fromJson(response.content(),new TypeToken<List<Funko>>(){}.getType());
                logger.info("🟢 Los funkos con Modelo " + model +" son: " + responseFunkos);
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }

    private void sendRequestGetFunkosByDate(String token, String date) throws ClientException, IOException {
        Request request = new Request(Request.Type.GETBYLAUNCHDATE, date, token, LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()) {
            case OK -> {
                List<Funko> responseFunkos = gson.fromJson(response.content(),new TypeToken<List<Funko>>(){}.getType());
                logger.info("🟢 Los funkos con fecha de lanzamiento " + date +" son: " + responseFunkos);
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }

    private void sendRequestPostFunko(String token, Funko funko) throws ClientException, IOException {
        Request request = new Request(Request.Type.POST, gson.toJson(funko), token, LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()) {
            case OK -> {
                Funko responseFunko = gson.fromJson(response.content(),new TypeToken<Funko>(){}.getType());
                logger.info("🟢 El Funko fue creado con exito");
                logger.info(responseFunko.toString());
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }

    private void sendRequestUpdateFunko(String token, Funko funko) throws ClientException, IOException {
        Request request = new Request(Request.Type.UPDATE, gson.toJson(funko),token,LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()) {
            case OK -> {
                Funko responseFunko = gson.fromJson(response.content(),new TypeToken<Funko>(){}.getType());
                logger.info("🟢 El Funko fue actualizado con exito");
                logger.info(responseFunko.toString());
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }

    private void sendRequestDeleteFunko(String token, Funko funko) throws ClientException, IOException {
        Request request = new Request(Request.Type.DELETE,String.valueOf(funko.getId()),token,LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()) {
            case OK -> {
                Funko deleted = gson.fromJson(response.content(),new TypeToken<Funko>(){}.getType());
                  logger.info("🟢 El Funko fue eliminado con exito: " + deleted);
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }
}

package org.develop.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.develop.commons.model.mainUse.Funko;
import org.develop.commons.model.mainUse.Modelo;
import org.develop.commons.model.serverUse.Login;
import org.develop.commons.model.serverUse.Request;
import org.develop.commons.model.serverUse.Response;
import org.develop.commons.model.serverUse.User;
import org.develop.commons.utils.adapters.LocalDateAdapter;
import org.develop.commons.utils.adapters.LocalDateTimeAdapter;
import org.develop.exceptions.server.ServerException;
import org.develop.repositories.users.UserRepository;
import org.develop.services.funkos.FunkoService;
import org.develop.services.token.TokenService;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Clase que maneja la comunicacion con un cliente en un hilo separado.
 */
public class ClientHandler extends Thread{
    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
    private final long clientNumber;
    private final FunkoService funkoService;

    BufferedReader in;
    PrintWriter out;

    /**
     * Constructor de la clase ClientHandler.
     *
     * @param socket El socket de cliente con el que se comunica este manejador.
     * @param clientNumber Un identificador único para el cliente manejado por este hilo.
     * @param funkoService El servicio FunkoService utilizado para realizar operaciones relacionadas con Funkos.
     */
    public ClientHandler(Socket socket, long clientNumber, FunkoService funkoService) {
        this.clientSocket = socket;
        this.clientNumber = clientNumber;
        this.funkoService = funkoService;
    }

    /**
     * Ejecuta el hilo del manejador de clientes para manejar las solicitudes del cliente.
     */
    @Override
    public void run(){
        try {
            openConnection();

            String clientInput;
            Request request;

            while (true){
                clientInput = in.readLine();
                logger.debug("Request Received: " + clientInput);
                request = gson.fromJson(clientInput,Request.class);
                handleRequest(request);
            }

        } catch (IOException e) {
            logger.error("Error: " + e.getMessage(), e);
        }catch (ServerException ex){
            out.println(gson.toJson(new Response(Response.Status.ERROR,ex.getMessage(),LocalDateTime.now().toString())));
        }
    }

    /**
     * Abre la conexion con el cliente.
     *
     * @throws IOException Si ocurre un error al abrir la conexion.
     */
    public void openConnection() throws IOException {
        logger.debug("Connectando con el cliente numero: " + clientNumber);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(),true);
    }

    /**
     * Cierra la conexion con el cliente.
     *
     * @throws IOException Si ocurre un error al cerrar la conexion.
     */
    public void closeConnection() throws IOException {
        logger.debug("Cerrando la conexion con el cliente numero: " + clientNumber);
        in.close();
        out.close();
        clientSocket.close();
    }

    /**
     * Maneja una solicitud recibida del cliente.
     *
     * @param request La solicitud recibida del cliente.
     * @throws ServerException Si se produce un error en el servidor al procesar la solicitud.
     */
    private void handleRequest(Request request) throws ServerException {
        logger.debug("Request Handler: " + request);

        switch (request.type()){
            case LOGIN -> processLogin(request);
            case SALIR -> processSalir();
            case GETALL -> processGetAll(request);
            case GETBYID -> processGetById(request);
            case GETBYMODEL -> processGetByModel(request);
            case GETBYLAUNCHDATE -> processGetByLaunchYear(request);
            case POST -> processPost(request);
            case UPDATE -> processUpdate(request);
            case DELETE -> processDelete(request);
            default -> new Response(Response.Status.ERROR, "Not implemented Request", LocalDateTime.now().toString());
        }
    }
    /**
     * Procesa la solicitud de salida (SALIR) del cliente y envia una respuesta de despedida.
     */
    private void processSalir(){
        out.println(gson.toJson(new Response(Response.Status.BYE,"Adios",LocalDateTime.now().toString())));
    }
    /**
     * Procesa la solicitud de inicio de sesion (LOGIN) del cliente y genera un token de autenticacion si es valido.
     *
     * @param request La solicitud de inicio de sesion del cliente.
     * @throws ServerException Si se produce un error durante el proceso de inicio de sesion.
     */
    private void processLogin(Request request) throws ServerException {
        logger.debug("Requested login Recieved: " + request);

        Login login = gson.fromJson(String.valueOf(request.content()),new TypeToken<Login>(){}.getType());

        var user = UserRepository.getInstance().findByUsername(login.username());
        if (user.isEmpty() || !BCrypt.checkpw(login.password(),user.get().password())){
            logger.warn("User not found or wrong password");
            throw  new ServerException("User not found or wrong password");
        }

        var token = TokenService.getInstance().createToken(user.get(),Server.TOKEN_SECRET,Server.TOKEN_EXPIRATION);

        logger.debug("Sending Response: " + token);
        out.println(gson.toJson(new Response(Response.Status.TOKEN,token,LocalDateTime.now().toString())));
    }
    /**
     * Procesa un token de autenticacion y verifica su validez, devolviendo el usuario correspondiente si es valido.
     *
     * @param token El token de autenticacion a procesar.
     * @return Un objeto Optional que contiene el usuario autenticado si el token es valido.
     * @throws ServerException Si se produce un error al verificar o procesar el token.
     */

    private Optional<User> processToken(String token) throws ServerException {
        if (TokenService.getInstance().verifyToken(token,Server.TOKEN_SECRET)){
            logger.debug("Token verified");
            var claims = TokenService.getInstance().getClaims(token,Server.TOKEN_SECRET);
            var id = claims.get("userid").asInt();
            var user = UserRepository.getInstance().findById(id);
            if (user.isEmpty()){
                logger.error("User Wrong Authentication");
                throw new ServerException("User Wrong Authentication");
            }
            return user;
        }else {
            logger.error("Token not verified");
            throw new ServerException("Token not verified");
        }

    }

    /**
     * Procesa la solicitud para obtener todos los Funkos y envia una respuesta que contiene la lista de Funkos.
     *
     * @param request La solicitud de obtener todos los Funkos.
     * @throws ServerException Si se produce un error durante el proceso de la solicitud.
     */
    private void processGetAll(Request request) throws ServerException {
        processToken(request.token());

        funkoService.findAll()
                .collectList()
                .subscribe(funkos -> {
                    logger.debug("Sending Response: " + funkos);
                    var resJson = gson.toJson(funkos);
                    out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                });
    }

    /**
     * Procesa la solicitud para obtener un Funko por su ID y envía una respuesta que contiene el Funko encontrado.
     *
     * @param request La solicitud de obtener un Funko por su ID.
     * @throws ServerException Si se produce un error durante el proceso de la solicitud.
     */
    private void processGetById(Request request) throws ServerException {
        processToken(request.token());

        var id = Integer.parseInt(request.content());
        funkoService.findById(id)
                .subscribe(
                        funko -> {
                            logger.debug("Sending Response: " + funko);
                            var resJson = gson.toJson(funko);
                            out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                        },
                        error -> {
                            logger.error("Error: " + error.getMessage());
                            out.println(gson.toJson(new Response(Response.Status.ERROR,error.getMessage(),LocalDateTime.now().toString())));
                        }
                );
    }

    /**
     * Procesa la solicitud para obtener Funkos por modelo y envia una respuesta que contiene la lista de Funkos encontrados.
     *
     * @param request La solicitud de obtener Funkos por modelo.
     * @throws ServerException Si se produce un error durante el proceso de la solicitud.
     */
    private void processGetByModel(Request request) throws ServerException {
        processToken(request.token());
        Modelo model = Modelo.valueOf(request.content());

        funkoService.findAll()
                .filter(funko -> funko.getModelo().equals(model))
                .collectList()
                .subscribe(
                        funkos -> {
                            logger.debug("Sending Response: " + funkos);
                            var resJson = gson.toJson(funkos);
                            out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                        },
                        error ->{
                            logger.error("Error: " + error.getMessage());
                            out.println(gson.toJson(new Response(Response.Status.ERROR,error.getMessage(),LocalDateTime.now().toString())));
                        }
                );
    }

    /**
     * Procesa la solicitud para obtener Funkos por ano de lanzamiento y envia una respuesta que contiene la lista de Funkos encontrados.
     *
     * @param request La solicitud de obtener Funkos por año de lanzamiento.
     * @throws ServerException Si se produce un error durante el proceso de la solicitud.
     */
    private void processGetByLaunchYear(Request request) throws ServerException {
        processToken(request.token());
        int launchDate = Integer.parseInt(request.content());

        funkoService.findAll()
                .filter(funko -> funko.getFecha_lanzamiento().getYear() == launchDate)
                .collectList()
                .subscribe(
                        funkos -> {
                            logger.debug("Sending Response: " + funkos);
                            var resJson = gson.toJson(funkos);
                            out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                        },
                        error ->{
                            logger.error("Error: " + error.getMessage());
                            out.println(gson.toJson(new Response(Response.Status.ERROR,error.getMessage(),LocalDateTime.now().toString())));
                        }
                );
    }

    /**
     * Procesa la solicitud para crear un nuevo Funko y envia una respuesta que contiene el Funko creado.
     *
     * @param request La solicitud de crear un nuevo Funko.
     * @throws ServerException Si se produce un error durante el proceso de la solicitud.
     */
    private void processPost(Request request) throws ServerException {
        var user = processToken(request.token());
        if (user.isPresent() && user.get().role().equals(User.Role.ADMIN)){
            Funko funko = gson.fromJson(String.valueOf(request.content()), new TypeToken<Funko>() {
            }.getType());
            funkoService.save(funko)
                    .subscribe(
                            funkoSave -> {
                                logger.debug("Sending Response: " + funkoSave);
                                var resJson = gson.toJson(funkoSave);
                                out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                            },
                            error -> {
                                logger.error("Error: " + error.getMessage());
                                out.println(gson.toJson(new Response(Response.Status.ERROR,error.getMessage(),LocalDateTime.now().toString())));
                            }
                    );
        }else {
            logger.error("User doesn't have the necessary permissions");
            out.println(gson.toJson(new Response(Response.Status.ERROR,"User doesn't have the necessary permissions",LocalDateTime.now().toString())));
        }
    }


    /**
     * Procesa la solicitud para actualizar un Funko existente y envia una respuesta que contiene el Funko actualizado.
     *
     * @param request La solicitud de actualizar un Funko existente.
     * @throws ServerException Si se produce un error durante el proceso de la solicitud.
     */
    private void processUpdate(Request request) throws ServerException {
        var user = processToken(request.token());

        if (user.isPresent() && user.get().role().equals(User.Role.ADMIN)){
            Funko funko = gson.fromJson(String.valueOf(request.content()), new TypeToken<Funko>() {
            }.getType());
            funkoService.update(funko)
                    .subscribe(
                            funkoUpt -> {
                                logger.debug("Sending Response: " + funkoUpt);
                                var resJson = gson.toJson(funkoUpt);
                                out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                            },
                            error -> {
                                logger.error("Error: " + error.getMessage());
                                out.println(gson.toJson(new Response(Response.Status.ERROR,error.getMessage(),LocalDateTime.now().toString())));
                            }
                    );
        }else {
            logger.error("User doesn't have the necessary permissions");
            out.println(gson.toJson(new Response(Response.Status.ERROR,"User doesn't have the necessary permissions",LocalDateTime.now().toString())));
        }
    }

    /**
     * Procesa la solicitud para eliminar un Funko existente y envia una respuesta que confirma la eliminacion.
     *
     * @param request La solicitud de eliminar un Funko existente por su ID.
     * @throws ServerException Si se produce un error durante el proceso de la solicitud.
     */
    private void processDelete(Request request) throws ServerException {
        var user = processToken(request.token());
        logger.debug("Borrando");
        if (user.isPresent() && user.get().role().equals(User.Role.ADMIN)){
            var id = Integer.parseInt(request.content());

            funkoService.deleteById(id)
                    .subscribe(
                            deleted -> {
                                logger.debug("Sending Response: " + deleted);
                                var resJson = gson.toJson(deleted);
                                out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                            },
                            error ->{
                                logger.error("Error: " + error.getMessage());
                                out.println(gson.toJson(new Response(Response.Status.ERROR,error.getMessage(),LocalDateTime.now().toString())));
                            }
                    );
        }else {
            logger.error("User doesn't have the necessary permissions");
            out.println(gson.toJson(new Response(Response.Status.ERROR,"User doesn't have the necessary permissions",LocalDateTime.now().toString())));
        }
    }

}

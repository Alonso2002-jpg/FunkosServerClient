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

    public ClientHandler(Socket socket, long clientNumber, FunkoService funkoService) {
        this.clientSocket = socket;
        this.clientNumber = clientNumber;
        this.funkoService = funkoService;
    }

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

    public void openConnection() throws IOException {
        logger.debug("Connectando con el cliente numero: " + clientNumber);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(),true);
    }

    public void closeConnection() throws IOException {
        logger.debug("Cerrando la conexion con el cliente numero: " + clientNumber);
        in.close();
        out.close();
        clientSocket.close();
    }

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
    private void processSalir(){
        out.println(gson.toJson(new Response(Response.Status.BYE,"Adios",LocalDateTime.now().toString())));
    }
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
            throw new ServerException("User doesn't have the necessary permissions");
        }
    }

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
            throw new ServerException("User doesn't have the necessary permissions");
        }
    }

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
            throw new ServerException("User doesn't have the necessary permissions");
        }
    }

}

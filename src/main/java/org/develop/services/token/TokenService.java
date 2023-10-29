package org.develop.services.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.develop.commons.model.serverUse.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Clase que proporciona metodos para la creacion y verificacion de tokens JWT (JSON Web Tokens).
 */
public class TokenService {
    private static TokenService INSTANCE = null;
    private final Logger logger = LoggerFactory.getLogger(TokenService.class);

    /**
     * Constructor privado para garantizar que esta clase sea un Singleton.
     */
    private TokenService() {
    }

    /**
     * Obtiene una instancia unica de TokenService (Singleton).
     *
     * @return La instancia unica de TokenService.
     */
    public synchronized static TokenService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TokenService();
        }
        return INSTANCE;
    }

    /**
     * Crea un token JWT para un usuario con los datos proporcionados.
     *
     * @param user           El usuario para el que se crea el token.
     * @param tokenSecret    La clave secreta para firmar el token.
     * @param tokenExpiration El tiempo de expiracion del token en milisegundos.
     * @return El token JWT creado.
     */
    public String createToken(User user, String tokenSecret, long tokenExpiration) {
        logger.debug("Creando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        return JWT.create()
                //.withIssuer("2DAW") // Quien lo emite *
                //.withSubject("Desarrollo Web Entornos Servidor") // Para que lo emite *
                // Solo guardar el id del usuario, lo demas siempre lo podemos recuperar
                // y no damos pistas al atacante
                .withClaim("userid", user.id()) // Datos que queremos guardar * (al menos algunos)
                .withClaim("username", user.username()) // Datos que queremos guardar
                .withClaim("rol", user.role().toString()) // Datos que queremos guardar
                .withIssuedAt(new Date()) // Fecha de emision *
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpiration)) // Fecha de expiracion *
                //.withJWTId(UUID.randomUUID().toString()) // Identificador unico del token
                //.withNotBefore(new Date(System.currentTimeMillis() + 1000L)) // Fecha de cuando se puede usar
                .sign(algorithm); // Firmamos el token
    }

    /**
     * Verifica un token JWT con respecto a un usuario especifico.
     *
     * @param token       El token JWT que se va a verificar.
     * @param tokenSecret La clave secreta utilizada para firmar el token.
     * @param user        El usuario con el que se compara el token.
     * @return `true` si el token es valido y pertenece al usuario especificado, de lo contrario, `false`.
     */
    public boolean verifyToken(String token, String tokenSecret, User user) {
        logger.debug("Verificando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); // Creamos el verificador
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verificado");
            // Comprobamos que el token es del usuario
            // Solo compruebas los datos obligatorios, en este caso el id
            // meter mas información es dar pistas al atacante
            return decodedJWT.getClaim("userid").asLong() == user.id() &&
                    decodedJWT.getClaim("username").asString().equals(user.username()) &&
                    decodedJWT.getClaim("rol").asString().equals(user.role().toString());
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica un token JWT sin especificar un usuario.
     *
     * @param token       El token JWT que se va a verificar.
     * @param tokenSecret La clave secreta utilizada para firmar el token.
     * @return `true` si el token es valido, de lo contrario, `false`.
     */
    public boolean verifyToken(String token, String tokenSecret) {
        logger.debug("Verificando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); // Creamos el verificador
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verificado");
            return true;
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene las reclamaciones (claims) de un token JWT verificado.
     *
     * @param token       El token JWT que se va a verificar y del que se obtendran las reclamaciones.
     * @param tokenSecret La clave secreta utilizada para firmar el token.
     * @return Un mapa de reclamaciones (claims) del token verificado o `null` si ocurre un error.
     */
    public java.util.Map<String, com.auth0.jwt.interfaces.Claim> getClaims(String token, String tokenSecret) {
        logger.debug("Verificando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); // Creamos el verificador
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verificado");
            return decodedJWT.getClaims();
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return null;
        }
    }
}
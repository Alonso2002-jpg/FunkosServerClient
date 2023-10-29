package org.develop.repositories.users;

import org.develop.commons.model.serverUse.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

/**
 * Clase que representa un repositorio de usuarios con metodos para buscar usuarios por nombre de usuario o por ID.
 */
public class UserRepository {
    private static UserRepository INSTANCE = null;
    private final List<User> users = List.of(
            new User(
                    1,
                    "pepe",
                    BCrypt.hashpw("pepe1234", BCrypt.gensalt(12)),
                    User.Role.ADMIN
            ),
            new User(
                    2,
                    "ana",
                    BCrypt.hashpw("ana1234", BCrypt.gensalt(12)),
                    User.Role.USER
            )
    );
    /**
     * Constructor privado para garantizar que esta clase sea un Singleton.
     */
    private UserRepository() {
    }

    /**
     * Obtiene una instancia unica de UserRepository (Singleton).
     *
     * @return La instancia unica de UserRepository.
     */
    public synchronized static UserRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserRepository();
        }
        return INSTANCE;
    }

    /**
     * Busca un usuario por nombre de usuario.
     *
     * @param username El nombre de usuario a buscar.
     * @return Un objeto Optional que contiene el usuario si se encuentra, o vacío si no se encuentra.
     */
    public Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(user -> user.username().equals(username))
                .findFirst();
    }

    /**
     * Busca un usuario por su ID.
     *
     * @param id El ID del usuario a buscar.
     * @return Un objeto Optional que contiene el usuario si se encuentra, o vacío si no se encuentra.
     */
    public Optional<User> findById(int id) {
        return users.stream()
                .filter(user -> user.id() == id)
                .findFirst();
    }
}

package org.develop.repositories.users;

import org.develop.commons.model.serverUse.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {
    private UserRepository userRepository;
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

  @BeforeEach
  void setup() {
      userRepository = UserRepository.getInstance();
  }
    @Test
    void getInstance() {
        assertNotNull(UserRepository.getInstance());
    }

    @Test
    void findByUsername() {
      var user = userRepository.findByUsername("pepe");

      assertAll(
              () -> assertTrue(user.isPresent()),
              ()-> assertNotNull(user),
              ()-> assertEquals("pepe",user.get().username()),
              ()-> assertEquals(User.Role.ADMIN,user.get().role())
      );

    }

    @Test
    void findById() {
    }
}
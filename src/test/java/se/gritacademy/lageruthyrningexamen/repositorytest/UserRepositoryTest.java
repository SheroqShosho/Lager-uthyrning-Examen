package se.gritacademy.lageruthyrningexamen.repositorytest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import se.gritacademy.lageruthyrningexamen.domain.User;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("User repository tests")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save user and find it by email")
    void shouldSaveAndFindByEmail() {
        User user = new User(
                null,
                "test@example.com",
                "hashed",
                "Test Person",
                "CUSTOMER",
                null
        );

        userRepository.save(user);

        assertTrue (userRepository.findByEmail("test@example.com").isPresent());
        assertTrue(userRepository.existsByEmail("test@example.com"));

    }
}


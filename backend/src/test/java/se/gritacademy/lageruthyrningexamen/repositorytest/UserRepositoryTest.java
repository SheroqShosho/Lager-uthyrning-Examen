package se.gritacademy.lageruthyrningexamen.repositorytest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import se.gritacademy.lageruthyrningexamen.model.User;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
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

    @Test
    @DisplayName("Should find user by exact email")
    void shouldFindUserByExactEmail() {
        User user = new User(null, "findme@test.com", "pass", "Find Me", "CUSTOMER", Instant.now());
        userRepository.save(user);

        var result = userRepository.findByEmail("findme@test.com");
        assertTrue(result.isPresent());
        assertEquals("findme@test.com", result.get().getEmail());
    }

    @Test
    @DisplayName("Should not find user with wrong email")
    void shouldNotFindUserWithWrongEmail() {
        User user = new User(null, "notfound@test.com", "pass", "Not Found", "CUSTOMER", Instant.now());
        userRepository.save(user);

        var result = userRepository.findByEmail("wrong@test.com");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should confirm user existence by email")
    void shouldConfirmUserExistenceByEmail() {
        User user = new User(null, "exists@test.com", "pass", "Exists", "CUSTOMER", Instant.now());
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("exists@test.com"));
        assertFalse(userRepository.existsByEmail("notexists@test.com"));
    }

    @Test
    @DisplayName("Should save multiple users independently")
    void shouldSaveMultipleUsersIndependently() {
        User user1 = new User(null, "user1@test.com", "pass1", "User 1", "CUSTOMER", Instant.now());
        User user2 = new User(null, "user2@test.com", "pass2", "User 2", "ADMIN", Instant.now());
        User user3 = new User(null, "user3@test.com", "pass3", "User 3", "CUSTOMER", Instant.now());

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        assertTrue(userRepository.existsByEmail("user1@test.com"));
        assertTrue(userRepository.existsByEmail("user2@test.com"));
        assertTrue(userRepository.existsByEmail("user3@test.com"));
    }

    @Test
    @DisplayName("Should retrieve user with correct role")
    void shouldRetrieveUserWithCorrectRole() {
        User adminUser = new User(null, "admin@test.com", "pass", "Admin", "ADMIN", Instant.now());
        User customerUser = new User(null, "customer@test.com", "pass", "Customer", "CUSTOMER", Instant.now());

        userRepository.save(adminUser);
        userRepository.save(customerUser);

        var admin = userRepository.findByEmail("admin@test.com");
        var customer = userRepository.findByEmail("customer@test.com");

        assertEquals("ADMIN", admin.get().getRole());
        assertEquals("CUSTOMER", customer.get().getRole());
    }

    @Test
    @DisplayName("Should persist user full name")
    void shouldPersistUserFullName() {
        String fullName = "John Doe Smith Johnson";
        User user = new User(null, "fullname@test.com", "pass", fullName, "CUSTOMER", Instant.now());
        userRepository.save(user);

        var result = userRepository.findByEmail("fullname@test.com");
        assertEquals(fullName, result.get().getFullName());
    }

    @Test
    @DisplayName("Should persist creation timestamp")
    void shouldPersistCreationTimestamp() {
        Instant now = Instant.now();
        User user = new User(null, "timestamp@test.com", "pass", "Timestamp", "CUSTOMER", now);
        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }
}


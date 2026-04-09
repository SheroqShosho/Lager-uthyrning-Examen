package se.gritacademy.lageruthyrningexamen.modeltest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.gritacademy.lageruthyrningexamen.model.User;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User domain entity tests")
public class UserDomainTest {

    @Test
    @DisplayName("Should create user with all fields")
    void shouldCreateUserWithAllFields() {
        Instant now = Instant.now();
        User user = new User(1L, "john@example.com", "hashedpassword", "John Doe", "CUSTOMER", now);

        assertEquals(1L, user.getId());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("hashedpassword", user.getPassword());
        assertEquals("John Doe", user.getFullName());
        assertEquals("CUSTOMER", user.getRole());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    @DisplayName("Should create user with empty constructor")
    void shouldCreateUserWithEmptyConstructor() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getEmail());
    }

    @Test
    @DisplayName("Should update user email")
    void shouldUpdateUserEmail() {
        User user = new User(1L, "old@example.com", "pass", "John", "CUSTOMER", Instant.now());

        user.setEmail("new@example.com");

        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Should update user password")
    void shouldUpdateUserPassword() {
        User user = new User(1L, "john@example.com", "oldpass", "John", "CUSTOMER", Instant.now());

        user.setPassword("newhashedpass");

        assertEquals("newhashedpass", user.getPassword());
    }

    @Test
    @DisplayName("Should update user full name")
    void shouldUpdateUserFullName() {
        User user = new User(1L, "john@example.com", "pass", "John", "CUSTOMER", Instant.now());

        user.setFullName("Jane Doe");

        assertEquals("Jane Doe", user.getFullName());
    }

    @Test
    @DisplayName("Should update user role")
    void shouldUpdateUserRole() {
        User user = new User(1L, "john@example.com", "pass", "John", "CUSTOMER", Instant.now());

        user.setRole("ADMIN");

        assertEquals("ADMIN", user.getRole());
    }

    @Test
    @DisplayName("Should have CUSTOMER role by default")
    void shouldHaveCustomerRoleByDefault() {
        User user = new User(1L, "john@example.com", "pass", "John", "CUSTOMER", Instant.now());

        assertEquals("CUSTOMER", user.getRole());
    }

    @Test
    @DisplayName("Should update user to ADMIN role")
    void shouldUpdateUserToAdminRole() {
        User user = new User(1L, "john@example.com", "pass", "John", "CUSTOMER", Instant.now());
        user.setRole("ADMIN");
        assertEquals("ADMIN", user.getRole());
    }

    @Test
    @DisplayName("Should handle multiple role changes")
    void shouldHandleMultipleRoleChanges() {
        User user = new User();
        user.setRole("CUSTOMER");
        assertEquals("CUSTOMER", user.getRole());
        
        user.setRole("ADMIN");
        assertEquals("ADMIN", user.getRole());
        
        user.setRole("CUSTOMER");
        assertEquals("CUSTOMER", user.getRole());
    }

    @Test
    @DisplayName("Should persist created timestamp on creation")
    void shouldPersistCreatedTimestampOnCreation() {
        Instant now = Instant.now();
        User user = new User(1L, "test@example.com", "pass", "Test", "CUSTOMER", now);
        
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle null full name")
    void shouldHandleNullFullName() {
        User user = new User();
        assertNull(user.getFullName());
    }

    @Test
    @DisplayName("Should handle email updates")
    void shouldHandleEmailUpdates() {
        User user = new User(1L, "old@example.com", "pass", "Test", "CUSTOMER", Instant.now());
        user.setEmail("new@example.com");
        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Should handle password updates")
    void shouldHandlePasswordUpdates() {
        User user = new User(1L, "test@example.com", "oldpass", "Test", "CUSTOMER", Instant.now());
        user.setPassword("newhashedpass");
        assertEquals("newhashedpass", user.getPassword());
    }

    @Test
    @DisplayName("Should not be equal when IDs differ")
    void shouldNotBeEqualWhenIdsDiffer() {
        User user1 = new User(1L, "test@example.com", "pass", "Test", "CUSTOMER", Instant.now());
        User user2 = new User(2L, "test@example.com", "pass", "Test", "CUSTOMER", Instant.now());
        
        assertNotEquals(user1.getId(), user2.getId());
    }

    @Test
    @DisplayName("Should store and retrieve full name correctly")
    void shouldStoreAndRetrieveFullNameCorrectly() {
        String fullName = "John Doe Smith";
        User user = new User(1L, "john@example.com", "pass", fullName, "CUSTOMER", Instant.now());
        
        assertEquals(fullName, user.getFullName());
    }

    @Test
    @DisplayName("Should handle empty string values")
    void shouldHandleEmptyStringValues() {
        User user = new User();
        user.setEmail("");
        user.setPassword("");
        user.setFullName("");
        user.setRole("");
        
        assertEquals("", user.getEmail());
        assertEquals("", user.getPassword());
        assertEquals("", user.getFullName());
        assertEquals("", user.getRole());
    }

    @Test
    @DisplayName("Should allow setting ID after construction")
    void shouldAllowSettingIdAfterConstruction() {
        User user = new User();
        user.setId(99L);
        assertEquals(99L, user.getId());
    }

    @Test
    @DisplayName("Should allow setting creation timestamp after construction")
    void shouldAllowSettingCreationTimestampAfterConstruction() {
        User user = new User();
        Instant timestamp = Instant.now();
        user.setCreatedAt(timestamp);
        assertEquals(timestamp, user.getCreatedAt());
    }
}



package se.gritacademy.lageruthyrningexamen.servicetest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;
import se.gritacademy.lageruthyrningexamen.model.User;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Admin service tests")
public class AdminServiceTest {

    @Autowired
    private StorageUnitRepository storageUnitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;

    @BeforeEach
    void setup() {
        storageUnitRepository.deleteAll();
        userRepository.deleteAll();

        // Skapa admin-användare
        adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setFullName("Admin User");
        adminUser.setRole("ADMIN");
        adminUser = userRepository.save(adminUser);
    }

    @Test
    @DisplayName("Admin user should be created with correct role")
    void testAdminUserCreation() {
        assertEquals("ADMIN", adminUser.getRole());
        assertEquals("admin@test.com", adminUser.getEmail());
        assertEquals("Admin User", adminUser.getFullName());
        assertTrue(adminUser.getId() > 0);
    }

    @Test
    @DisplayName("Can save storage unit to repository")
    void testSaveStorageUnit() {
        StorageUnit unit = new StorageUnit(
            null, "Test Lager", "Beskrivning", new BigDecimal("50.00"),
            new BigDecimal("250.00"), "Test City", true, null
        );

        StorageUnit saved = storageUnitRepository.save(unit);

        assertNotNull(saved.getId());
        assertEquals("Test Lager", saved.getName());
        assertTrue(saved.isActive());
    }

    @Test
    @DisplayName("Can retrieve active storage units")
    void testRetrieveActiveStorageUnits() {
        // Skapa två aktiva och en inaktiv lagru
        StorageUnit unit1 = new StorageUnit(
            null, "Aktiv 1", "Desc", new BigDecimal("10.00"),
            new BigDecimal("100.00"), "City 1", true, null
        );
        StorageUnit unit2 = new StorageUnit(
            null, "Aktiv 2", "Desc", new BigDecimal("20.00"),
            new BigDecimal("200.00"), "City 2", true, null
        );
        StorageUnit unit3 = new StorageUnit(
            null, "Inaktiv", "Desc", new BigDecimal("30.00"),
            new BigDecimal("300.00"), "City 3", false, null
        );

        storageUnitRepository.saveAll(List.of(unit1, unit2, unit3));

        List<StorageUnit> activeUnits = storageUnitRepository.findByActiveTrue();

        assertEquals(2, activeUnits.size());
        assertTrue(activeUnits.stream().allMatch(StorageUnit::isActive));
    }

    @Test
    @DisplayName("Storage unit with different price ranges can be created")
    void testStorageUnitsWithDifferentPrices() {
        StorageUnit cheapUnit = new StorageUnit(
            null, "Billig", "Liten", new BigDecimal("5.00"),
            new BigDecimal("49.99"), "City", true, null
        );
        StorageUnit expensiveUnit = new StorageUnit(
            null, "Dyr", "Stor", new BigDecimal("500.00"),
            new BigDecimal("9999.99"), "City", true, null
        );

        StorageUnit savedCheap = storageUnitRepository.save(cheapUnit);
        StorageUnit savedExpensive = storageUnitRepository.save(expensiveUnit);

        assertTrue(savedCheap.getPricePerDay().compareTo(new BigDecimal("100.00")) < 0);
        assertTrue(savedExpensive.getPricePerDay().compareTo(new BigDecimal("1000.00")) > 0);
    }

    @Test
    @DisplayName("Storage unit can be updated")
    void testUpdateStorageUnit() {
        StorageUnit unit = new StorageUnit(
            null, "Original", "Desc", new BigDecimal("50.00"),
            new BigDecimal("250.00"), "City", true, null
        );
        StorageUnit saved = storageUnitRepository.save(unit);

        // Uppdatera
        saved.setName("Updated");
        saved.setPricePerDay(new BigDecimal("350.00"));
        StorageUnit updated = storageUnitRepository.save(saved);

        assertEquals("Updated", updated.getName());
        assertEquals(new BigDecimal("350.00"), updated.getPricePerDay());
    }

    @Test
    @DisplayName("Storage units with same name can exist")
    void testMultipleStorageUnitsWithSameName() {
        StorageUnit unit1 = new StorageUnit(
            null, "Samma Namn", "Desc 1", new BigDecimal("50.00"),
            new BigDecimal("250.00"), "City 1", true, null
        );
        StorageUnit unit2 = new StorageUnit(
            null, "Samma Namn", "Desc 2", new BigDecimal("100.00"),
            new BigDecimal("500.00"), "City 2", true, null
        );

        StorageUnit saved1 = storageUnitRepository.save(unit1);
        StorageUnit saved2 = storageUnitRepository.save(unit2);

        assertNotEquals(saved1.getId(), saved2.getId());
        List<StorageUnit> allUnits = storageUnitRepository.findByActiveTrue();
        assertEquals(2, allUnits.size());
    }

    @Test
    @DisplayName("Can deactivate storage unit")
    void testDeactivateStorageUnit() {
        StorageUnit unit = new StorageUnit(
            null, "Test", "Desc", new BigDecimal("50.00"),
            new BigDecimal("250.00"), "City", true, null
        );
        StorageUnit saved = storageUnitRepository.save(unit);
        assertTrue(saved.isActive());

        // Deaktivera
        saved.setActive(false);
        StorageUnit deactivated = storageUnitRepository.save(saved);

        assertFalse(deactivated.isActive());
        List<StorageUnit> activeUnits = storageUnitRepository.findByActiveTrue();
        assertFalse(activeUnits.stream().anyMatch(u -> u.getId().equals(deactivated.getId())));
    }

    @Test
    @DisplayName("Admin user can create and retrieve storage units")
    void testAdminWorkflow() {
        // Admin skapar lagru
        StorageUnit unit = new StorageUnit(
            null, "Admin Lager", "Skapat av admin", new BigDecimal("75.00"),
            new BigDecimal("375.00"), "Admin City", true, null
        );
        StorageUnit saved = storageUnitRepository.save(unit);

        // Hämta alla aktiva
        List<StorageUnit> activeUnits = storageUnitRepository.findByActiveTrue();

        assertTrue(activeUnits.stream().anyMatch(u -> u.getId().equals(saved.getId())));
        assertEquals("Admin Lager", saved.getName());
    }
}


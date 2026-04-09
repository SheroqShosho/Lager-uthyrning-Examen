package se.gritacademy.lageruthyrningexamen.modeltest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StorageUnit domain entity tests")
public class StorageUnitDomainTest {

    @Test
    @DisplayName("Should create storage unit with all fields")
    void shouldCreateStorageUnitWithAllFields() {
        StorageUnit unit = new StorageUnit(
            1L,
            "A1",
            "Premium storage",
            new BigDecimal("10.50"),
            new BigDecimal("100.00"),
            "Gothenburg",
            true,
            Instant.now()
        );

        assertEquals(1L, unit.getId());
        assertEquals("A1", unit.getName());
        assertEquals("Premium storage", unit.getDescription());
        assertEquals(new BigDecimal("10.50"), unit.getSizeM2());
        assertEquals(new BigDecimal("100.00"), unit.getPricePerDay());
        assertEquals("Gothenburg", unit.getLocation());
        assertTrue(unit.isActive());
    }

    @Test
    @DisplayName("Should create inactive storage unit")
    void shouldCreateInactiveStorageUnit() {
        StorageUnit unit = new StorageUnit(
            1L, "B2", "Inactive", new BigDecimal("5.00"), new BigDecimal("50.00"), "Stockholm", false, Instant.now()
        );

        assertFalse(unit.isActive());
    }

    @Test
    @DisplayName("Should set storage unit as active")
    void shouldSetStorageUnitAsActive() {
        StorageUnit unit = new StorageUnit(
            1L, "C3", "Test", new BigDecimal("5.00"), new BigDecimal("50.00"), "Stockholm", false, Instant.now()
        );

        unit.setActive(true);

        assertTrue(unit.isActive());
    }

    @Test
    @DisplayName("Should update storage unit price")
    void shouldUpdateStorageUnitPrice() {
        StorageUnit unit = new StorageUnit(
            1L, "D4", "Test", new BigDecimal("5.00"), new BigDecimal("50.00"), "Stockholm", true, Instant.now()
        );

        unit.setPricePerDay(new BigDecimal("75.00"));

        assertEquals(new BigDecimal("75.00"), unit.getPricePerDay());
    }

    @Test
    @DisplayName("Should get storage unit name")
    void shouldGetStorageUnitName() {
        StorageUnit unit = new StorageUnit(
            1L, "E5", "Test", new BigDecimal("5.00"), new BigDecimal("50.00"), "Stockholm", true, Instant.now()
        );

        assertEquals("E5", unit.getName());
    }

    @Test
    @DisplayName("Should have empty booking items initially")
    void shouldHaveEmptyBookingItemsInitially() {
        StorageUnit unit = new StorageUnit(
            1L, "F6", "Test", new BigDecimal("5.00"), new BigDecimal("50.00"), "Stockholm", true, Instant.now()
        );

        assertNotNull(unit.getBookingItems());
    }

    @Test
    @DisplayName("Should update storage unit size")
    void shouldUpdateStorageUnitSize() {
        StorageUnit unit = new StorageUnit(
            1L, "G1", "Test", new BigDecimal("50.00"), new BigDecimal("500.00"), "Gothenburg", true, Instant.now()
        );
        unit.setSizeM2(new BigDecimal("75.00"));
        assertEquals(new BigDecimal("75.00"), unit.getSizeM2());
    }


    @Test
    @DisplayName("Should update storage unit location")
    void shouldUpdateStorageUnitLocation() {
        StorageUnit unit = new StorageUnit(
            1L, "U1", "Test", new BigDecimal("40.00"), new BigDecimal("400.00"), "Uppsala", true, Instant.now()
        );
        unit.setLocation("Stockholm");
        assertEquals("Stockholm", unit.getLocation());
    }

    @Test
    @DisplayName("Should deactivate storage unit")
    void shouldDeactivateStorageUnit() {
        StorageUnit unit = new StorageUnit(
            1L, "L1", "Test", new BigDecimal("20.00"), new BigDecimal("200.00"), "Linköping", true, Instant.now()
        );
        unit.setActive(false);
        assertFalse(unit.isActive());
    }

    @Test
    @DisplayName("Should activate inactive storage unit")
    void shouldActivateInactiveStorageUnit() {
        StorageUnit unit = new StorageUnit(
            1L, "O1", "Test", new BigDecimal("15.00"), new BigDecimal("150.00"), "Örebro", false, Instant.now()
        );
        unit.setActive(true);
        assertTrue(unit.isActive());
    }

    @Test
    @DisplayName("Should update storage unit description")
    void shouldUpdateStorageUnitDescription() {
        StorageUnit unit = new StorageUnit(
            1L, "V1", "Old Description", new BigDecimal("25.00"), new BigDecimal("250.00"), "Västerås", true, Instant.now()
        );
        unit.setDescription("New Description");
        assertEquals("New Description", unit.getDescription());
    }

    @Test
    @DisplayName("Should handle null description")
    void shouldHandleNullDescription() {
        StorageUnit unit = new StorageUnit(
            1L, "N1", null, new BigDecimal("35.00"), new BigDecimal("350.00"), "Norrköping", true, Instant.now()
        );
        assertNull(unit.getDescription());
    }

    @Test
    @DisplayName("Should handle very large prices")
    void shouldHandleVeryLargePrices() {
        BigDecimal largePrice = new BigDecimal("99999.99");
        StorageUnit unit = new StorageUnit(
            1L, "BIG", "Large Price", new BigDecimal("1000.00"), largePrice, "Stockholm", true, Instant.now()
        );
        assertEquals(largePrice, unit.getPricePerDay());
    }

    @Test
    @DisplayName("Should handle very small prices")
    void shouldHandleVerySmallPrices() {
        BigDecimal smallPrice = new BigDecimal("0.01");
        StorageUnit unit = new StorageUnit(
            1L, "SMALL", "Small Price", new BigDecimal("1.00"), smallPrice, "Stockholm", true, Instant.now()
        );
        assertEquals(smallPrice, unit.getPricePerDay());
    }

    @Test
    @DisplayName("Should preserve name with special characters")
    void shouldPreserveNameWithSpecialCharacters() {
        String specialName = "Lager A-B 123 (Stockholm) #Test";
        StorageUnit unit = new StorageUnit(
            1L, specialName, "Test", new BigDecimal("50.00"), new BigDecimal("500.00"), "Stockholm", true, Instant.now()
        );
        assertEquals(specialName, unit.getName());
    }

    @Test
    @DisplayName("Should update booking items list")
    void shouldUpdateBookingItemsList() {
        StorageUnit unit = new StorageUnit(
            1L, "B1", "Test", new BigDecimal("50.00"), new BigDecimal("500.00"), "Stockholm", true, Instant.now()
        );
        assertTrue(unit.getBookingItems().isEmpty());
        
        unit.setBookingItems(new ArrayList<>());
        assertNotNull(unit.getBookingItems());
    }

    @Test
    @DisplayName("Should persist creation timestamp")
    void shouldPersistCreationTimestamp() {
        Instant now = Instant.now();
        StorageUnit unit = new StorageUnit(
            1L, "T1", "Test", new BigDecimal("50.00"), new BigDecimal("500.00"), "Stockholm", true, now
        );
        assertEquals(now, unit.getCreatedAt());
    }
}


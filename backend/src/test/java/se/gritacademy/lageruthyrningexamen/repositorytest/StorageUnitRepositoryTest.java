package se.gritacademy.lageruthyrningexamen.repositorytest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Storage unit repository tests")
public class StorageUnitRepositoryTest {

    @Autowired
    private StorageUnitRepository storageUnitRepository;

    @Test
    @DisplayName("Should return only active storage units")
    void shouldReturnOnlyActiveStorageUnits() {
        StorageUnit activeUnit = new StorageUnit(
                null,
                "A1",
                "Active unit",
                new BigDecimal("5.00"),
                new BigDecimal("99.00"),
                "Gothenburg",
                true,
                null
        );

        StorageUnit inactiveUnit = new StorageUnit(
                null,
                "B2",
                "Inactive unit",
                new BigDecimal("10.00"),
                new BigDecimal("149.00"),
                "Malmo",
                false,
                null
        );

        storageUnitRepository.save(activeUnit);
        storageUnitRepository.save(inactiveUnit);

        List<StorageUnit> activeUnits = storageUnitRepository.findByActiveTrue();

        assertEquals(1, activeUnits.size());
        assertEquals("A1", activeUnits.get(0).getName());

    }

    @Test
    @DisplayName("Should save and retrieve storage unit with all fields")
    void shouldSaveAndRetrieveStorageUnitWithAllFields() {
        StorageUnit unit = new StorageUnit(
            null, "Complete", "Fully furnished", 
            new BigDecimal("100.50"), new BigDecimal("999.99"), 
            "Stockholm", true, Instant.now()
        );
        
        StorageUnit saved = storageUnitRepository.save(unit);
        
        assertNotNull(saved.getId());
        assertEquals("Complete", saved.getName());
        assertEquals("Fully furnished", saved.getDescription());
        assertEquals(new BigDecimal("100.50"), saved.getSizeM2());
        assertEquals(new BigDecimal("999.99"), saved.getPricePerDay());
        assertEquals("Stockholm", saved.getLocation());
        assertTrue(saved.isActive());
    }

    @Test
    @DisplayName("Should filter out inactive units")
    void shouldFilterOutInactiveUnits() {
        storageUnitRepository.save(new StorageUnit(null, "Active1", null, new BigDecimal("50"), new BigDecimal("500"), "Location1", true, Instant.now()));
        storageUnitRepository.save(new StorageUnit(null, "Inactive1", null, new BigDecimal("50"), new BigDecimal("500"), "Location2", false, Instant.now()));
        storageUnitRepository.save(new StorageUnit(null, "Active2", null, new BigDecimal("50"), new BigDecimal("500"), "Location3", true, Instant.now()));
        
        List<StorageUnit> activeUnits = storageUnitRepository.findByActiveTrue();
        
        assertEquals(2, activeUnits.size());
        assertTrue(activeUnits.stream().allMatch(StorageUnit::isActive));
    }

    @Test
    @DisplayName("Should retrieve multiple active storage units")
    void shouldRetrieveMultipleActiveStorageUnits() {
        for (int i = 0; i < 5; i++) {
            storageUnitRepository.save(new StorageUnit(
                null, "Unit" + i, null, 
                new BigDecimal(10 + i * 10), new BigDecimal(100 + i * 100), 
                "Location" + i, true, Instant.now()
            ));
        }
        
        List<StorageUnit> activeUnits = storageUnitRepository.findByActiveTrue();
        assertEquals(5, activeUnits.size());
    }

    @Test
    @DisplayName("Should persist storage unit with large size")
    void shouldPersistStorageUnitWithLargeSize() {
        StorageUnit largeUnit = new StorageUnit(
            null, "Large", null,
            new BigDecimal("5000.00"), new BigDecimal("50000.00"),
            "Stockholm", true, Instant.now()
        );
        
        StorageUnit saved = storageUnitRepository.save(largeUnit);
        assertEquals(new BigDecimal("5000.00"), saved.getSizeM2());
    }

    @Test
    @DisplayName("Should persist storage unit with decimal values")
    void shouldPersistStorageUnitWithDecimalValues() {
        StorageUnit decimalUnit = new StorageUnit(
            null, "Decimal", null,
            new BigDecimal("33.33"), new BigDecimal("333.33"),
            "Gothenburg", true, Instant.now()
        );
        
        StorageUnit saved = storageUnitRepository.save(decimalUnit);
        assertEquals(new BigDecimal("33.33"), saved.getSizeM2());
        assertEquals(new BigDecimal("333.33"), saved.getPricePerDay());
    }
}

package se.gritacademy.lageruthyrningexamen.repositorytest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import se.gritacademy.lageruthyrningexamen.domain.StorageUnit;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
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
}

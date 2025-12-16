package se.gritacademy.lageruthyrningexamen.repositorytest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import se.gritacademy.lageruthyrningexamen.domain.StorageUnit;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class StorageUnitRepositoryTest {

    @Autowired
    private StorageUnitRepository storageUnitRepository;

    @Test
    void shouldReturnOnlyActiveStorageUnits() {
        StorageUnit activeUnit = StorageUnit.builder()
                .name("A1")
                .description("Active unit")
                .sizeM2(new BigDecimal("5.00"))
                .pricePerDay(new BigDecimal("99.00"))
                .location("Malmo")
                .active(true)
                .build();

        StorageUnit inactiveUnit = StorageUnit.builder()
                .name("B2")
                .description("Inactive unit")
                .sizeM2(new BigDecimal("10.00"))
                .pricePerDay(new BigDecimal("149.00"))
                .location(("Malmo"))
                .active(false)
                .build();

        storageUnitRepository.save(activeUnit);
        storageUnitRepository.save(inactiveUnit);

        List<StorageUnit> activeUnits = storageUnitRepository.findByActiveTrue();

        assertEquals(1, activeUnits.size());
        assertEquals("A1", activeUnits.get(0).getName());

    }
}

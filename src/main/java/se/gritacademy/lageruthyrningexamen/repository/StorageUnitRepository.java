package se.gritacademy.lageruthyrningexamen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.gritacademy.lageruthyrningexamen.domain.StorageUnit;

import java.util.List;

public interface StorageUnitRepository extends JpaRepository <StorageUnit, Long> {
    List<StorageUnit> findByActiveTrue();
}

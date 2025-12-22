package se.gritacademy.lageruthyrningexamen.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.gritacademy.lageruthyrningexamen.domain.StorageUnit;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;

import java.util.List;

@RestController
@RequestMapping("/api/storage-units")
public class StorageUnitController {

    private final StorageUnitRepository storageUnitRepository;

    public StorageUnitController(StorageUnitRepository storageUnitRepository) {
        this.storageUnitRepository = storageUnitRepository;
    }

    @GetMapping
    public List<StorageUnit> listActiveUnits() {
        return storageUnitRepository.findByActiveTrue();
    }
}

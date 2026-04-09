package se.gritacademy.lageruthyrningexamen.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/iot")
public class IotController {

    private static final Logger logger = LoggerFactory.getLogger(IotController.class);
    private final StorageUnitRepository storageUnitRepository;

    public IotController(StorageUnitRepository storageUnitRepository) {
        this.storageUnitRepository = storageUnitRepository;
    }

    // Simulera öppning av ett lagerutrymme genom att verifiera att enheten finns
    @PostMapping("/storage-units/{storageUnitId}/open")
    public ResponseEntity<?> open(@PathVariable Long storageUnitId) {
        logger.info("IoT begäran: Öppna lagerutrymme med ID: {}", storageUnitId);
        
        // Kontrollera om lagerutrymmets ID finns i databasen
        if (!storageUnitRepository.existsById(storageUnitId)) {
            logger.warn("IoT begäran misslyckades: lagerutrymme med ID {} finns inte", storageUnitId);
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Storage unit not found",
                    "storageUnitId", storageUnitId
            ));
        }

        logger.info("IoT öppning lyckades för lagerutrymme ID: {}", storageUnitId);
        return ResponseEntity.ok(Map.of(
                "storageUnitId", storageUnitId,
                "action", "OPEN",
                "result", "OK",
                "timestamp", Instant.now().toString()
        ));
    }

    // Simulera låsning av ett lagerutrymme genom att verifiera att enheten finns
    @PostMapping("/storage-units/{storageUnitId}/lock")
    public ResponseEntity<?> lock(@PathVariable Long storageUnitId) {
        logger.info("IoT begäran: Lasa lagerutrymme med ID: {}", storageUnitId);
        
        // Kontrollera om lagerutrymmets ID finns i databasen
        if (!storageUnitRepository.existsById(storageUnitId)) {
            logger.warn("IoT begäran misslyckades: lagerutrymme med ID {} finns inte", storageUnitId);
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Storage unit not found",
                    "storageUnitId", storageUnitId
            ));
        }

        logger.info("IoT lasning lyckades for lagerutrymme ID: {}", storageUnitId);
        return ResponseEntity.ok(Map.of(
                "storageUnitId", storageUnitId,
                "action", "LOCK",
                "result", "OK",
                "timestamp", Instant.now().toString()
        ));
    }
}

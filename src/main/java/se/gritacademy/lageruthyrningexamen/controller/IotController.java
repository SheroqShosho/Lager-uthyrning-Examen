package se.gritacademy.lageruthyrningexamen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/iot")
public class IotController {

    @PostMapping("/storage-units/{storageUnitId}/open")
    public ResponseEntity<?> open(@PathVariable Long storageUnitId) {
        return ResponseEntity.ok(Map.of(
                "storageUnitId", storageUnitId,
                "action", "OPEN",
                "result", "OK",
                "timestamp", Instant.now().toString()
        ));
    }

    @PostMapping("/storage-units/{storageUnitId}/lock")
    public ResponseEntity<?> lock(@PathVariable Long storageUnitId) {
        return ResponseEntity.ok(Map.of(
                "storageUnitId", storageUnitId,
                "action", "LOCK",
                "result", "OK",
                "timestamp", Instant.now().toString()
        ));
    }
}

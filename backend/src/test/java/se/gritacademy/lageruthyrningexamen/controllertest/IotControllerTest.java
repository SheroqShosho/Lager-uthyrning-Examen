package se.gritacademy.lageruthyrningexamen.controllertest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("IotController integration tests")
public class IotControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private StorageUnitRepository storageUnitRepository;
    @Autowired private BookingRepository bookingRepository;

    private StorageUnit storageUnit;

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        storageUnitRepository.deleteAll();

        storageUnit = new StorageUnit(
                null, "IOT1", "IoT Test Unit", new BigDecimal("5.00"),
                new BigDecimal("200.00"), "Stockholm", true, null
        );
        storageUnit = storageUnitRepository.save(storageUnit);
    }

    @Test
    @DisplayName("Open existing unit returns storageUnitId, action=OPEN, result=OK and timestamp")
    void shouldOpenStorageUnit() throws Exception {
        mockMvc.perform(post("/api/iot/storage-units/{id}/open", storageUnit.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageUnitId", is(storageUnit.getId().intValue())))
                .andExpect(jsonPath("$.action", is("OPEN")))
                .andExpect(jsonPath("$.result", is("OK")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Lock existing unit returns storageUnitId, action=LOCK, result=OK and timestamp")
    void shouldLockStorageUnit() throws Exception {
        mockMvc.perform(post("/api/iot/storage-units/{id}/lock", storageUnit.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageUnitId", is(storageUnit.getId().intValue())))
                .andExpect(jsonPath("$.action", is("LOCK")))
                .andExpect(jsonPath("$.result", is("OK")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Open non-existent unit returns 404 with error body")
    void shouldReturn404WhenOpeningMissingUnit() throws Exception {
        mockMvc.perform(post("/api/iot/storage-units/99999/open")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Storage unit not found")))
                .andExpect(jsonPath("$.storageUnitId", is(99999)));
    }

    @Test
    @DisplayName("Lock non-existent unit returns 404 with error body")
    void shouldReturn404WhenLockingMissingUnit() throws Exception {
        mockMvc.perform(post("/api/iot/storage-units/99999/lock")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Storage unit not found")))
                .andExpect(jsonPath("$.storageUnitId", is(99999)));
    }

    @Test
    @DisplayName("Multiple open/lock cycles all succeed")
    void shouldHandleMultipleCycles() throws Exception {
        mockMvc.perform(post("/api/iot/storage-units/{id}/open", storageUnit.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("OPEN")));

        mockMvc.perform(post("/api/iot/storage-units/{id}/lock", storageUnit.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("LOCK")));

        mockMvc.perform(post("/api/iot/storage-units/{id}/open", storageUnit.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("OPEN")));
    }
}
package se.gritacademy.lageruthyrningexamen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class CreateBookingRequest {

    @NotNull(message = "Booking items are required")
    @NotEmpty(message = "At least one item must be selected")
    @Valid
    private List<BookingItemRequest> items;

    public List<BookingItemRequest> getItems() { return items; }
    public void setItems(List<BookingItemRequest> items) { this.items = items; }

    // Inre klass för att hålla data per förråd
    public static class BookingItemRequest {
        @NotNull private Long storageUnitId;
        @NotNull private LocalDate startDate;
        @NotNull private LocalDate endDate;

        // Getters & Setters
        public Long getStorageUnitId() { return storageUnitId; }
        public void setStorageUnitId(Long storageUnitId) { this.storageUnitId = storageUnitId; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }
}
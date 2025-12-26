package se.gritacademy.lageruthyrningexamen.dto;

import java.time.LocalDate;
import java.util.List;

public class CreateBookingRequest {

    private List<Long> storageUnitIds;
    private LocalDate startDate;
    private LocalDate endDate;

    public CreateBookingRequest() {}

    public List<Long> getStorageUnitIds() {
        return storageUnitIds;
    }

    public void setStorageUnitIds(List<Long> storageUnitIds) {
        this.storageUnitIds = storageUnitIds;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}

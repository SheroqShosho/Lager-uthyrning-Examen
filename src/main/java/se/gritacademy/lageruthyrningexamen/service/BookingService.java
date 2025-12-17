package se.gritacademy.lageruthyrningexamen.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.gritacademy.lageruthyrningexamen.domain.StorageUnit;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;

import java.time.LocalDate;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public boolean isStorageUnitAvailable(StorageUnit unit, LocalDate startDate, LocalDate endDate) {

        boolean overlapExists = bookingRepository.existsOverlappingBookingForUnit(
                unit.getId(),
                startDate,
                endDate
        );
        return !overlapExists;
    }
}

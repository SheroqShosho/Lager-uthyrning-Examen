package se.gritacademy.lageruthyrningexamen.servicetest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import se.gritacademy.lageruthyrningexamen.domain.Booking;
import se.gritacademy.lageruthyrningexamen.domain.BookingItem;
import se.gritacademy.lageruthyrningexamen.domain.StorageUnit;
import se.gritacademy.lageruthyrningexamen.domain.User;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;
import se.gritacademy.lageruthyrningexamen.service.BookingService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@DisplayName("Booking availability service tests")
public class BookingServiceAvailabilityTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageUnitRepository storageUnitRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Should return false when a storage unit is already booked for overlapping dates")
    void shouldReturnFalseWhenOverlappingBookingExists() {
        //Arrange
        User user = userRepository.save(new User(
                null,
                "u2@example.com",
                "hashed",
                "User Two",
                "CUSTOMER",
                null
        ));


    StorageUnit unit = storageUnitRepository.save(new StorageUnit(
            null,
            "A1",
            "Unit",
            new BigDecimal("5.00"),
            new BigDecimal("99.00"),
            "Gbg",
            true,
            null
    ));

    Booking existing = new Booking(
            user,
            LocalDate.of(2026, 1, 10),
            LocalDate.of(2026, 1, 15),
            new BigDecimal("495.00"),
            "PAID"
    );

    existing.addItem(new BookingItem(unit, new BigDecimal("99.00")));
    bookingRepository.save(existing);

    BookingService bookingService = new BookingService(bookingRepository);

    // Act (overlappar: 12-14 ligger inne i 10-15)
    boolean available = bookingService.isStorageUnitAvailable(
            unit,
            LocalDate.of(2026, 1, 12),
            LocalDate.of(2026, 1, 14)
    );

    // Assert
    assertFalse(available);

    }
}


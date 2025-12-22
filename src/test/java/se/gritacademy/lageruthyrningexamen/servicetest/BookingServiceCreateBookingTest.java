package se.gritacademy.lageruthyrningexamen.servicetest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import se.gritacademy.lageruthyrningexamen.domain.Booking;
import se.gritacademy.lageruthyrningexamen.domain.BookingItem;
import se.gritacademy.lageruthyrningexamen.domain.StorageUnit;
import se.gritacademy.lageruthyrningexamen.domain.User;
import se.gritacademy.lageruthyrningexamen.exception.StorageUnitUnavailableException;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;
import se.gritacademy.lageruthyrningexamen.service.BookingService;
import se.gritacademy.lageruthyrningexamen.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Booking createBooking service tests")
public class BookingServiceCreateBookingTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageUnitRepository storageUnitRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Should create a pending booking with items and correct total price")
    void shouldCreateBookingWithCorrectTotalPrice() {
        User user = userRepository.save(new User(
                null,
                "u#@example.com",
                "hashed",
                "User Three",
                "CUSTOMER",
                null
        ));

        StorageUnit unit1 = storageUnitRepository.save(new StorageUnit(
                null,
                "A1",
                "Unit A1",
                new BigDecimal("5.00"),
                new BigDecimal("100.00"),
                "Gbg",
                true,
                null
        ));

        StorageUnit unit2 = storageUnitRepository.save(new StorageUnit(
                null,
                "B2",
                "Unit B2",
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                "Gbg",
                true,
                null
        ));

        BookingService service = new BookingService(bookingRepository, new PaymentService());

        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 4); // 3 dagar

        Booking booking = service.createBooking(user, List.of(unit1, unit2), start, end);

        assertNotNull(booking.getId());
        assertEquals("PENDING", booking.getStatus());
        assertEquals(2, booking.getItems().size());
        assertEquals(new BigDecimal("450.00"), booking.getTotalPrice()); // (100+50)*3
    }

    @Test
    @DisplayName("Should throw when unit is not available due to overlapping booking")
    void shouldThrowWhenNotAvailable() {
        User user = userRepository.save(new User(
                null,
                "u#@example.com",
                "hashed",
                "User Four",
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

        // Existing PAID booking overlaps
        Booking existing = new Booking(
                user,
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 15),
                new BigDecimal("495.00"),
                "PAID"
        );

        existing.addItem(new BookingItem(unit, new BigDecimal("99.00")));
        bookingRepository.save(existing);

        BookingService service = new BookingService(bookingRepository, new PaymentService());

        assertThrows(StorageUnitUnavailableException.class, () ->
                service.createBooking(
                        user,
                        List.of(unit),
                        LocalDate.of(2026,3,12),
                        LocalDate.of(2026,3,14)

                )
        );
    }
}
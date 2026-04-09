package se.gritacademy.lageruthyrningexamen.servicetest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import se.gritacademy.lageruthyrningexamen.model.Booking;
import se.gritacademy.lageruthyrningexamen.model.BookingItem;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;
import se.gritacademy.lageruthyrningexamen.model.User;
import se.gritacademy.lageruthyrningexamen.dto.CreateBookingRequest;
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
@ActiveProfiles("test")
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

        BookingService service = new BookingService(bookingRepository, storageUnitRepository, new PaymentService());

        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 4); // 3 dagar

        CreateBookingRequest.BookingItemRequest item1 = new CreateBookingRequest.BookingItemRequest();
        item1.setStorageUnitId(unit1.getId());
        item1.setStartDate(start);
        item1.setEndDate(end);

        CreateBookingRequest.BookingItemRequest item2 = new CreateBookingRequest.BookingItemRequest();
        item2.setStorageUnitId(unit2.getId());
        item2.setStartDate(start);
        item2.setEndDate(end);

        Booking booking = service.createBooking(user, List.of(item1, item2));

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

        BookingService service = new BookingService(bookingRepository, storageUnitRepository, new PaymentService());

        CreateBookingRequest.BookingItemRequest item = new CreateBookingRequest.BookingItemRequest();
        item.setStorageUnitId(unit.getId());
        item.setStartDate(LocalDate.of(2026, 3, 12));
        item.setEndDate(LocalDate.of(2026, 3, 14));

        assertThrows(StorageUnitUnavailableException.class, () ->
                service.createBooking(user, List.of(item))
        );
    }
}

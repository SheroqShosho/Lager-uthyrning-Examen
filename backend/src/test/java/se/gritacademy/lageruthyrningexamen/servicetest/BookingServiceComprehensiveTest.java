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
@DisplayName("BookingService comprehensive tests")
public class BookingServiceComprehensiveTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageUnitRepository storageUnitRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Should get all bookings for user")
    void shouldGetAllBookingsForUser() {
        User user = userRepository.save(new User(
            null, "user@test.com", "hashed", "Test User", "CUSTOMER", null
        ));

        StorageUnit unit = storageUnitRepository.save(new StorageUnit(
            null, "A1", "Unit", new BigDecimal("5.00"), new BigDecimal("100.00"), "Gbg", true, null
        ));

        Booking booking1 = new Booking(user, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5), new BigDecimal("500.00"), "PAID");
        booking1.addItem(new BookingItem(unit, new BigDecimal("100.00")));
        bookingRepository.save(booking1);

        Booking booking2 = new Booking(user, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 5), new BigDecimal("500.00"), "PENDING");
        booking2.addItem(new BookingItem(unit, new BigDecimal("100.00")));
        bookingRepository.save(booking2);

        BookingService service = new BookingService(bookingRepository, storageUnitRepository, new PaymentService());
        List<Booking> bookings = service.getBookingsByUserId(user.getId());

        assertEquals(2, bookings.size());
    }

    @Test
    @DisplayName("Should create booking with correct total price for multiple days")
    void shouldCalculateCorrectTotalPriceForMultipleDays() {
        User user = userRepository.save(new User(
            null, "user2@test.com", "hashed", "Test User 2", "CUSTOMER", null
        ));

        StorageUnit unit1 = storageUnitRepository.save(new StorageUnit(
            null, "B1", "Unit B", new BigDecimal("5.00"), new BigDecimal("100.00"), "Gbg", true, null
        ));

        StorageUnit unit2 = storageUnitRepository.save(new StorageUnit(
            null, "B2", "Unit C", new BigDecimal("10.00"), new BigDecimal("150.00"), "Gbg", true, null
        ));

        CreateBookingRequest.BookingItemRequest item1 = new CreateBookingRequest.BookingItemRequest();
        item1.setStorageUnitId(unit1.getId());
        item1.setStartDate(LocalDate.of(2026, 3, 1));
        item1.setEndDate(LocalDate.of(2026, 3, 6)); // 5 dagar

        CreateBookingRequest.BookingItemRequest item2 = new CreateBookingRequest.BookingItemRequest();
        item2.setStorageUnitId(unit2.getId());
        item2.setStartDate(LocalDate.of(2026, 3, 1));
        item2.setEndDate(LocalDate.of(2026, 3, 6)); // 5 dagar

        BookingService service = new BookingService(bookingRepository, storageUnitRepository, new PaymentService());
        Booking booking = service.createBooking(user, List.of(item1, item2));

        // (100 + 150) * 5 = 1250
        assertEquals(new BigDecimal("1250.00"), booking.getTotalPrice());
    }

    @Test
    @DisplayName("Should update booking status to PAID with payment reference")
    void shouldUpdateBookingToPaidWithReference() {
        User user = userRepository.save(new User(
            null, "user3@test.com", "hashed", "Test User 3", "CUSTOMER", null
        ));

        StorageUnit unit = storageUnitRepository.save(new StorageUnit(
            null, "C1", "Unit", new BigDecimal("5.00"), new BigDecimal("100.00"), "Gbg", true, null
        ));

        Booking booking = new Booking(user, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5), new BigDecimal("400.00"), "PENDING");
        booking.addItem(new BookingItem(unit, new BigDecimal("100.00")));
        booking = bookingRepository.save(booking);

        BookingService service = new BookingService(bookingRepository, storageUnitRepository, new PaymentService());
        Booking paid = service.markBookingAsPaid(booking.getId());

        assertEquals("PAID", paid.getStatus());
        assertNotNull(paid.getPaymentRef());
        assertTrue(paid.getPaymentRef().startsWith("PAY-"));
    }

    @Test
    @DisplayName("Should check storage unit availability correctly")
    void shouldCheckAvailabilityCorrectly() {
        User user = userRepository.save(new User(
            null, "user4@test.com", "hashed", "Test User 4", "CUSTOMER", null
        ));

        StorageUnit unit = storageUnitRepository.save(new StorageUnit(
            null, "D1", "Unit", new BigDecimal("5.00"), new BigDecimal("100.00"), "Gbg", true, null
        ));

        // Create a PAID booking (will block availability)
        Booking paid = new Booking(user, LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 15), new BigDecimal("500.00"), "PAID");
        paid.addItem(new BookingItem(unit, new BigDecimal("100.00")));
        bookingRepository.save(paid);

        BookingService service = new BookingService(bookingRepository, storageUnitRepository, new PaymentService());

        // Check: overlapping dates should be unavailable
        boolean unavailable = service.isStorageUnitAvailable(unit, LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 14));
        assertFalse(unavailable);

        // Check: non-overlapping dates should be available
        boolean available = service.isStorageUnitAvailable(unit, LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 25));
        assertTrue(available);
    }

    @Test
    @DisplayName("Should reject booking when unit unavailable")
    void shouldRejectBookingWhenUnitUnavailable() {
        User user = userRepository.save(new User(
            null, "user5@test.com", "hashed", "Test User 5", "CUSTOMER", null
        ));

        StorageUnit unit = storageUnitRepository.save(new StorageUnit(
            null, "E1", "Unit", new BigDecimal("5.00"), new BigDecimal("100.00"), "Gbg", true, null
        ));

        // Block dates with PAID booking
        Booking blocked = new Booking(user, LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 15), new BigDecimal("500.00"), "PAID");
        blocked.addItem(new BookingItem(unit, new BigDecimal("100.00")));
        bookingRepository.save(blocked);

        BookingService service = new BookingService(bookingRepository, storageUnitRepository, new PaymentService());

        // Try to book overlapping dates
        CreateBookingRequest.BookingItemRequest item = new CreateBookingRequest.BookingItemRequest();
        item.setStorageUnitId(unit.getId());
        item.setStartDate(LocalDate.of(2026, 6, 12));
        item.setEndDate(LocalDate.of(2026, 6, 14));

        assertThrows(Exception.class, () -> service.createBooking(user, List.of(item)));
    }
}


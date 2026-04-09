package se.gritacademy.lageruthyrningexamen.repositorytest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import se.gritacademy.lageruthyrningexamen.model.Booking;
import se.gritacademy.lageruthyrningexamen.model.BookingItem;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;
import se.gritacademy.lageruthyrningexamen.model.User;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Booking repository tests")
public class BookingRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageUnitRepository storageUnitRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Should save booking with items and find by user id")
    void shouldSaveBookingWithItemsAndFindByUserId() {
        User user = new User(
                null,
                "u1@example.com",
                "hashed",
                "User One",
                "CUSTOMER",
                null
        );

        user = userRepository.save(user);

        StorageUnit unit = new StorageUnit(
                null,
                "A1",
                "Unit",
                new BigDecimal("5.00"),
                new BigDecimal("99.00"),
                "Gbg",
                true,
                null
        );
        unit = storageUnitRepository.save(unit);


        Booking booking = new Booking(
                user,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 7),
                new BigDecimal("594.00"),
                "PAID"
        );

        booking.addItem(new BookingItem(unit, new BigDecimal("99.00")));

        bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findByUserId(user.getId());
        assertEquals(1, bookings.size());
        assertEquals(1, bookings.get(0).getItems().size());
        assertEquals("PAID", bookings.get(0).getStatus());

    }

    @Test
    @DisplayName("Should retrieve multiple bookings for user")
    void shouldRetrieveMultipleBookingsForUser() {
        User user = userRepository.save(new User(null, "multi@test.com", "pass", "Multi", "CUSTOMER", Instant.now()));
        
        for (int i = 0; i < 3; i++) {
            Booking booking = new Booking(
                user, 
                LocalDate.of(2026, 1, 1 + i * 10), 
                LocalDate.of(2026, 1, 5 + i * 10), 
                new BigDecimal("500.00"), 
                "PENDING"
            );
            bookingRepository.save(booking);
        }
        
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());
        assertEquals(3, bookings.size());
    }

    @Test
    @DisplayName("Should filter bookings by different users")
    void shouldFilterBookingsByDifferentUsers() {
        User user1 = userRepository.save(new User(null, "user1@test.com", "pass", "User 1", "CUSTOMER", Instant.now()));
        User user2 = userRepository.save(new User(null, "user2@test.com", "pass", "User 2", "CUSTOMER", Instant.now()));
        
        Booking booking1 = new Booking(user1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5), new BigDecimal("500"), "PAID");
        Booking booking2 = new Booking(user2, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 5), new BigDecimal("600"), "PENDING");
        
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        
        List<Booking> user1Bookings = bookingRepository.findByUserId(user1.getId());
        List<Booking> user2Bookings = bookingRepository.findByUserId(user2.getId());
        
        assertEquals(1, user1Bookings.size());
        assertEquals(1, user2Bookings.size());
        assertEquals("PAID", user1Bookings.get(0).getStatus());
        assertEquals("PENDING", user2Bookings.get(0).getStatus());
    }

    @Test
    @DisplayName("Should save booking with payment reference")
    void shouldSaveBookingWithPaymentReference() {
        User user = userRepository.save(new User(null, "payment@test.com", "pass", "Payment", "CUSTOMER", Instant.now()));
        Booking booking = new Booking(user, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), new BigDecimal("1000"), "PAID");
        booking.setPaymentRef("PAY-12345");
        
        Booking saved = bookingRepository.save(booking);
        assertEquals("PAY-12345", saved.getPaymentRef());
    }

    @Test
    @DisplayName("Should save booking with different statuses")
    void shouldSaveBookingWithDifferentStatuses() {
        User user = userRepository.save(new User(null, "status@test.com", "pass", "Status", "CUSTOMER", Instant.now()));
        
        Booking pendingBooking = new Booking(user, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5), new BigDecimal("500"), "PENDING");
        Booking paidBooking = new Booking(user, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5), new BigDecimal("500"), "PAID");
        Booking cancelledBooking = new Booking(user, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5), new BigDecimal("500"), "CANCELLED");
        
        bookingRepository.save(pendingBooking);
        bookingRepository.save(paidBooking);
        bookingRepository.save(cancelledBooking);
        
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());
        assertEquals(3, bookings.size());
    }

    @Test
    @DisplayName("Should persist booking with multiple items")
    void shouldPersistBookingWithMultipleItems() {
        User user = userRepository.save(new User(null, "items@test.com", "pass", "Items", "CUSTOMER", Instant.now()));
        StorageUnit unit1 = storageUnitRepository.save(new StorageUnit(null, "U1", null, new BigDecimal("10"), new BigDecimal("100"), "Loc1", true, Instant.now()));
        StorageUnit unit2 = storageUnitRepository.save(new StorageUnit(null, "U2", null, new BigDecimal("20"), new BigDecimal("200"), "Loc2", true, Instant.now()));
        
        Booking booking = new Booking(user, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5), new BigDecimal("1000"), "PENDING");
        booking.addItem(new BookingItem(unit1, new BigDecimal("100")));
        booking.addItem(new BookingItem(unit2, new BigDecimal("200")));
        
        Booking saved = bookingRepository.save(booking);
        assertEquals(2, saved.getItems().size());
    }

    @Test
    @DisplayName("Should update booking status")
    void shouldUpdateBookingStatus() {
        User user = userRepository.save(new User(null, "updatestatus@test.com", "pass", "Update", "CUSTOMER", Instant.now()));
        Booking booking = new Booking(user, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5), new BigDecimal("500"), "PENDING");
        
        Booking saved = bookingRepository.save(booking);
        saved.setStatus("PAID");
        Booking updated = bookingRepository.save(saved);
        
        assertEquals("PAID", updated.getStatus());
    }
}

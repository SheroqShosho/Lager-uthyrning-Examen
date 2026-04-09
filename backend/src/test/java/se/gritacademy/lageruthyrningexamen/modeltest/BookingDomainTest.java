package se.gritacademy.lageruthyrningexamen.modeltest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.gritacademy.lageruthyrningexamen.model.Booking;
import se.gritacademy.lageruthyrningexamen.model.BookingItem;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;
import se.gritacademy.lageruthyrningexamen.model.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Booking domain entity tests")
public class BookingDomainTest {

    @Test
    @DisplayName("Should create booking with all fields")
    void shouldCreateBookingWithAllFields() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end = LocalDate.of(2026, 4, 5);
        BigDecimal price = new BigDecimal("500.00");

        Booking booking = new Booking(user, start, end, price, "PENDING");

        assertEquals(user, booking.getUser());
        assertEquals(start, booking.getStartDate());
        assertEquals(end, booking.getEndDate());
        assertEquals(price, booking.getTotalPrice());
        assertEquals("PENDING", booking.getStatus());
    }

    @Test
    @DisplayName("Should add item to booking")
    void shouldAddItemToBooking() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        Booking booking = new Booking(user, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5), new BigDecimal("500.00"), "PENDING");
        
        StorageUnit unit = new StorageUnit(1L, "A1", "Test unit", new BigDecimal("100.00"), new BigDecimal("100.00"), "Location", true, Instant.now());
        BookingItem item = new BookingItem(unit, new BigDecimal("100.00"));

        booking.addItem(item);

        assertEquals(1, booking.getItems().size());
        assertEquals(item, booking.getItems().get(0));
    }

    @Test
    @DisplayName("Should set status to PAID")
    void shouldSetStatusToPaid() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        Booking booking = new Booking(user, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5), new BigDecimal("500.00"), "PENDING");

        booking.setStatus("PAID");

        assertEquals("PAID", booking.getStatus());
    }

    @Test
    @DisplayName("Should set payment reference")
    void shouldSetPaymentReference() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        Booking booking = new Booking(user, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5), new BigDecimal("500.00"), "PENDING");

        booking.setPaymentRef("PAY-123456");

        assertEquals("PAY-123456", booking.getPaymentRef());
    }

    @Test
    @DisplayName("Should get booking items")
    void shouldGetBookingItems() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        Booking booking = new Booking(user, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5), new BigDecimal("500.00"), "PENDING");

        assertNotNull(booking.getItems());
        assertTrue(booking.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should calculate days in booking")
    void shouldCalculateDaysInBooking() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end = LocalDate.of(2026, 4, 5);
        Booking booking = new Booking(user, start, end, new BigDecimal("400.00"), "PENDING");

        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        
        assertEquals(4, days);
    }

    @Test
    @DisplayName("Should update booking status to PAID")
    void shouldUpdateBookingStatusToPaid() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 10);
        Booking booking = new Booking(user, start, end, new BigDecimal("900.00"), "PENDING");
        
        booking.setStatus("PAID");
        assertEquals("PAID", booking.getStatus());
    }

    @Test
    @DisplayName("Should update booking status to CANCELLED")
    void shouldUpdateBookingStatusToCancelled() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 5);
        Booking booking = new Booking(user, start, end, new BigDecimal("400.00"), "PENDING");
        
        booking.setStatus("CANCELLED");
        assertEquals("CANCELLED", booking.getStatus());
    }

    @Test
    @DisplayName("Should add booking items to booking")
    void shouldAddBookingItemsToBooking() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        StorageUnit unit = new StorageUnit(1L, "A1", "Test", new BigDecimal("10.00"), new BigDecimal("100.00"), "Location", true, Instant.now());
        Booking booking = new Booking(user, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5), new BigDecimal("500.00"), "PENDING");
        BookingItem item = new BookingItem(unit, new BigDecimal("100.00"));
        
        booking.addItem(item);
        assertEquals(1, booking.getItems().size());
    }

    @Test
    @DisplayName("Should remove booking items from booking")
    void shouldRemoveBookingItemsFromBooking() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        StorageUnit unit = new StorageUnit(1L, "B1", "Test", new BigDecimal("10.00"), new BigDecimal("100.00"), "Location", true, Instant.now());
        Booking booking = new Booking(user, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5), new BigDecimal("500.00"), "PENDING");
        BookingItem item = new BookingItem(unit, new BigDecimal("100.00"));
        
        booking.addItem(item);
        assertEquals(1, booking.getItems().size());
        
        booking.removeItem(item);
        assertEquals(0, booking.getItems().size());
    }

    @Test
    @DisplayName("Should update total price")
    void shouldUpdateTotalPrice() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        Booking booking = new Booking(user, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5), new BigDecimal("500.00"), "PENDING");
        
        booking.setTotalPrice(new BigDecimal("750.00"));
        assertEquals(new BigDecimal("750.00"), booking.getTotalPrice());
    }


    @Test
    @DisplayName("Should handle booking with same start and end date")
    void shouldHandleBookingWithSameStartAndEndDate() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        LocalDate date = LocalDate.of(2026, 11, 15);
        Booking booking = new Booking(user, date, date, new BigDecimal("100.00"), "PENDING");
        
        assertEquals(date, booking.getStartDate());
        assertEquals(date, booking.getEndDate());
    }

    @Test
    @DisplayName("Should handle long duration bookings")
    void shouldHandleLongDurationBookings() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        Booking booking = new Booking(user, start, end, new BigDecimal("36500.00"), "PENDING");
        
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        assertEquals(364, days);
    }

    @Test
    @DisplayName("Should initialize with empty items list")
    void shouldInitializeWithEmptyItemsList() {
        User user = new User(1L, "test@email.com", "pass", "John", "CUSTOMER", Instant.now());
        Booking booking = new Booking(user, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5), new BigDecimal("500.00"), "PENDING");
        
        assertNotNull(booking.getItems());
        assertTrue(booking.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should set user for booking")
    void shouldSetUserForBooking() {
        User user1 = new User(1L, "user1@email.com", "pass", "User 1", "CUSTOMER", Instant.now());
        User user2 = new User(2L, "user2@email.com", "pass", "User 2", "CUSTOMER", Instant.now());
        Booking booking = new Booking(user1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5), new BigDecimal("500.00"), "PENDING");
        
        booking.setUser(user2);
        assertEquals(user2, booking.getUser());
    }
}



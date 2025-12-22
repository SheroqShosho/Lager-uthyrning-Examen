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
import se.gritacademy.lageruthyrningexamen.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Booking payment service tests")
public class BookingServicePaymentTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageUnitRepository storageUnitRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Should mark booking as PAID and set payment reference")
    void shouldMarkBookingAsPaid() {
        User user = userRepository.save(new User(
                null,
                "pay@example.com",
                "hashed",
                "Pay User",
                "CUSTOMER",
                null
        ));

        StorageUnit unit = storageUnitRepository.save(new StorageUnit(
                null,
                "A1",
                "Unit",
                new BigDecimal("5.00"),
                new BigDecimal("100.00"),
                "Gbg",
                true,
                null
        ));

        Booking booking = new Booking(
                user,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 4),
                new BigDecimal("300.00"),
                "PENDING"
        );
        booking.addItem(new BookingItem(unit, new BigDecimal("100.00")));
        booking = bookingRepository.save(booking);

        BookingService service = new BookingService(bookingRepository, new PaymentService());

        Booking paid = service.markBookingAsPaid(booking.getId());

        assertEquals("PAID", paid.getStatus());
        assertNotNull(paid.getPaymentRef());
        assertTrue(paid.getPaymentRef().startsWith("PAY"));
    }
}

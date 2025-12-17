package se.gritacademy.lageruthyrningexamen.repositorytest;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
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
}

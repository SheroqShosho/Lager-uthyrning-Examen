package se.gritacademy.lageruthyrningexamen.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.gritacademy.lageruthyrningexamen.domain.Booking;
import se.gritacademy.lageruthyrningexamen.domain.BookingItem;
import se.gritacademy.lageruthyrningexamen.domain.StorageUnit;
import se.gritacademy.lageruthyrningexamen.domain.User;
import se.gritacademy.lageruthyrningexamen.exception.StorageUnitUnavailableException;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;

    public BookingService(BookingRepository bookingRepository, PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.paymentService = paymentService;
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
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

    @Transactional
    public Booking createBooking(User user, List<StorageUnit> units, LocalDate startDate, LocalDate endDate ) {
        if (user == null) throw new IllegalArgumentException("user is required");
        if (units == null || units.isEmpty()) throw new IllegalArgumentException("units is required");
        if (startDate == null || endDate == null) throw new IllegalArgumentException("startDate or endDate are required");
        if (!endDate.isAfter(startDate)) throw new IllegalArgumentException("endDate must be after startDate");

        // 1) availability check per unit
        for (StorageUnit unit : units) {
            boolean available = isStorageUnitAvailable(unit, startDate, endDate);
            if (!available) {
                throw new StorageUnitUnavailableException("Storage unit is not available: " + unit.getId());

            }
        }

        // 2) price calc (antal dygn)
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        BigDecimal total = BigDecimal.ZERO;

        Booking booking = new Booking(user, startDate, endDate, BigDecimal.ZERO, "PENDING");

        for (StorageUnit unit : units) {
            BigDecimal pricePerDay = unit.getPricePerDay();
            if(pricePerDay == null) throw new IllegalArgumentException("pricePerDay is required for unit: " + unit.getId());

            total = total.add(pricePerDay.multiply(BigDecimal.valueOf(days)));
            booking.addItem(new BookingItem(unit, pricePerDay));
        }

        booking.setTotalPrice(total);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking markBookingAsPaid(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        long amountInCents = booking.getTotalPrice()
                .multiply(java.math.BigDecimal.valueOf(100))
                .longValueExact();

        String paymentRef = paymentService.mockPay(amountInCents);

        booking.setStatus("PAID");
        booking.setPaymentRef(paymentRef);

        return bookingRepository.save(booking);
    }

}

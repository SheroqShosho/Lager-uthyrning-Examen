package se.gritacademy.lageruthyrningexamen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.gritacademy.lageruthyrningexamen.exception.ResourceNotFoundException;
import se.gritacademy.lageruthyrningexamen.domain.Booking;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final BookingRepository bookingRepository;

    public PaymentController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @PostMapping("/bookings/{bookingId}")
    public ResponseEntity<?> pay(@PathVariable Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        booking.setStatus("CONFIRMED");
        booking.setPaymentRef("PAY-" + UUID.randomUUID());

        bookingRepository.save(booking);

        return ResponseEntity.ok(Map.of(
                "bookingId", booking.getId(),
                "status", booking.getStatus(),
                "paymentRef", booking.getPaymentRef(),
                "timestamp", Instant.now().toString()
        ));
    }
}

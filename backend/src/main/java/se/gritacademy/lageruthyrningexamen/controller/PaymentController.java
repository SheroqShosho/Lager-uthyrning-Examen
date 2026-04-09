package se.gritacademy.lageruthyrningexamen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.gritacademy.lageruthyrningexamen.model.Booking;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;
import se.gritacademy.lageruthyrningexamen.service.BookingService;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    public PaymentController(BookingService bookingService, BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
    }

    // Markera en bokning som betald och returnera bekräftelse med betalningsreferens
    @PostMapping("/bookings/{bookingId}")
    public ResponseEntity<?> pay(@PathVariable Long bookingId) {
        Booking paid = bookingService.markBookingAsPaid(bookingId);

        return ResponseEntity.ok(Map.of(
                "bookingId", paid.getId(),
                "status", paid.getStatus(),
                "paymentRef", paid.getPaymentRef(),
                "timestamp", Instant.now().toString()
        ));
    }
}

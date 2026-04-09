package se.gritacademy.lageruthyrningexamen.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.gritacademy.lageruthyrningexamen.model.Booking;
import se.gritacademy.lageruthyrningexamen.model.User;
import se.gritacademy.lageruthyrningexamen.dto.CreateBookingRequest;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;
import se.gritacademy.lageruthyrningexamen.service.BookingService;
import se.gritacademy.lageruthyrningexamen.security.AuthUtil;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    // Hämta alla bokningar för den inloggade användaren
    @GetMapping("/my")
    public ResponseEntity<?> myBookings() {
        Long userId = AuthUtil.currentUserId();
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    // Skapa en ny bokning för användaren genom att validera lagerutrymmen och beräkna pris
    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        Long userId = AuthUtil.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Booking booking = bookingService.createBooking(user, request.getItems());
        return ResponseEntity.ok(booking);
    }
}
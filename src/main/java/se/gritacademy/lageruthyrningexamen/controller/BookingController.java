package se.gritacademy.lageruthyrningexamen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.gritacademy.lageruthyrningexamen.domain.Booking;
import se.gritacademy.lageruthyrningexamen.domain.StorageUnit;
import se.gritacademy.lageruthyrningexamen.domain.User;
import se.gritacademy.lageruthyrningexamen.dto.CreateBookingRequest;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;
import se.gritacademy.lageruthyrningexamen.service.BookingService;
import se.gritacademy.lageruthyrningexamen.security.AuthUtil;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final StorageUnitRepository storageUnitRepository;

    public BookingController(BookingService bookingService,
                             BookingRepository bookingRepository,
                             UserRepository userRepository,
                             StorageUnitRepository storageUnitRepository)
    {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.storageUnitRepository = storageUnitRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<?> myBookings() {
        Long userId = AuthUtil.currentUserId();
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    @PostMapping
    public Booking createBooking(@RequestBody CreateBookingRequest request) {

        Long userId = AuthUtil.currentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<StorageUnit> units =
                storageUnitRepository.findAllById(request.getStorageUnitIds());

        return bookingService.createBooking(
                user,
                units,
                request.getStartDate(),
                request.getEndDate()
        );
    }






}

package se.gritacademy.lageruthyrningexamen.controller;

import org.springframework.web.bind.annotation.*;
import se.gritacademy.lageruthyrningexamen.domain.Booking;
import se.gritacademy.lageruthyrningexamen.domain.StorageUnit;
import se.gritacademy.lageruthyrningexamen.domain.User;
import se.gritacademy.lageruthyrningexamen.dto.CreateBookingRequest;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;
import se.gritacademy.lageruthyrningexamen.service.BookingService;

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

    @GetMapping("/user/{userId}")
    public List<Booking> getBookingsForUser(@PathVariable Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @PostMapping
    public Booking createBooking(@RequestBody CreateBookingRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        List<StorageUnit> units = storageUnitRepository.findAllById(request.getStorageUnitIds());

        return bookingService.createBooking(user, units, request.getStartDate(), request.getEndDate());
    }


}

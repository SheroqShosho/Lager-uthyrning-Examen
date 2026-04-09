package se.gritacademy.lageruthyrningexamen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.gritacademy.lageruthyrningexamen.controller.StorageUnitController;
import se.gritacademy.lageruthyrningexamen.model.Booking;
import se.gritacademy.lageruthyrningexamen.model.BookingItem;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;
import se.gritacademy.lageruthyrningexamen.model.User;
import se.gritacademy.lageruthyrningexamen.dto.CreateBookingRequest;
import se.gritacademy.lageruthyrningexamen.exception.StorageUnitUnavailableException;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(StorageUnitController.class);
    private final BookingRepository bookingRepository;
    private final StorageUnitRepository storageUnitRepository;
    private final PaymentService paymentService;

    public BookingService(BookingRepository bookingRepository,
                          StorageUnitRepository storageUnitRepository,
                          PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.storageUnitRepository = storageUnitRepository;
        this.paymentService = paymentService;
    }

    // Hämta alla bokningar för en specifik användare och ladda relaterade lagerutrymmen
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserId(Long userId) {
        logger.info("Hämtar alla bokningar för användar-ID: {}", userId);
        
        // Hämta alla bokningar som tillhör denna användare
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        logger.debug("Antal bokningar hittade för användar-ID {}: {}", userId, bookings.size());
        
        // Loopa genom alla bokningar för att tvinga laddning av relaterade lagerutrymmen
        bookings.forEach(b -> {
            // Loopa genom alla bokningsitems i denna bokning
            b.getItems().forEach(item -> {
                // Kontrollera om bokningsitemen har ett associerat lagertruymme
                if (item.getStorageUnit() != null) {
                    // Tvinga laddning av lagerutrymmet (prevent lazy loading issues)
                    item.getStorageUnit().getName();
                    logger.trace("Lagerutrymme laddat för bokningsitem: {}", item.getId());
                }
            });
        });
        
        logger.info("Bokningslista med laderutrymmen laddad, returnerar {} bokningar", bookings.size());
        return bookings;
    }

    // Kontrollera om ett lagerutrymme är tillgängligt under önskad datumperiod
    @Transactional(readOnly = true)
    public boolean isStorageUnitAvailable(StorageUnit unit, LocalDate startDate, LocalDate endDate) {
        return !bookingRepository.existsOverlappingBookingForUnit(unit.getId(), startDate, endDate);
    }

    // Skapa en ny bokning genom att validera lagerutrymmen, beräkna totalpris och spara
    @Transactional
    public Booking createBooking(User user, List<CreateBookingRequest.BookingItemRequest> itemRequests) {
        if (user == null) throw new IllegalArgumentException("user is required");
        if (itemRequests == null || itemRequests.isEmpty()) throw new IllegalArgumentException("items are required");

        // Vi sätter bokningens huvuddatum till det första objektets datum
        LocalDate firstStart = itemRequests.get(0).getStartDate();
        LocalDate firstEnd = itemRequests.get(0).getEndDate();

        Booking booking = new Booking(user, firstStart, firstEnd, BigDecimal.ZERO, "PENDING");
        BigDecimal total = BigDecimal.ZERO;

        // Loopa genom varje bokningsförfrågan för att validera och beräkna priser
        for (CreateBookingRequest.BookingItemRequest req : itemRequests) {
            // Hämta lagerutrymmet från databasen baserat på ID
            StorageUnit unit = storageUnitRepository.findById(req.getStorageUnitId())
                    .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + req.getStorageUnitId()));

            // Kontrollera om lagerutrymmet är tillgängligt under den angivna perioden
            if (!isStorageUnitAvailable(unit, req.getStartDate(), req.getEndDate())) {
                throw new StorageUnitUnavailableException("Unit " + unit.getName() + " is not available.");
            }

            // Beräkna antalet dagar mellan start- och slutdatum
            long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate());
            
            // Validera att slutdatumet är efter startdatumet
            if (days <= 0) throw new IllegalArgumentException("End date must be after start date for unit: " + unit.getName());

            // Beräkna priset för detta bokningsitem (pris per dag * antal dagar)
            BigDecimal itemPrice = unit.getPricePerDay().multiply(BigDecimal.valueOf(days));
            total = total.add(itemPrice);

            // Lägg till detta bokningsitem till huvudbokningen
            booking.addItem(new BookingItem(unit, unit.getPricePerDay()));
        }

        // Sätt det totala priset för hela bokningen
        booking.setTotalPrice(total);
        return bookingRepository.save(booking);
    }

    // Markera en bokning som betald och generera en unik betalningsreferens
    @Transactional
    public Booking markBookingAsPaid(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
        booking.setStatus("PAID");
        booking.setPaymentRef("PAY-" + bookingId + "-" + System.currentTimeMillis());
        return bookingRepository.save(booking);
    }
}
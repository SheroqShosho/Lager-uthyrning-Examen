package se.gritacademy.lageruthyrningexamen.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import se.gritacademy.lageruthyrningexamen.model.Booking;
import se.gritacademy.lageruthyrningexamen.model.BookingItem;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/storage-units")
public class StorageUnitController {

    private static final Logger logger = LoggerFactory.getLogger(StorageUnitController.class);
    private final StorageUnitRepository storageUnitRepository;

    public StorageUnitController(StorageUnitRepository storageUnitRepository) {
        this.storageUnitRepository = storageUnitRepository;
    }

    // Hämta alla aktiva lagerutrymmen med deras bokningar och returneradetaljer i DTO-format
    @GetMapping
    @Transactional(readOnly = true)
    public List<StorageUnitDto> listActiveUnits() {
        logger.info("API-anrop: Hämtar alla aktiva lagerenheter...");
        List<StorageUnit> units = storageUnitRepository.findByActiveTrue();
        logger.debug("Totalt antal aktiva lagerenheter hittade: {}", units.size());

        // Loopa genom alla lagerutrymmen och konvertera till DTO-format
        List<StorageUnitDto> dtos = units.stream().map(u -> {
            int count = u.getBookingItems().size();
            logger.debug("Behandlar enhet: {} (ID: {}) med {} boknings-items", u.getName(), u.getId(), count);

            // Skapa DTO-objekt och kopiera grundläggande information från enheten
            StorageUnitDto dto = new StorageUnitDto();
            dto.id = u.getId();
            dto.name = u.getName();
            dto.description = u.getDescription();
            dto.sizeM2 = u.getSizeM2();
            dto.pricePerDay = u.getPricePerDay();
            dto.location = u.getLocation();
            dto.active = u.isActive();

            // Loopa genom alla bokningsitems för denna enhet och konvertera dem till DTO
            dto.bookingItems = u.getBookingItems().stream().map(item -> {
                BookingItemDto bi = new BookingItemDto();
                bi.id = item.getId();
                Booking b = item.getBooking();
                
                // Om bokningsitemen har en kopplad bokning, lägg till dess information
                if (b != null) {
                    BookingRef br = new BookingRef();
                    br.id = b.getId();
                    br.startDate = b.getStartDate();
                    br.endDate = b.getEndDate();
                    bi.booking = br;
                    logger.trace("Bokningsitem {} har bokning {} från {} till {}", item.getId(), b.getId(), b.getStartDate(), b.getEndDate());
                }
                return bi;
            }).collect(Collectors.toList());

            return dto;
        }).collect(Collectors.toList());

        logger.info("Returnerar {} lagerenheter i DTO-format", dtos.size());
        return dtos;
    }

    // Skapa ett nytt lagerutrymme (endast admin) genom att validera behörighet och spara till databas
    @PostMapping
    @Transactional
    public ResponseEntity<?> createStorageUnit(@RequestBody CreateStorageUnitRequest request) {
        logger.info("Försök att skapa nytt lagerutrymme: {}", request.name);
        
        // Hämta autentiseringsinformationen från säkerhetskontexten
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Kontrollera om användaren är autentiserad - om inte eller är anonym, returnera 401
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            logger.warn("Försok att skapa lagerutrymme utan autentisering - returnerar 401");
            return ResponseEntity.status(401).body(new AdminResponse("Unauthorized", null));
        }
        
        // Verifiera att autentiseringen är aktiv
        if (!authentication.isAuthenticated()) {
            logger.warn("Autentisering är inte aktiv - returnerar 401");
            return ResponseEntity.status(401).body(new AdminResponse("Unauthorized", null));
        }
        
        // Loopa genom alla användarens behörigheter för att kontrollera om hen är admin
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        // Om användaren inte har admin-roll, returnera 403 Forbidden
        if (!hasAdminRole) {
            logger.warn("Användare försokade skapa lagerutrymme utan admin-roll - returnerar 403");
            return ResponseEntity.status(403).body(new AdminResponse("Forbidden", null));
        }
        
        logger.info("Admin har godkänd behörighet, fortsätter med att skapa lagerutrymme");
        
        try {
            // Skapa ett nytt StorageUnit-objekt med de angivna värdena
            StorageUnit unit = new StorageUnit(
                null,
                request.name,
                request.description,
                request.sizeM2,
                request.pricePerDay,
                request.location,
                request.active != null ? request.active : true,
                Instant.now()
            );
            logger.debug("StorageUnit-objekt skapat i minnet för: {}", request.name);
            
            // Spara lagerutrymmet till databasen
            StorageUnit saved = storageUnitRepository.save(unit);
            logger.info("Lagerutrymme sparad till databas: {} (ID: {})", saved.getName(), saved.getId());
            
            return ResponseEntity.ok(new AdminResponse("Lagerutrymme skapat!", saved.getId()));
        } catch (Exception e) {
            logger.error("Ett fel uppstod vid skapande av lagerutrymme: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Fel: " + e.getMessage(), null));
        }
    }

    public static class StorageUnitDto {
        public Long id;
        public String name;
        public String description;
        public java.math.BigDecimal sizeM2;
        public java.math.BigDecimal pricePerDay;
        public String location;
        public boolean active;
        public List<BookingItemDto> bookingItems = new ArrayList<>();
    }

    public static class BookingItemDto {
        public Long id;
        public BookingRef booking;
    }

    public static class BookingRef {
        public Long id;
        public java.time.LocalDate startDate;
        public java.time.LocalDate endDate;
    }

    public static class CreateStorageUnitRequest {
        public String name;
        public String description;
        public BigDecimal sizeM2;
        public BigDecimal pricePerDay;
        public String location;
        public Boolean active;
    }

    public static class AdminResponse {
        public String message;
        public Long id;

        public AdminResponse(String message, Long id) {
            this.message = message;
            this.id = id;
        }
    }
}
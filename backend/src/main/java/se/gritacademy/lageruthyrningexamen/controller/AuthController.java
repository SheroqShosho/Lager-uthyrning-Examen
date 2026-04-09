package se.gritacademy.lageruthyrningexamen.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import se.gritacademy.lageruthyrningexamen.model.User;
import se.gritacademy.lageruthyrningexamen.dto.ApiErrorResponse;
import se.gritacademy.lageruthyrningexamen.dto.AuthLoginRequest;
import se.gritacademy.lageruthyrningexamen.dto.AuthRegisterRequest;
import se.gritacademy.lageruthyrningexamen.dto.AuthResponse;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;
import se.gritacademy.lageruthyrningexamen.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(StorageUnitController.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Registrera en ny användare genom att validera e-post, kryptera lösenord och generera JWT-token
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRegisterRequest request) {
        logger.info("Registreringsförsok för e-post: {}", request.getEmail());

        // Kontrollera om e-postadressen redan finns i databasen
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registrering misslyckades: e-post redan i användning - {}", request.getEmail());
            return ResponseEntity.status(409).body(new ApiErrorResponse(409, "Conflict", "Email already exists", "/api/auth/register"));
        }

        // Skapa ett nytt användarkonto
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        
        // Kryptera lösenordet innan det sparas i databasen
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Spara den nya användaren i databasen
        userRepository.save(user);
        logger.info("Ny användare registrerad: {} (ID: {})", request.getEmail(), user.getId());

        // Generera JWT-token för den nyregistrerade användaren
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getEmail()));
    }

    // Autentisera användare med e-post och lösenord, returnera JWT-token vid lyckad inloggning
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthLoginRequest request) {
        logger.info("Inloggningsförsok för e-post: {}", request.getEmail());

        // Söka efter användar med angiven e-postadress i databasen
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        // Om användaren inte finns, returnera 401 Unauthorized
        if (user == null) {
            logger.warn("Inloggningsförsok misslyckades: e-post finns inte - {}", request.getEmail());
            return ResponseEntity.status(401).body(new ApiErrorResponse(401, "Unauthorized", "Invalid credentials", "/api/auth/login"));
        }

        // Validera att det angivna lösenordet matchar det lagrade krypterade lösenordet
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Inloggningsförsok misslyckades: fel lösenord för e-post - {}", request.getEmail());
            return ResponseEntity.status(401).body(new ApiErrorResponse(401, "Unauthorized", "Invalid credentials", "/api/auth/login"));
        }

        // Lösenord är korrekt - generera JWT-token för denna användare
        String token = jwtService.generateToken(user);
        logger.info("Användare inloggad lyckat: {} (ID: {})", request.getEmail(), user.getId());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getEmail()));
    }

    // Hämta information om den aktuellt inloggade användaren från säkerhetskontexten
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            Long userId = Long.parseLong(userIdStr);
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getRole()));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(401).build();
        }
    }

    public static class UserResponse {
        public Long id;
        public String email;
        public String role;

        public UserResponse(Long id, String email, String role) {
            this.id = id;
            this.email = email;
            this.role = role;
        }
    }
}

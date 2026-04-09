package se.gritacademy.lageruthyrningexamen.controllertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import se.gritacademy.lageruthyrningexamen.dto.CreateBookingRequest;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;
import se.gritacademy.lageruthyrningexamen.model.User;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;
import se.gritacademy.lageruthyrningexamen.security.JwtService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("BookingController integration tests")
public class BookingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private StorageUnitRepository storageUnitRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private ObjectMapper objectMapper;

    private String userToken;
    private String otherUserToken;
    private StorageUnit storageUnit;

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        storageUnitRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("booker@test.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFullName("Booker User");
        user.setRole("CUSTOMER");
        user = userRepository.save(user);
        userToken = jwtService.generateToken(user);

        User other = new User();
        other.setEmail("other@test.com");
        other.setPassword(passwordEncoder.encode("password123"));
        other.setFullName("Other User");
        other.setRole("CUSTOMER");
        other = userRepository.save(other);
        otherUserToken = jwtService.generateToken(other);

        storageUnit = new StorageUnit(
                null, "S1", "Test unit", new BigDecimal("10.00"),
                new BigDecimal("100.00"), "Stockholm", true, null
        );
        storageUnit = storageUnitRepository.save(storageUnit);
    }

    // ---- GET /api/bookings/my ----

    @Test
    @DisplayName("Authenticated user can retrieve their (empty) booking list")
    void shouldReturnEmptyListWhenNoBookings() throws Exception {
        mockMvc.perform(get("/api/bookings/my")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Unauthenticated request to /my returns 403")
    void shouldRejectUnauthenticatedMyBookings() throws Exception {
        mockMvc.perform(get("/api/bookings/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User only sees their own bookings, not other users'")
    void shouldOnlyReturnOwnBookings() throws Exception {
        // Create a booking for the main user
        CreateBookingRequest req = buildRequest(storageUnit.getId(), LocalDate.now(), LocalDate.now().plusDays(3));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // The other user should see an empty list
        mockMvc.perform(get("/api/bookings/my")
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // The main user should see their one booking
        mockMvc.perform(get("/api/bookings/my")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ---- POST /api/bookings ----

    @Test
    @DisplayName("Authenticated user can create a booking")
    void shouldCreateBooking() throws Exception {
        CreateBookingRequest req = buildRequest(storageUnit.getId(), LocalDate.now(), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @DisplayName("Unauthenticated request to POST /api/bookings returns 403")
    void shouldRejectUnauthenticatedCreateBooking() throws Exception {
        CreateBookingRequest req = buildRequest(storageUnit.getId(), LocalDate.now(), LocalDate.now().plusDays(3));

        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Created booking appears in /my list")
    void createdBookingAppearsInMyList() throws Exception {
        CreateBookingRequest req = buildRequest(storageUnit.getId(), LocalDate.now(), LocalDate.now().plusDays(2));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/bookings/my")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ---- helper ----

    private CreateBookingRequest buildRequest(Long unitId, LocalDate start, LocalDate end) {
        CreateBookingRequest.BookingItemRequest item = new CreateBookingRequest.BookingItemRequest();
        item.setStorageUnitId(unitId);
        item.setStartDate(start);
        item.setEndDate(end);

        CreateBookingRequest req = new CreateBookingRequest();
        req.setItems(List.of(item));
        return req;
    }
}
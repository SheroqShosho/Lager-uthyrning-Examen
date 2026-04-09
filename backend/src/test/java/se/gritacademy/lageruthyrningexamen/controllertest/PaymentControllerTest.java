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
import se.gritacademy.lageruthyrningexamen.model.Booking;
import se.gritacademy.lageruthyrningexamen.model.BookingItem;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;
import se.gritacademy.lageruthyrningexamen.model.User;
import se.gritacademy.lageruthyrningexamen.repository.BookingRepository;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;
import se.gritacademy.lageruthyrningexamen.security.JwtService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PaymentController integration tests")
public class PaymentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private StorageUnitRepository storageUnitRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private Booking booking;
    private StorageUnit storageUnit;
    private User user;
    private String userToken;

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        storageUnitRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setEmail("payment-test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFullName("Payment Tester");
        user.setRole("CUSTOMER");
        user = userRepository.save(user);
        userToken = jwtService.generateToken(user);

        storageUnit = new StorageUnit(
                null, "P1", "Payment Test Unit", new BigDecimal("10.00"),
                new BigDecimal("500.00"), "Stockholm", true, null
        );
        storageUnit = storageUnitRepository.save(storageUnit);

        booking = new Booking(user, LocalDate.now(), LocalDate.now().plusDays(5), new BigDecimal("50.00"), "PENDING");
        booking = bookingRepository.save(booking);

        BookingItem item = new BookingItem(storageUnit, new BigDecimal("10.00"));
        item.setBooking(booking);
        booking.getItems().add(item);
        booking = bookingRepository.save(booking);
    }

    @Test
    @DisplayName("POST /api/payments/bookings/{id} returns bookingId, status, paymentRef and timestamp")
    void shouldMarkBookingAsPaid() throws Exception {
        mockMvc.perform(post("/api/payments/bookings/{id}", booking.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", notNullValue()))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.paymentRef", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Payment reference has PAY- prefix")
    void shouldGeneratePaymentRefWithPrefix() throws Exception {
        mockMvc.perform(post("/api/payments/bookings/{id}", booking.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentRef", matchesPattern("PAY-.*")));
    }

    @Test
    @DisplayName("Two separate bookings can each be paid independently")
    void shouldHandleMultipleIndependentPayments() throws Exception {
        // Pay first booking
        mockMvc.perform(post("/api/payments/bookings/{id}", booking.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Create and pay second booking
        Booking booking2 = new Booking(user, LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), new BigDecimal("60.00"), "PENDING");
        booking2 = bookingRepository.save(booking2);
        BookingItem item2 = new BookingItem(storageUnit, new BigDecimal("10.00"));
        item2.setBooking(booking2);
        booking2.getItems().add(item2);
        bookingRepository.save(booking2);

        mockMvc.perform(post("/api/payments/bookings/{id}", booking2.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentRef", matchesPattern("PAY-.*")));
    }

    @Test
    @DisplayName("Paying a non-existent booking returns 4xx error")
    void shouldReturn4xxForMissingBooking() throws Exception {
        mockMvc.perform(post("/api/payments/bookings/999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
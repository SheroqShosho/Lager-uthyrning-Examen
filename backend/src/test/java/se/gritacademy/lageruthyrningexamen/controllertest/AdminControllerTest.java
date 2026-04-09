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
import se.gritacademy.lageruthyrningexamen.controller.StorageUnitController;
import se.gritacademy.lageruthyrningexamen.model.StorageUnit;
import se.gritacademy.lageruthyrningexamen.model.User;
import se.gritacademy.lageruthyrningexamen.repository.StorageUnitRepository;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;
import se.gritacademy.lageruthyrningexamen.security.JwtService;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Admin functionality tests")
public class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private StorageUnitRepository storageUnitRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setup() {
        storageUnitRepository.deleteAll();
        userRepository.deleteAll();

        User adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setFullName("Admin User");
        adminUser.setRole("ADMIN");
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser);

        User regularUser = new User();
        regularUser.setEmail("user@test.com");
        regularUser.setPassword(passwordEncoder.encode("user123"));
        regularUser.setFullName("Regular User");
        regularUser.setRole("CUSTOMER");
        regularUser = userRepository.save(regularUser);
        userToken = jwtService.generateToken(regularUser);
    }

    // ---- Authorization tests ----

    @Test
    @DisplayName("Admin can create storage unit")
    void testAdminCanCreateStorageUnit() throws Exception {
        StorageUnitController.CreateStorageUnitRequest request = new StorageUnitController.CreateStorageUnitRequest();
        request.name = "Premium Lager";
        request.description = "Stort och modernt lagerutrymme";
        request.sizeM2 = new BigDecimal("100.00");
        request.pricePerDay = new BigDecimal("499.00");
        request.location = "Stockholm";
        request.active = true;

        mockMvc.perform(post("/api/storage-units")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Lagerutrymme skapat")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @DisplayName("Regular user cannot create storage unit (403)")
    void testRegularUserCannotCreateStorageUnit() throws Exception {
        StorageUnitController.CreateStorageUnitRequest request = new StorageUnitController.CreateStorageUnitRequest();
        request.name = "Försök lager";
        request.sizeM2 = new BigDecimal("50.00");
        request.pricePerDay = new BigDecimal("200.00");
        request.location = "Gothenburg";
        request.active = true;

        mockMvc.perform(post("/api/storage-units")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request returns 401")
    void testUnauthenticatedUserCannotCreateStorageUnit() throws Exception {
        StorageUnitController.CreateStorageUnitRequest request = new StorageUnitController.CreateStorageUnitRequest();
        request.name = "Obehörig försök";
        request.sizeM2 = new BigDecimal("30.00");
        request.pricePerDay = new BigDecimal("150.00");
        request.location = "Malmö";
        request.active = true;

        mockMvc.perform(post("/api/storage-units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalid JWT token returns 401")
    void testInvalidTokenReturns401() throws Exception {
        StorageUnitController.CreateStorageUnitRequest request = new StorageUnitController.CreateStorageUnitRequest();
        request.name = "Test";
        request.sizeM2 = new BigDecimal("10.00");
        request.pricePerDay = new BigDecimal("100.00");
        request.location = "Test";
        request.active = true;

        mockMvc.perform(post("/api/storage-units")
                        .header("Authorization", "Bearer invalid.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Malformed Bearer header returns 401")
    void testMalformedBearerHeaderReturns401() throws Exception {
        StorageUnitController.CreateStorageUnitRequest request = new StorageUnitController.CreateStorageUnitRequest();
        request.name = "Test";
        request.sizeM2 = new BigDecimal("10.00");
        request.pricePerDay = new BigDecimal("100.00");
        request.location = "Test";
        request.active = true;

        mockMvc.perform(post("/api/storage-units")
                        .header("Authorization", "Bearer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ---- Data correctness tests ----

    @Test
    @DisplayName("Created unit contains correct data")
    void testStorageUnitContainsCorrectData() throws Exception {
        StorageUnitController.CreateStorageUnitRequest request = new StorageUnitController.CreateStorageUnitRequest();
        request.name = "Premium Lager Stockholm";
        request.description = "Klimatiserat och säkert";
        request.sizeM2 = new BigDecimal("150.50");
        request.pricePerDay = new BigDecimal("799.99");
        request.location = "Södermalm, Stockholm";
        request.active = true;

        mockMvc.perform(post("/api/storage-units")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/storage-units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Premium Lager Stockholm")))
                .andExpect(jsonPath("$[0].description", is("Klimatiserat och säkert")))
                .andExpect(jsonPath("$[0].sizeM2", is(150.50)))
                .andExpect(jsonPath("$[0].pricePerDay", is(799.99)))
                .andExpect(jsonPath("$[0].location", is("Södermalm, Stockholm")))
                .andExpect(jsonPath("$[0].active", is(true)));
    }

    @Test
    @DisplayName("Unit with null active defaults to active=true")
    void testDefaultActiveTrue() throws Exception {
        StorageUnitController.CreateStorageUnitRequest request = new StorageUnitController.CreateStorageUnitRequest();
        request.name = "Default Active Lager";
        request.sizeM2 = new BigDecimal("5.00");
        request.pricePerDay = new BigDecimal("25.00");
        request.location = "Västerås";
        // active is null — should default to true

        mockMvc.perform(post("/api/storage-units")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()));

        mockMvc.perform(get("/api/storage-units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active", is(true)));
    }

    // ---- List / visibility tests ----

    @Test
    @DisplayName("GET /api/storage-units is accessible by everyone (admin, user, anonymous)")
    void testListIsPublic() throws Exception {
        StorageUnit unit = new StorageUnit(
                null, "Test Lager", "Test", new BigDecimal("50.00"),
                new BigDecimal("250.00"), "Test City", true, null
        );
        storageUnitRepository.save(unit);

        mockMvc.perform(get("/api/storage-units")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Test Lager")));

        mockMvc.perform(get("/api/storage-units")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Test Lager")));

        mockMvc.perform(get("/api/storage-units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Test Lager")));
    }

    @Test
    @DisplayName("Inactive unit is NOT returned by GET /api/storage-units")
    void testInactiveUnitIsHiddenFromList() throws Exception {
        // Create active unit
        StorageUnitController.CreateStorageUnitRequest activeReq = new StorageUnitController.CreateStorageUnitRequest();
        activeReq.name = "Aktiv Lager";
        activeReq.sizeM2 = new BigDecimal("40.00");
        activeReq.pricePerDay = new BigDecimal("300.00");
        activeReq.location = "Stockholm";
        activeReq.active = true;

        mockMvc.perform(post("/api/storage-units")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activeReq)))
                .andExpect(status().isOk());

        // Create inactive unit
        StorageUnitController.CreateStorageUnitRequest inactiveReq = new StorageUnitController.CreateStorageUnitRequest();
        inactiveReq.name = "Inaktiv Lager";
        inactiveReq.sizeM2 = new BigDecimal("40.00");
        inactiveReq.pricePerDay = new BigDecimal("300.00");
        inactiveReq.location = "Borås";
        inactiveReq.active = false;

        mockMvc.perform(post("/api/storage-units")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inactiveReq)))
                .andExpect(status().isOk());

        // Only the active unit should appear in the list
        mockMvc.perform(get("/api/storage-units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Aktiv Lager")));
    }

    @Test
    @DisplayName("Empty list when no active units exist")
    void testEmptyList() throws Exception {
        storageUnitRepository.deleteAll();
        mockMvc.perform(get("/api/storage-units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Admin can create multiple units — all appear in list")
    void testMultipleUnitsAppearsInList() throws Exception {
        for (int i = 0; i < 5; i++) {
            StorageUnitController.CreateStorageUnitRequest req = new StorageUnitController.CreateStorageUnitRequest();
            req.name = "Lager " + i;
            req.sizeM2 = new BigDecimal(10 + i * 10);
            req.pricePerDay = new BigDecimal(100 + i * 50);
            req.location = "Location " + i;
            req.active = true;

            mockMvc.perform(post("/api/storage-units")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/storage-units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }
}
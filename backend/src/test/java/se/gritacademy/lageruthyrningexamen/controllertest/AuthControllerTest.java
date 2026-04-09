package se.gritacademy.lageruthyrningexamen.controllertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import se.gritacademy.lageruthyrningexamen.dto.AuthRegisterRequest;
import se.gritacademy.lageruthyrningexamen.dto.AuthLoginRequest;
import se.gritacademy.lageruthyrningexamen.repository.UserRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController integration tests")
public class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // ==================== REGISTER ====================
    @Nested
    @DisplayName("POST /api/auth/register")
    class RegistrationTests {

        @Test
        @DisplayName("Successful registration returns token, id and email")
        void shouldRegisterSuccessfully() throws Exception {
            AuthRegisterRequest req = new AuthRegisterRequest();
            req.setEmail("newuser@example.com");
            req.setPassword("password123!");
            req.setFullName("New User");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", notNullValue()))
                    .andExpect(jsonPath("$.email", is("newuser@example.com")))
                    .andExpect(jsonPath("$.id", greaterThan(0)));
        }

        @Test
        @DisplayName("Token in registration response is valid JWT format")
        void shouldReturnJwtToken() throws Exception {
            AuthRegisterRequest req = new AuthRegisterRequest();
            req.setEmail("jwttoken@example.com");
            req.setPassword("password123!");
            req.setFullName("JWT User");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token",
                            matchesRegex("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")));
        }

        @Test
        @DisplayName("Duplicate email returns 409 Conflict")
        void shouldRejectDuplicateEmail() throws Exception {
            AuthRegisterRequest req = new AuthRegisterRequest();
            req.setEmail("duplicate@example.com");
            req.setPassword("password123!");
            req.setFullName("First User");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            AuthRegisterRequest req2 = new AuthRegisterRequest();
            req2.setEmail("duplicate@example.com");
            req2.setPassword("different!");
            req2.setFullName("Second User");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req2)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Password is hashed — slightly wrong password is rejected on login")
        void shouldHashPassword() throws Exception {
            AuthRegisterRequest req = new AuthRegisterRequest();
            req.setEmail("hashedpass@example.com");
            req.setPassword("myplainpassword!");
            req.setFullName("Hash Test");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            AuthLoginRequest loginReq = new AuthLoginRequest();
            loginReq.setEmail("hashedpass@example.com");
            loginReq.setPassword("myplainpassword"); // missing !

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Multiple independent registrations succeed")
        void shouldRegisterMultipleUsers() throws Exception {
            for (int i = 1; i <= 5; i++) {
                AuthRegisterRequest req = new AuthRegisterRequest();
                req.setEmail("user" + i + "@example.com");
                req.setPassword("password" + i + "!");
                req.setFullName("User " + i);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.email", is("user" + i + "@example.com")));
            }
        }
    }

    // ==================== LOGIN ====================
    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Correct credentials return token, id and email")
        void shouldLoginSuccessfully() throws Exception {
            AuthRegisterRequest reg = new AuthRegisterRequest();
            reg.setEmail("login@example.com");
            reg.setPassword("password123!");
            reg.setFullName("Login User");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isOk());

            AuthLoginRequest login = new AuthLoginRequest();
            login.setEmail("login@example.com");
            login.setPassword("password123!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", notNullValue()))
                    .andExpect(jsonPath("$.email", is("login@example.com")))
                    .andExpect(jsonPath("$.id", notNullValue()));
        }

        @Test
        @DisplayName("Wrong password returns 401")
        void shouldRejectWrongPassword() throws Exception {
            AuthRegisterRequest reg = new AuthRegisterRequest();
            reg.setEmail("wrongpass@example.com");
            reg.setPassword("correct123!");
            reg.setFullName("Wrong Pass");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isOk());

            AuthLoginRequest login = new AuthLoginRequest();
            login.setEmail("wrongpass@example.com");
            login.setPassword("wrong123!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Non-existent user returns 401")
        void shouldRejectNonExistentUser() throws Exception {
            AuthLoginRequest login = new AuthLoginRequest();
            login.setEmail("nobody@example.com");
            login.setPassword("anypassword!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Password comparison is case-sensitive")
        void shouldRejectWrongPasswordCase() throws Exception {
            AuthRegisterRequest reg = new AuthRegisterRequest();
            reg.setEmail("casepwd@example.com");
            reg.setPassword("Password123!");
            reg.setFullName("Case Pwd");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isOk());

            AuthLoginRequest login = new AuthLoginRequest();
            login.setEmail("casepwd@example.com");
            login.setPassword("password123!"); // lowercase 'p'

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Login token has valid JWT format")
        void shouldReturnJwtTokenOnLogin() throws Exception {
            AuthRegisterRequest reg = new AuthRegisterRequest();
            reg.setEmail("jwtlogin@example.com");
            reg.setPassword("password123!");
            reg.setFullName("JWT Login");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isOk());

            AuthLoginRequest login = new AuthLoginRequest();
            login.setEmail("jwtlogin@example.com");
            login.setPassword("password123!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token",
                            matchesRegex("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")));
        }
    }

    // ==================== GET /api/auth/me ====================
    @Nested
    @DisplayName("GET /api/auth/me")
    class GetCurrentUserTests {

        private String registerAndGetToken(String email) throws Exception {
            AuthRegisterRequest req = new AuthRegisterRequest();
            req.setEmail(email);
            req.setPassword("password123!");
            req.setFullName("Test User");

            MvcResult result = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andReturn();

            return objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("token").asText();
        }

        @Test
        @DisplayName("Valid registration token gives correct profile")
        void shouldGetCurrentUser() throws Exception {
            String token = registerAndGetToken("profile@example.com");

            mockMvc.perform(get("/api/auth/me")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email", is("profile@example.com")))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.role", is("CUSTOMER")));
        }

        @Test
        @DisplayName("Token from login endpoint also works on /me")
        void shouldHandleLoginTokenInMeEndpoint() throws Exception {
            // Register
            AuthRegisterRequest reg = new AuthRegisterRequest();
            reg.setEmail("logintoken@example.com");
            reg.setPassword("password123!");
            reg.setFullName("Login Token Test");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isOk());

            // Login and extract token
            AuthLoginRequest login = new AuthLoginRequest();
            login.setEmail("logintoken@example.com");
            login.setPassword("password123!");

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andReturn();

            String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                    .get("token").asText();

            mockMvc.perform(get("/api/auth/me")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email", is("logintoken@example.com")));
        }

        @Test
        @DisplayName("No Authorization header returns 401")
        void shouldRejectMissingToken() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Invalid token returns 401")
        void shouldRejectInvalidToken() throws Exception {
            mockMvc.perform(get("/api/auth/me")
                            .header("Authorization", "Bearer invalid_token"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Malformed Authorization header (no Bearer prefix) returns 401")
        void shouldRejectMalformedHeader() throws Exception {
            mockMvc.perform(get("/api/auth/me")
                            .header("Authorization", "NotBearer sometoken"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
package se.gritacademy.lageruthyrningexamen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {
    private String token;
    @JsonProperty("id")
    private Long userId;
    private String email;

    public AuthResponse(String token, Long userId, String email) {
        this.token = token;
        this.userId = userId;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}

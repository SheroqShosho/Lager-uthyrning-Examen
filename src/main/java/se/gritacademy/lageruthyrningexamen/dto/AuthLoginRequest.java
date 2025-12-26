package se.gritacademy.lageruthyrningexamen.dto;

public class AuthLoginRequest {
    private String email;
    private String password;

    public AuthLoginRequest() {}

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}

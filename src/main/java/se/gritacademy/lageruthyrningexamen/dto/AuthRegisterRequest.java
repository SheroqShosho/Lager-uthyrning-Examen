package se.gritacademy.lageruthyrningexamen.dto;

public class AuthRegisterRequest {

    private String email;
    private String password;
    private String fullName;

    public AuthRegisterRequest() {}

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }
}

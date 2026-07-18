package com.example.demo.dto;

import jakarta.validation.constraints.*;

public class SignUpRequestDTO {

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "Password must be at least 4 chars")
    private String password;

    @NotBlank(message = "Contact required")
    @Pattern(regexp = "\\d{10}", message = "Contact must be 10 digits")
    private String contact;

    // Removed Role field → Doctors only sign up
    // Admin will be hardcoded in DB at startup

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        this.contact = contact;
    }
}

package com.example.demo.Controller;

import org.springframework.web.bind.annotation.*;

import com.example.demo.Service.UserService;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.SignUpRequestDTO;
import com.example.demo.dto.UserResponseDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // Doctor signup only (Admin is hardcoded)
    @PostMapping("/signup")
    public UserResponseDTO signup(@Valid @RequestBody SignUpRequestDTO dto) {
        return service.signupDoctor(dto);
    }

    // Login for both Admin and Doctor
    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequestDTO dto) {
        return service.login(dto);
    }

    // Admin password change endpoint
    @PostMapping("/admin/change-password")
    public String changeAdminPassword(@RequestParam String oldPassword,
                                      @RequestParam String newPassword) {
        return service.changeAdminPassword(oldPassword, newPassword);
    }

    // Get user profile by ID
    @GetMapping("/{id}")
    public UserResponseDTO getUserProfile(@PathVariable Long id) {
        return service.getUserById(id);
    }
}

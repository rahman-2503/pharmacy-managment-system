package com.example.demo.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.Exception.*;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Security.JwtUtil;
import com.example.demo.dto.*;
import com.example.demo.model.Role;
import com.example.demo.model.User;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository repo,
                       PasswordEncoder encoder,
                       JwtUtil jwtUtil) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    // ✅ Hardcode single Admin at startup
    @PostConstruct
    public void initAdmin() {
        if (repo.findByEmail("admin@pharmacy.com").isEmpty()) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@pharmacy.com");
            admin.setPassword(encoder.encode("Admin@123")); // default password
            admin.setRole(Role.ADMIN);
            admin.setContact("0000000000");
            repo.save(admin);
        }
    }

    // ✅ Doctor Signup only
    public UserResponseDTO signupDoctor(SignUpRequestDTO dto) {
        if (repo.findByEmail(dto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(Role.DOCTOR); // force Doctor role
        user.setContact(dto.getContact());

        return map(repo.save(user));
    }

    // ✅ Login (Admin or Doctor)
    public String login(LoginRequestDTO dto) {
        User user = repo.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with email: " + dto.getEmail()));

        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        return jwtUtil.generateToken(user.getId(), user.getRole().name());
    }

    // ✅ Admin password change
    public String changeAdminPassword(String oldPassword, String newPassword) {
        User admin = repo.findByEmail("admin@pharmacy.com")
                .orElseThrow(() -> new UserNotFoundException("Admin not found"));

        if (!encoder.matches(oldPassword, admin.getPassword())) {
            throw new InvalidCredentialsException("Old password is incorrect");
        }

        admin.setPassword(encoder.encode(newPassword));
        repo.save(admin);

        return "Admin password updated successfully";
    }

    // ✅ Get user profile by ID
    public UserResponseDTO getUserById(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return map(user);
    }

    // ✅ Get all users (doctors + admin) for admin panel
    public List<UserResponseDTO> getAllUsers() {
        return repo.findAll().stream().map(this::map).collect(java.util.stream.Collectors.toList());
    }

    // ✅ Mapper
    private UserResponseDTO map(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setContact(user.getContact());
        return dto;
    }
}

package com.abhay.finance_backend.controller;

import com.abhay.finance_backend.entity.User;
import com.abhay.finance_backend.enums.Role;
import com.abhay.finance_backend.exception.ResourceNotFoundException;
import com.abhay.finance_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
// ONLY Admins can access ANY endpoint in this entire controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. View all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // 2. Create a new user (Admin creates Analysts or Viewers)
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> request) {
        if(userRepository.existsByEmail(request.get("email"))) {
            throw new IllegalArgumentException("Email already exists!");
        }

        User newUser = User.builder()
                .name(request.get("name"))
                .email(request.get("email"))
                .password(passwordEncoder.encode(request.get("password"))) // Securely hash password
                .role(Role.valueOf(request.get("role"))) // e.g., "ROLE_ANALYST"
                .isActive(true)
                .build();

        return ResponseEntity.ok(userRepository.save(newUser));
    }

    // 3. Toggle Active/Inactive status
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(isActive);
        userRepository.save(user);

        return ResponseEntity.ok("User status updated to: " + (isActive ? "Active" : "Inactive"));
    }
}
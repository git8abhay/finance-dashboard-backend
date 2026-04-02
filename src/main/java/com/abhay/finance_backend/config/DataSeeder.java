package com.abhay.finance_backend.config;

import com.abhay.finance_backend.entity.User;
import com.abhay.finance_backend.enums.Role;
import com.abhay.finance_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only seed data if the users table is completely empty
        if (userRepository.count() == 0) {

            User admin = User.builder()
                    .name("Admin User")
                    .email("admin@test.com")
                    // We MUST hash the password before saving it to the database!
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .isActive(true)
                    .build();

            User viewer = User.builder()
                    .name("Viewer User")
                    .email("viewer@test.com")
                    .password(passwordEncoder.encode("viewer123"))
                    .role(Role.ROLE_VIEWER)
                    .isActive(true)
                    .build();

            userRepository.save(admin);
            userRepository.save(viewer);

            System.out.println("✅ Test users (Admin & Viewer) seeded successfully!");
        }
    }
}
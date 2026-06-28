package com.maintainx.auth_service.config;

import com.maintainx.auth_service.entity.Users;
import com.maintainx.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    public void run(String... args) {

        if (userRepository.findByEmail("admin@maintainx.com").isEmpty()) {

            Users admin = Users.builder()
                    .name("System Admin")
                    .email("admin@maintainx.com")
                    .password(encoder.encode("Admin@123"))
                    .role("ADMIN")
                    .aadharNumber("234562345678").build();

            userRepository.save(admin);
        }
    }
}
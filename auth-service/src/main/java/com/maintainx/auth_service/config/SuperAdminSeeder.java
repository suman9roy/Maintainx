package com.maintainx.auth_service.config;

import com.maintainx.auth_service.entity.Users;
import com.maintainx.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds exactly one SUPER_ADMIN account at startup — the platform owner
 * who onboards apartments. This replaces the old AdminSeeder, which
 * seeded an apartment-scoped ADMIN; that no longer makes sense since
 * every ADMIN must now belong to a specific apartment created via
 * SuperAdminService.onboardApartment().
 *
 * CHANGE THE PASSWORD IMMEDIATELY after first login in any real
 * deployment — this default only exists so you have a way in on a
 * fresh database.
 */
@Component
@RequiredArgsConstructor
public class SuperAdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    public void run(String... args) {

        if (userRepository.findByEmail("superadmin@maintainx.com").isEmpty()) {

            Users superAdmin = Users.builder()
                    .name("Platform Super Admin")
                    .email("superadmin@maintainx.com")
                    .password(encoder.encode("SuperAdmin@123"))
                    .role("SUPER_ADMIN")
                    .apartmentId(null)
                    .build();

            userRepository.save(superAdmin);
        }
    }
}

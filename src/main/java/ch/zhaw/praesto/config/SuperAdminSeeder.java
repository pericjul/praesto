package ch.zhaw.praesto.config;

import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Legt beim Start den SUPER_ADMIN an (sofern Email/Passwort konfiguriert sind und
 * der Account noch nicht existiert). schoolId bleibt null.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class SuperAdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${praesto.super-admin.email:}")
    private String email;

    @Value("${praesto.super-admin.password:}")
    private String password;

    @Override
    public void run(String... args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.warn("SUPER_ADMIN_EMAIL/PASSWORD nicht gesetzt – überspringe SuperAdmin-Seeding");
            return;
        }

        String normalized = email.toLowerCase().trim();
        if (userRepository.existsByEmail(normalized)) {
            log.info("SuperAdmin existiert bereits, überspringe Seeding");
            return;
        }

        User superAdmin = User.builder()
                .email(normalized)
                .passwordHash(passwordEncoder.encode(password))
                .firstName("Super")
                .lastName("Admin")
                .role(UserRole.SUPER_ADMIN)
                .schoolId(null)
                .isActive(true)
                .createdAt(Instant.now())
                .build();

        userRepository.save(superAdmin);
        log.info("SuperAdmin erstellt: {}", normalized);
    }
}

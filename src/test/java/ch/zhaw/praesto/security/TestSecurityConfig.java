package ch.zhaw.praesto.security;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestSecurityConfig {
    // Basiert auf Freelancer4u TestSecurityConfig aus Übung 8

    public static final String STUDENT = "Bearer student";
    public static final String TEACHER = "Bearer teacher";
    public static final String INVALID = "Bearer invalid";

    @Bean
    public JwtDecoder jwtDecoder() {
        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) {
                var bearer = "Bearer " + token;
                if (bearer.equals(STUDENT) || bearer.equals(TEACHER)) {
                    return createJwtWithRole(token);
                }
                throw new AuthenticationException("Invalid JWT") {
                };
            }
        };
    }

    private Jwt createJwtWithRole(String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "test-user-id");
        claims.put("email", "test@test.ch");
        claims.put("name", "Test User");
        // user_roles als Liste (wie bei Auth0)
        claims.put("user_roles", List.of(role.toUpperCase()));

        return new Jwt(
                "valid-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims);
    }
}

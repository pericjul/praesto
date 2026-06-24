package ch.zhaw.praesto.security;

import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Erstellt und validiert die plattform-eigenen JWTs (HMAC-SHA256).
 * Das Secret kommt aus {@code praesto.jwt.secret} (Azure App Settings) und muss
 * mindestens 256 Bit (32 Bytes) lang sein.
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey key;
    private final long expirationHours;

    public JwtService(
            @Value("${praesto.jwt.secret}") String secret,
            @Value("${praesto.jwt.expiration-hours:24}") long expirationHours) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET muss mindestens 256 Bit (32 Bytes) lang sein");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationHours = expirationHours;
    }

    /**
     * Erstellt ein signiertes JWT mit allen relevanten User-Claims.
     */
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(expirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole() != null ? user.getRole().name() : null)
                .claim("schoolId", user.getSchoolId())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * Prüft Signatur und Ablauf. Gibt {@code false} bei jedem ungültigen Token zurück.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("Ungültiges JWT: {}", e.getMessage());
            return false;
        }
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public UserRole extractRole(String token) {
        String role = parseClaims(token).get("role", String.class);
        return role != null ? UserRole.valueOf(role) : null;
    }

    public String extractSchoolId(String token) {
        return parseClaims(token).get("schoolId", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

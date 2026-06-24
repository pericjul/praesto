package ch.zhaw.praesto.security;

import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private static final String SECRET = "0123456789012345678901234567890123456789"; // 40 bytes
    private final JwtService jwtService = new JwtService(SECRET, 24);

    private User testUser() {
        return User.builder()
                .id("user-1")
                .email("teacher@schule.ch")
                .firstName("Max")
                .lastName("Muster")
                .role(UserRole.TEACHER)
                .schoolId("school-1")
                .build();
    }

    @Test
    @DisplayName("generateToken + extract round-trip")
    void generateAndExtract() {
        String token = jwtService.generateToken(testUser());

        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo("user-1");
        assertThat(jwtService.extractEmail(token)).isEqualTo("teacher@schule.ch");
        assertThat(jwtService.extractRole(token)).isEqualTo(UserRole.TEACHER);
        assertThat(jwtService.extractSchoolId(token)).isEqualTo("school-1");
    }

    @Test
    @DisplayName("validateToken false für Müll")
    void validateToken_invalid() {
        assertThat(jwtService.validateToken("not-a-real-token")).isFalse();
    }

    @Test
    @DisplayName("Token mit falschem Secret ist ungültig")
    void validateToken_wrongSecret() {
        String token = jwtService.generateToken(testUser());
        JwtService other = new JwtService("abcdefghabcdefghabcdefghabcdefgh12345678", 24);
        assertThat(other.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("Zu kurzes Secret wird abgelehnt")
    void shortSecret_throws() {
        assertThatThrownBy(() -> new JwtService("too-short", 24))
                .isInstanceOf(IllegalStateException.class);
    }
}

package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit-Tests für den neuen, principal-basierten UserService (kein JWT-Parsing mehr).
 */
class UserServiceTest {

    private final UserService userService = new UserService();

    private void authenticate(User user) {
        var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        var auth = new UsernamePasswordAuthenticationToken(user, null, List.of(authority));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private User testUser() {
        return User.builder()
                .id("u1")
                .email("max.muster@schule.ch")
                .firstName("Max")
                .lastName("Muster")
                .role(UserRole.STUDENT)
                .schoolId("school-1")
                .isActive(true)
                .build();
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getCurrentUser liefert den Principal")
    void getCurrentUser_returnsPrincipal() {
        User user = testUser();
        authenticate(user);
        assertThat(userService.getCurrentUser()).isSameAs(user);
    }

    @Test
    @DisplayName("getCurrentUser ohne Auth wirft ForbiddenException")
    void getCurrentUser_notAuthenticated_throws() {
        assertThatThrownBy(userService::getCurrentUser).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Accessor-Methoden lesen den Principal")
    void accessors_readPrincipal() {
        authenticate(testUser());
        assertThat(userService.getUserId()).isEqualTo("u1");
        assertThat(userService.getCurrentUserId()).isEqualTo("u1");
        assertThat(userService.getEmail()).isEqualTo("max.muster@schule.ch");
        assertThat(userService.getUserName()).isEqualTo("Max Muster");
        assertThat(userService.getCurrentSchoolId()).isEqualTo("school-1");
        assertThat(userService.getCurrentUserRole()).isEqualTo(UserRole.STUDENT);
    }

    @Test
    @DisplayName("getUserName fällt auf Email-Prefix zurück")
    void getUserName_fallsBackToEmailPrefix() {
        User user = testUser();
        user.setFirstName(null);
        user.setLastName(null);
        authenticate(user);
        assertThat(userService.getUserName()).isEqualTo("max.muster");
    }

    @Test
    @DisplayName("userHasRole(String) prüft die Rolle")
    void userHasRole_string() {
        authenticate(testUser());
        assertThat(userService.userHasRole("STUDENT")).isTrue();
        assertThat(userService.userHasRole("TEACHER")).isFalse();
    }

    @Test
    @DisplayName("userHasRole(UserRole) prüft die Rolle")
    void userHasRole_enum() {
        authenticate(testUser());
        assertThat(userService.userHasRole(UserRole.STUDENT)).isTrue();
        assertThat(userService.userHasRole(UserRole.TEACHER)).isFalse();
    }

    @Test
    @DisplayName("userHasRole ohne Auth liefert false")
    void userHasRole_notAuthenticated_false() {
        assertThat(userService.userHasRole("STUDENT")).isFalse();
        assertThat(userService.userHasRole(UserRole.STUDENT)).isFalse();
    }
}

package ch.zhaw.praesto.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;
    private SecurityContext securityContext;
    private Authentication authentication;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        jwt = mock(Jwt.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ========================================
    // userHasRole
    // ========================================
    @Nested
    @DisplayName("userHasRole")
    class UserHasRole {

        @Test
        @DisplayName("User hat Rolle STUDENT")
        void userHasRole_hasStudent_returnsTrue() {
            when(jwt.getClaimAsStringList("user_roles")).thenReturn(List.of("STUDENT"));

            boolean result = userService.userHasRole("STUDENT");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("User hat Rolle TEACHER")
        void userHasRole_hasTeacher_returnsTrue() {
            when(jwt.getClaimAsStringList("user_roles")).thenReturn(List.of("TEACHER"));

            boolean result = userService.userHasRole("TEACHER");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("User hat Rolle nicht")
        void userHasRole_doesNotHaveRole_returnsFalse() {
            when(jwt.getClaimAsStringList("user_roles")).thenReturn(List.of("STUDENT"));

            boolean result = userService.userHasRole("TEACHER");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("User hat keine Rollen (null)")
        void userHasRole_nullRoles_returnsFalse() {
            when(jwt.getClaimAsStringList("user_roles")).thenReturn(null);

            boolean result = userService.userHasRole("STUDENT");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("User hat leere Rollenliste")
        void userHasRole_emptyRoles_returnsFalse() {
            when(jwt.getClaimAsStringList("user_roles")).thenReturn(List.of());

            boolean result = userService.userHasRole("STUDENT");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("User hat mehrere Rollen")
        void userHasRole_multipleRoles_returnsTrue() {
            when(jwt.getClaimAsStringList("user_roles")).thenReturn(List.of("STUDENT", "ADMIN"));

            assertThat(userService.userHasRole("STUDENT")).isTrue();
            assertThat(userService.userHasRole("ADMIN")).isTrue();
            assertThat(userService.userHasRole("TEACHER")).isFalse();
        }
    }

    // ========================================
    // getUserId
    // ========================================
    @Nested
    @DisplayName("getUserId")
    class GetUserId {

        @Test
        @DisplayName("User ID aus JWT")
        void getUserId_returnsSubClaim() {
            when(jwt.getClaimAsString("sub")).thenReturn("auth0|12345");

            String result = userService.getUserId();

            assertThat(result).isEqualTo("auth0|12345");
        }

        @Test
        @DisplayName("User ID null")
        void getUserId_nullSub_returnsNull() {
            when(jwt.getClaimAsString("sub")).thenReturn(null);

            String result = userService.getUserId();

            assertThat(result).isNull();
        }
    }

    // ========================================
    // getEmail
    // ========================================
    @Nested
    @DisplayName("getEmail")
    class GetEmail {

        @Test
        @DisplayName("Email aus JWT")
        void getEmail_returnsEmailClaim() {
            when(jwt.getClaimAsString("email")).thenReturn("test@example.com");

            String result = userService.getEmail();

            assertThat(result).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Email null")
        void getEmail_nullEmail_returnsNull() {
            when(jwt.getClaimAsString("email")).thenReturn(null);

            String result = userService.getEmail();

            assertThat(result).isNull();
        }
    }

    // ========================================
    // getUserName
    // ========================================
    @Nested
    @DisplayName("getUserName")
    class GetUserName {

        @Test
        @DisplayName("Name aus JWT")
        void getUserName_hasName_returnsName() {
            when(jwt.getClaimAsString("name")).thenReturn("Max Mustermann");

            String result = userService.getUserName();

            assertThat(result).isEqualTo("Max Mustermann");
        }

        @Test
        @DisplayName("Name null - Fallback auf Email")
        void getUserName_nullName_returnsEmailPrefix() {
            when(jwt.getClaimAsString("name")).thenReturn(null);
            when(jwt.getClaimAsString("email")).thenReturn("max.mustermann@example.com");

            String result = userService.getUserName();

            assertThat(result).isEqualTo("max.mustermann");
        }

        @Test
        @DisplayName("Name leer - Fallback auf Email")
        void getUserName_blankName_returnsEmailPrefix() {
            when(jwt.getClaimAsString("name")).thenReturn("   ");
            when(jwt.getClaimAsString("email")).thenReturn("test@test.ch");

            String result = userService.getUserName();

            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("Name und Email null - Fallback auf 'User'")
        void getUserName_nullNameAndEmail_returnsUser() {
            when(jwt.getClaimAsString("name")).thenReturn(null);
            when(jwt.getClaimAsString("email")).thenReturn(null);

            String result = userService.getUserName();

            assertThat(result).isEqualTo("User");
        }

        @Test
        @DisplayName("Name null, Email leer - Fallback auf 'User'")
        void getUserName_nullNameBlankEmail_returnsUser() {
            when(jwt.getClaimAsString("name")).thenReturn(null);
            when(jwt.getClaimAsString("email")).thenReturn("   ");

            String result = userService.getUserName();

            assertThat(result).isEqualTo("User");
        }
    }
}

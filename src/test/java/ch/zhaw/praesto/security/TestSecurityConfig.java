package ch.zhaw.praesto.security;

import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Test-Helfer für die neue, JWT-basierte Security. Statt eines echten Tokens wird
 * der bereits aufgelöste {@link User}-Principal direkt in den SecurityContext gesetzt
 * (genau wie es der {@code JwtAuthenticationFilter} zur Laufzeit tut). Verwendung in
 * MockMvc-Requests: {@code .with(TestSecurityConfig.teacher())}.
 */
public final class TestSecurityConfig {

    public static final String SCHOOL_ID = "test-school";
    public static final String OTHER_SCHOOL_ID = "other-school";
    public static final String STUDENT_ID = "test-student-id";
    public static final String TEACHER_ID = "test-teacher-id";
    public static final String ADMIN_ID = "test-admin-id";
    public static final String SUPER_ID = "test-super-id";
    public static final String DEMO_ID = "test-demo-id";
    public static final String STUDENT_EMAIL = "student@test.ch";
    public static final String TEACHER_EMAIL = "teacher@test.ch";

    private TestSecurityConfig() {
    }

    public static User user(String id, String email, UserRole role, String schoolId) {
        return User.builder()
                .id(id)
                .email(email)
                .firstName("Test")
                .lastName(role.name())
                .role(role)
                .schoolId(schoolId)
                .isActive(true)
                .build();
    }

    public static RequestPostProcessor as(User user) {
        var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        return authentication(new UsernamePasswordAuthenticationToken(user, null, List.of(authority)));
    }

    public static RequestPostProcessor student() {
        return as(user(STUDENT_ID, STUDENT_EMAIL, UserRole.STUDENT, SCHOOL_ID));
    }

    public static RequestPostProcessor teacher() {
        return as(user(TEACHER_ID, TEACHER_EMAIL, UserRole.TEACHER, SCHOOL_ID));
    }

    public static RequestPostProcessor schoolAdmin() {
        return as(user(ADMIN_ID, "admin@test.ch", UserRole.SCHOOL_ADMIN, SCHOOL_ID));
    }

    public static RequestPostProcessor superAdmin() {
        return as(user(SUPER_ID, "super@test.ch", UserRole.SUPER_ADMIN, null));
    }

    public static RequestPostProcessor demo() {
        return as(user(DEMO_ID, "demo@test.ch", UserRole.DEMO_USER, SCHOOL_ID));
    }
}

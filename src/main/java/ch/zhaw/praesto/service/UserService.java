package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Liefert den aktuell authentifizierten {@link User} aus dem SecurityContext.
 * Das eigentliche Token-Parsing übernimmt der {@code JwtAuthenticationFilter};
 * dieser Service liest nur noch den bereits aufgelösten Principal.
 */
@Service
public class UserService {

    // ============================================================
    // Neue, rollen-typisierte API (bevorzugt verwenden)
    // ============================================================

    public User getCurrentUser() {
        User user = getCurrentUserOrNull();
        if (user == null) {
            throw new ForbiddenException("Nicht authentifiziert");
        }
        return user;
    }

    public String getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public UserRole getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    public String getCurrentSchoolId() {
        return getCurrentUser().getSchoolId();
    }

    public boolean userHasRole(UserRole role) {
        User user = getCurrentUserOrNull();
        return user != null && user.getRole() == role;
    }

    // ============================================================
    // Rückwärtskompatible API (von bestehenden Services genutzt)
    // ============================================================

    public String getUserId() {
        return getCurrentUserId();
    }

    public String getEmail() {
        return getCurrentUser().getEmail();
    }

    public String getUserName() {
        return getCurrentUser().getFullName();
    }

    public boolean userHasRole(String role) {
        User user = getCurrentUserOrNull();
        return user != null && user.getRole() != null && user.getRole().name().equals(role);
    }

    // ============================================================
    // Intern
    // ============================================================

    private User getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}

package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean userHasRole(String role) {
        Jwt jwt = getJwt();
        List<String> userRoles = jwt.getClaimAsStringList("user_roles");
        if (userRoles == null) {
            return false;
        }
        return userRoles.contains(role);
    }

    public String getUserId() {
        Jwt jwt = getJwt();
        return jwt.getClaimAsString("sub");
    }

    public String getEmail() {
        Jwt jwt = getJwt();
        return jwt.getClaimAsString("email");
    }

    /**
     * Holt den Namen des Users aus der MongoDB.
     * Fallback auf Email wenn kein Name gespeichert.
     */
    public String getUserName() {
        String auth0Id = getUserId();
        Optional<User> userOpt = userRepository.findByAuth0Id(auth0Id);
        
        if (userOpt.isPresent() && userOpt.get().getName() != null) {
            return userOpt.get().getName();
        }
        
        // Fallback: Email oder "Student"
        String email = getEmail();
        if (email != null && !email.isBlank()) {
            // Nur Teil vor @ zurückgeben
            return email.split("@")[0];
        }
        return "Student";
    }

    /**
     * Holt die classId des Users aus der MongoDB.
     */
    public String getClassId() {
        String auth0Id = getUserId();
        return userRepository.findByAuth0Id(auth0Id)
                .map(User::getClassId)
                .orElse(null);
    }

    /**
     * Holt den kompletten User aus der MongoDB.
     */
    public Optional<User> getCurrentUser() {
        String auth0Id = getUserId();
        return userRepository.findByAuth0Id(auth0Id);
    }

    private Jwt getJwt() {
        return (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
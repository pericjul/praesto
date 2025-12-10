package ch.zhaw.praesto.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

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

    public String getUserName() {
        Jwt jwt = getJwt();
        String name = jwt.getClaimAsString("name");
        
        if (name != null && !name.isBlank()) {
            return name;
        }
        
        // Fallback: Email oder "User"
        String email = getEmail();
        if (email != null && !email.isBlank()) {
            return email.split("@")[0];
        }
        return "User";
    }

    private Jwt getJwt() {
        return (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
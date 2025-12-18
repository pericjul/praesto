package ch.zhaw.praesto.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;

public class JwtRoleConverter implements Converter<Jwt, Collection<SimpleGrantedAuthority>> {

    @Override
    public Collection<SimpleGrantedAuthority> convert(Jwt jwt) {
        var realmAccess = jwt.getClaimAsMap("https://2f124d.ch/user_roles");

        if (realmAccess == null || realmAccess.isEmpty()) {
            return Collections.emptyList(); // statt null
        }

        @SuppressWarnings("unchecked")
        var roles = (Collection<String>) realmAccess.get("roles");

        if (roles == null) {
            return Collections.emptyList(); // statt null
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
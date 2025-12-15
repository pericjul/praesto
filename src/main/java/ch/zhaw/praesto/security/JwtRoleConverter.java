package ch.zhaw.praesto.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import com.mongodb.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JwtRoleConverter implements Converter<Jwt, Collection<SimpleGrantedAuthority>> {

    @Override
    @Nullable
    public Collection<SimpleGrantedAuthority> convert(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("user_roles");
        if (roles == null)
            roles = List.of();

        // macht aus TEACHER -> ROLE_TEACHER
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());
    }
}
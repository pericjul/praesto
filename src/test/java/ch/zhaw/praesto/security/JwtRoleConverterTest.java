package ch.zhaw.praesto.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JwtRoleConverter Tests")
class JwtRoleConverterTest {

    private static final String ROLES_CLAIM = "https://2f124d.ch/user_roles";
    
    private JwtRoleConverter converter;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        converter = new JwtRoleConverter();
        jwt = mock(Jwt.class);
    }

    @Nested
    @DisplayName("convert")
    class Convert {

        @Test
        @DisplayName("Einzelne Rolle STUDENT")
        void convert_singleRoleStudent_returnsRoleStudent() {
            when(jwt.getClaimAsMap(ROLES_CLAIM)).thenReturn(Map.of("roles", List.of("STUDENT")));

            Collection<SimpleGrantedAuthority> result = converter.convert(jwt);

            assertThat(result).hasSize(1);
            assertThat(result).containsExactly(new SimpleGrantedAuthority("ROLE_STUDENT"));
        }

        @Test
        @DisplayName("Einzelne Rolle TEACHER")
        void convert_singleRoleTeacher_returnsRoleTeacher() {
            when(jwt.getClaimAsMap(ROLES_CLAIM)).thenReturn(Map.of("roles", List.of("TEACHER")));

            Collection<SimpleGrantedAuthority> result = converter.convert(jwt);

            assertThat(result).hasSize(1);
            assertThat(result).containsExactly(new SimpleGrantedAuthority("ROLE_TEACHER"));
        }

        @Test
        @DisplayName("Mehrere Rollen")
        void convert_multipleRoles_returnsAllRoles() {
            when(jwt.getClaimAsMap(ROLES_CLAIM)).thenReturn(Map.of("roles", List.of("STUDENT", "TEACHER", "ADMIN")));

            Collection<SimpleGrantedAuthority> result = converter.convert(jwt);

            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyInAnyOrder(
                    new SimpleGrantedAuthority("ROLE_STUDENT"),
                    new SimpleGrantedAuthority("ROLE_TEACHER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            );
        }

        @Test
        @DisplayName("Keine Rollen (null claim)")
        void convert_nullRoles_returnsEmptyList() {
            when(jwt.getClaimAsMap(ROLES_CLAIM)).thenReturn(null);

            Collection<SimpleGrantedAuthority> result = converter.convert(jwt);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Leere Map")
        void convert_emptyMap_returnsEmptyList() {
            when(jwt.getClaimAsMap(ROLES_CLAIM)).thenReturn(Map.of());

            Collection<SimpleGrantedAuthority> result = converter.convert(jwt);

            assertThat(result).isEmpty();
        }
        
        @Test
        @DisplayName("Map ohne roles Key")
        void convert_mapWithoutRolesKey_returnsEmptyList() {
            when(jwt.getClaimAsMap(ROLES_CLAIM)).thenReturn(Map.of("other", "value"));

            Collection<SimpleGrantedAuthority> result = converter.convert(jwt);

            assertThat(result).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"STUDENT", "TEACHER", "ADMIN", "USER", "MANAGER"})
        @DisplayName("Verschiedene Rollennamen werden zu ROLE_ prefixed")
        void convert_variousRoles_addsPrefx(String role) {
            when(jwt.getClaimAsMap(ROLES_CLAIM)).thenReturn(Map.of("roles", List.of(role)));

            Collection<SimpleGrantedAuthority> result = converter.convert(jwt);

            assertThat(result).hasSize(1);
            assertThat(result.iterator().next().getAuthority()).isEqualTo("ROLE_" + role);
        }
    }

    @Nested
    @DisplayName("Real JWT Tests")
    class RealJwtTests {

        @Test
        @DisplayName("Mit echtem JWT-Objekt")
        void convert_realJwt_works() {
            Jwt realJwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim(ROLES_CLAIM, Map.of("roles", List.of("STUDENT")))
                    .claim("sub", "auth0|12345")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            Collection<SimpleGrantedAuthority> result = converter.convert(realJwt);

            assertThat(result).hasSize(1);
            assertThat(result).containsExactly(new SimpleGrantedAuthority("ROLE_STUDENT"));
        }

        @Test
        @DisplayName("JWT ohne user_roles Claim")
        void convert_jwtWithoutRolesClaim_returnsEmpty() {
            Jwt realJwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim("sub", "auth0|12345")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            Collection<SimpleGrantedAuthority> result = converter.convert(realJwt);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("JWT mit beiden Rollen")
        void convert_jwtWithBothRoles_returnsBoth() {
            Jwt realJwt = Jwt.withTokenValue("token")
                    .header("alg", "RS256")
                    .claim(ROLES_CLAIM, Map.of("roles", List.of("STUDENT", "TEACHER")))
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            Collection<SimpleGrantedAuthority> result = converter.convert(realJwt);

            assertThat(result).hasSize(2);
        }
    }
}
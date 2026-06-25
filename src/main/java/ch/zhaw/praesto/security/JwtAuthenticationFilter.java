package ch.zhaw.praesto.security;

import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Liest das JWT aus dem {@code Authorization: Bearer ...} Header, validiert es,
 * lädt den zugehörigen (aktiven) User aus der DB und setzt die Authentication im
 * SecurityContext. Der Principal ist die {@link User}-Entität, die Authority
 * {@code ROLE_<role>}.
 *
 * DEMO_USER ist read-only: schreibende Requests (alles ausser GET/HEAD/OPTIONS)
 * werden mit 403 abgelehnt.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtService.validateToken(token)) {
                String userId = jwtService.extractUserId(token);
                Optional<User> userOpt = userRepository.findById(userId);

                if (userOpt.isPresent() && userOpt.get().isActive()) {
                    User user = userOpt.get();

                    // Zeitlich begrenzte Demo-Schule: Zugang nur am gebuchten Tag
                    if (!user.isWithinDemoWindow(java.time.Instant.now())) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Demo-Zugang ist ausserhalb des gebuchten Zeitraums");
                        return;
                    }

                    // Read-only Demo-Account: schreibende Zugriffe blockieren
                    if (user.getRole() == UserRole.DEMO_USER && isMutating(request)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Demo-Account ist nur lesend");
                        return;
                    }

                    var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                    var authentication = new UsernamePasswordAuthenticationToken(
                            user, null, List.of(authority));
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isMutating(HttpServletRequest request) {
        String method = request.getMethod();
        return !(HttpMethod.GET.matches(method)
                || HttpMethod.HEAD.matches(method)
                || HttpMethod.OPTIONS.matches(method));
    }
}

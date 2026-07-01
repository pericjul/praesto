package ch.zhaw.praesto.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Loggt jeden API-Aufruf mit Methode, Pfad, Status, Dauer und (falls eingeloggt)
 * Nutzer + Schule. Läuft ganz aussen (höchste Priorität), damit auch abgewiesene
 * Requests (401/403) und Fehler erfasst werden – so ist im Log sofort sichtbar,
 * WO und für WEN etwas schiefging.
 *
 * Log-Level: 2xx/3xx = INFO, 4xx = WARN, 5xx = ERROR. Health-Checks werden ignoriert.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            String path = request.getRequestURI();
            if (path != null && path.startsWith("/api") && !path.startsWith("/api/health")) {
                long ms = System.currentTimeMillis() - start;
                int status = response.getStatus();
                Object user = request.getAttribute("praesto.user");
                Object school = request.getAttribute("praesto.schoolId");
                String who = user != null
                        ? "user=" + user + (school != null ? " school=" + school : "")
                        : "anon";

                String line = "API {} {} -> {} ({}ms) {}";
                if (status >= 500) {
                    log.error(line, request.getMethod(), path, status, ms, who);
                } else if (status >= 400) {
                    log.warn(line, request.getMethod(), path, status, ms, who);
                } else {
                    log.info(line, request.getMethod(), path, status, ms, who);
                }
            }
        }
    }
}

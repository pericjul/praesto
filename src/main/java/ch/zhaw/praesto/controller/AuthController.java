package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.security.JwtService;
import ch.zhaw.praesto.service.AuthService;
import ch.zhaw.praesto.service.InviteService;
import ch.zhaw.praesto.service.PasswordResetService;
import ch.zhaw.praesto.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

/**
 * Authentifizierung: Login, Registrierung über Invite-Token, Invite-Vorschau und Demo-Login.
 * Diese Endpoints sind (ausser dem implizit über den Token geschützten Register) öffentlich.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final InviteService inviteService;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;
    private final RateLimiterService rateLimiter;

    private static String clientIp(HttpServletRequest request) {
        String fwd = request.getHeader("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) {
            return fwd.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = authService.login(request.email(), request.password());
        return toAuthResponse(user);
    }

    @PostMapping("/auth/register/{token}")
    public AuthResponse register(@PathVariable String token, @RequestBody RegisterRequest request) {
        User user = authService.register(token, request);
        return toAuthResponse(user);
    }

    // Selbst-Registrierung eines Privat-/B2C-Kontos (ohne Einladung/Schule).
    @PostMapping("/auth/signup")
    public AuthResponse signup(@RequestBody RegisterRequest request, HttpServletRequest http) {
        rateLimiter.hit("signup:" + clientIp(http), 5, Duration.ofHours(1));
        User user = authService.registerIndividual(request);
        return toAuthResponse(user);
    }

    @GetMapping("/auth/invite/{token}")
    public InviteDetailsDTO inviteDetails(@PathVariable String token) {
        return inviteService.getInviteDetails(token);
    }

    // Passwort vergessen: schickt (falls Konto existiert) eine E-Mail mit Reset-Link.
    // Antwortet immer neutral, um nicht zu verraten, ob eine E-Mail registriert ist.
    @PostMapping("/auth/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body,
                                                              HttpServletRequest http) {
        rateLimiter.hit("forgot:" + clientIp(http), 5, Duration.ofHours(1));
        passwordResetService.requestReset(body.get("email"));
        return ResponseEntity.ok(Map.of("message",
                "Falls ein Konto mit dieser E-Mail existiert, haben wir dir einen Link geschickt."));
    }

    // Neues Passwort per Reset-Token setzen.
    @PostMapping("/auth/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        passwordResetService.resetPassword(body.get("token"), body.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Passwort wurde geändert."));
    }

    @GetMapping("/demo/login")
    public AuthResponse demoLogin(@RequestParam(name = "as", required = false) String as) {
        // Öffentliche Anschau-Demo: read-only Token (kein Schreibzugriff).
        User user = authService.demoLogin(as);
        return new AuthResponse(jwtService.generateToken(user, true), UserDTO.from(user));
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), UserDTO.from(user));
    }
}

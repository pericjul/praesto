package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.security.JwtService;
import ch.zhaw.praesto.service.AuthService;
import ch.zhaw.praesto.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/auth/invite/{token}")
    public InviteDetailsDTO inviteDetails(@PathVariable String token) {
        return inviteService.getInviteDetails(token);
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

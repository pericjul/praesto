package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.service.ParentConsentService;
import ch.zhaw.praesto.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Eltern-Einverständnis für Privat-Konten: bestätigen (öffentlich) und erneut senden (eingeloggt). */
@RestController
@RequestMapping("/api/consent")
@RequiredArgsConstructor
public class ParentConsentController {

    private final ParentConsentService parentConsentService;
    private final UserService userService;

    // Öffentlich: der Elternteil klickt den Link (nicht eingeloggt).
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirm(@RequestBody Map<String, String> body) {
        String name = parentConsentService.confirm(body.get("token"));
        return ResponseEntity.ok(Map.of("name", name == null ? "" : name));
    }

    // Eingeloggte:r Schüler:in fordert die Mail erneut an.
    @PostMapping("/resend")
    public ResponseEntity<Void> resend() {
        parentConsentService.resendForUser(userService.getUserId());
        return ResponseEntity.noContent().build();
    }
}

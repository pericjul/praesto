package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Willkommens-Umfrage nach der Registrierung (rollenspezifisch). */
@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of("completed", onboardingService.isCompleted()));
    }

    @PostMapping("")
    public ResponseEntity<Void> submit(@RequestBody Map<String, Object> answers) {
        onboardingService.save(answers);
        return ResponseEntity.noContent().build();
    }
}

package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.ChallengeStartRequest;
import ch.zhaw.praesto.model.ClassChallengeDTO;
import ch.zhaw.praesto.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Klassen-Challenge: Lehrer starten/beenden, Lehrer & Schüler sehen den Fortschritt.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    // ---- Lehrer ----

    @PostMapping("/classes/{id}/challenge")
    public ClassChallengeDTO start(@PathVariable String id,
                                   @RequestBody(required = false) ChallengeStartRequest body) {
        Integer target = body != null ? body.target() : null;
        String title = body != null ? body.title() : null;
        return challengeService.startChallenge(id, target, title);
    }

    @GetMapping("/classes/{id}/challenge")
    public ResponseEntity<ClassChallengeDTO> forClass(@PathVariable String id) {
        return ResponseEntity.ok(challengeService.getForClass(id));
    }

    @DeleteMapping("/classes/{id}/challenge")
    public ResponseEntity<Void> end(@PathVariable String id) {
        challengeService.endChallenge(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Schüler ----

    @GetMapping("/student/challenge")
    public ResponseEntity<ClassChallengeDTO> forStudent() {
        return ResponseEntity.ok(challengeService.getForCurrentStudent());
    }
}

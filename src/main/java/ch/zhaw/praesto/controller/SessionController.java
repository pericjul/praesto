package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.model.ChatMessageRequest;
import ch.zhaw.praesto.model.Session;
import ch.zhaw.praesto.service.SessionService;
import ch.zhaw.praesto.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final UserService userService;

    // Session starten (nur Student)
    @PostMapping("")
    public ResponseEntity<Session> startSession() {
        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler duerfen Sessions starten");
        }
        Session session = sessionService.startSession();
        return ResponseEntity.ok(session);
    }

    // Einzelne Session holen
    @GetMapping("/{id}")
    public ResponseEntity<Session> getSessionById(@PathVariable String id) {
        Session session = sessionService.getSessionById(id);
        return ResponseEntity.ok(session);
    }

    // Nachricht senden und KI-Antwort erhalten
    @PostMapping("/{id}/messages")
    public ResponseEntity<Session> sendMessage(
            @PathVariable String id,
            @RequestBody ChatMessageRequest request) {
        
        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler duerfen Nachrichten senden");
        }
        
        Session updated = sessionService.addMessageAndGetAIResponse(id, request.getMessage());
        return ResponseEntity.ok(updated);
    }

    // Session schliessen (nur Student, nur eigene)
    @PutMapping("/{id}/close")
    public ResponseEntity<Session> closeSession(@PathVariable String id) {
        Session closed = sessionService.closeSession(id);
        return ResponseEntity.ok(closed);
    }

    // eigene Sessions ansehen (nur Student)
    @GetMapping("")
    public ResponseEntity<List<Session>> mySessions() {
        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler duerfen ihre Sessions sehen");
        }
        String studentId = userService.getUserId();
        return ResponseEntity.ok(sessionService.getSessionsForStudent(studentId));
    }
}
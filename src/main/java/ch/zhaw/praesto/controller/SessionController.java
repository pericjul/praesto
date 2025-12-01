package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.ChatMessageRequest;
import ch.zhaw.praesto.model.Session;
import ch.zhaw.praesto.model.StartSessionRequest;
import ch.zhaw.praesto.repository.AssignmentRepository;
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
    private final AssignmentRepository assignmentRepository;

    // Session starten (nur Student)
    @PostMapping("")
    public ResponseEntity<Session> startSession(@RequestBody StartSessionRequest req) {

        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler duerfen Sessions starten");
        }

        Session session = sessionService.startSession(req);
        return ResponseEntity.ok(session);
    }

    // Nachricht senden (nur Student)
    @PostMapping("/{id}/messages")
    public ResponseEntity<Session> sendMessage(
            @PathVariable String id,
            @RequestBody ChatMessageRequest req
    ) {

        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler duerfen Nachrichten senden");
        }

        Session updated = sessionService.sendMessage(id, req);
        return ResponseEntity.ok(updated);
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

    // Teacher: Sessions eines Assignments ansehen
    @GetMapping("/teacher/assignments/{assignmentId}")
    public ResponseEntity<List<Session>> getSessionsForAssignment(
            @PathVariable String assignmentId
    ) {
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen Sessions der Klasse ansehen");
        }

        var assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment nicht gefunden"));

        String teacherId = userService.getUserId();
        if (!teacherId.equals(assignment.getCreatedByTeacherId())) {
            throw new ForbiddenException("Kein Zugriff auf Sessions anderer Lehrer");
        }

        return ResponseEntity.ok(sessionService.getSessionsForAssignment(assignmentId));
    }
}
package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.ChatMessageRequest;
import ch.zhaw.praesto.model.Session;
import ch.zhaw.praesto.model.SessionMessage;
import ch.zhaw.praesto.model.SessionStatus;
import ch.zhaw.praesto.model.StartSessionRequest;
import ch.zhaw.praesto.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final InterviewAiService interviewAiService;

    /**
     * Neue Session fuer einen Schueler starten.
     * Wenn assignmentId null ist → freies Training.
     * Wenn assignmentId gesetzt ist → Aufgabe der Lehrperson als Kontext.
     */
    public Session startSession(StartSessionRequest req) {

        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler duerfen Sessions starten");
        }

        String studentId = userService.getUserId();

        Session session = Session.builder()
                .studentId(studentId)
                .assignmentId(req.getAssignmentId()) // kann null sein
                .status(SessionStatus.OPEN)
                .startedAt(Instant.now())
                .messages(new ArrayList<>())
                .build();

        // zuerst speichern, damit eine ID existiert
        session = sessionRepository.save(session);

        // Intro Nachricht von der KI erzeugen
        SessionMessage intro = interviewAiService.buildIntroMessage(session);
        session.getMessages().add(intro);

        return sessionRepository.save(session);
    }

    /**
     * Nachricht senden und KI Antwort holen.
     */
    public Session sendMessage(String sessionId, ChatMessageRequest req) {

        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler duerfen Nachrichten senden");
        }

        String studentId = userService.getUserId();

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session nicht gefunden"));

        if (!studentId.equals(session.getStudentId())) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Session");
        }

        if (session.getStatus() != SessionStatus.OPEN) {
            throw new ForbiddenException("Session ist geschlossen");
        }

        // Liste initialisieren, falls noch leer
        if (session.getMessages() == null) {
            session.setMessages(new ArrayList<>());
        }

        // Schueler Nachricht
        SessionMessage userMsg = SessionMessage.builder()
                .role("USER")
                .content(req.getMessage())
                .createdAt(Instant.now())
                .build();
        session.getMessages().add(userMsg);

        // KI Antwort erzeugen (erweiterte Logik)
        String aiText = interviewAiService.answer(
                session.getMessages(),
                req.getMessage()
        );

        SessionMessage aiMsg = SessionMessage.builder()
                .role("ASSISTANT")
                .content(aiText)
                .createdAt(Instant.now())
                .build();
        session.getMessages().add(aiMsg);

        // spaeter koenntest du hier Session ggf. schliessen (Score etc.)

        return sessionRepository.save(session);
    }

    /**
     * Alle Sessions fuer einen Schueler.
     */
    public List<Session> getSessionsForStudent(String studentId) {
        return sessionRepository.findByStudentId(studentId);
    }

    /**
     * Alle Sessions fuer ein Assignment (z B fuer Lehrer Uebersicht).
     */
    public List<Session> getSessionsForAssignment(String assignmentId) {
        return sessionRepository.findByAssignmentId(assignmentId);
    }
}
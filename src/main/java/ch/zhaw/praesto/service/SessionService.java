package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
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
import java.util.Comparator;
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

        return sessionRepository.save(session);
    }

    /**
     * Session schliessen.
     */
    public Session closeSession(String sessionId) {
        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler duerfen Sessions schliessen");
        }

        String studentId = userService.getUserId();

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session nicht gefunden"));

        if (!studentId.equals(session.getStudentId())) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Session");
        }

        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new BadRequestException("Session ist bereits geschlossen");
        }

        session.setStatus(SessionStatus.CLOSED);
        session.setClosedAt(Instant.now());

        return sessionRepository.save(session);
    }

    /**
     * Eine einzelne Session holen (mit Ownership-Check fuer Students).
     */
    public Session getSessionById(String sessionId) {
        String userId = userService.getUserId();
        boolean isStudent = userService.userHasRole("STUDENT");
        boolean isTeacher = userService.userHasRole("TEACHER");

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session nicht gefunden"));

        // Students duerfen nur eigene Sessions sehen
        if (isStudent && !userId.equals(session.getStudentId())) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Session");
        }

        // Teacher duerfen alle Sessions sehen (spaeter ggf. einschraenken auf eigene Klassen)
        if (!isStudent && !isTeacher) {
            throw new ForbiddenException("Keine Berechtigung");
        }

        return session;
    }

    /**
     * Alle Sessions fuer einen Schueler (sortiert nach Datum, neueste zuerst).
     */
    public List<Session> getSessionsForStudent(String studentId) {
        List<Session> sessions = sessionRepository.findByStudentId(studentId);
        // Sortieren: neueste zuerst
        sessions.sort(Comparator.comparing(Session::getStartedAt).reversed());
        return sessions;
    }

    /**
     * Alle Sessions fuer ein Assignment (z B fuer Lehrer Uebersicht).
     */
    public List<Session> getSessionsForAssignment(String assignmentId) {
        return sessionRepository.findByAssignmentId(assignmentId);
    }
}
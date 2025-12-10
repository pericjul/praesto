package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.repository.SessionRepository;
import ch.zhaw.praesto.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;
    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            Du bist ein freundlicher Bewerbungscoach fuer Jugendliche in der Schweiz (14 bis 20 Jahre).
            Du hilfst ihnen, sich auf Bewerbungsgespraeche vorzubereiten.
            
            Deine Aufgaben:
            - Stelle typische Interviewfragen (z.B. "Erzaehl mir etwas ueber dich", "Warum moechtest du diese Lehrstelle?")
            - Gib kurzes, konstruktives Feedback auf Antworten
            - Gib konkrete Tipps zur Verbesserung
            - Sei ermutigend und positiv
            
            Wichtig:
            - Sprich einfaches, klares Deutsch
            - Halte Antworten kurz (max 3-4 Saetze)
            - Stelle immer nur EINE Frage auf einmal
            - Sei wertschaetzend und motivierend
            
            Beginne das Gespraech mit einer freundlichen Begruessung und frage, fuer welchen Beruf sich die Person interessiert.
            """;

    private static final String ASSIGNMENT_SYSTEM_PROMPT = """
            Du bist ein freundlicher Bewerbungscoach fuer Jugendliche in der Schweiz (14 bis 20 Jahre).
            
            WICHTIG: Dies ist eine AUFGABE von der Lehrperson mit dem Titel: "%s"
            %s
            
            Der Schueler hat %d Minuten Zeit fuer dieses Training.
            
            Deine Aufgaben:
            - Fuehre ein realistisches Bewerbungsgespraech
            - Stelle typische Interviewfragen
            - Gib nach jeder Antwort kurzes Feedback
            - Sei ermutigend aber auch ehrlich
            
            Wichtig:
            - Sprich einfaches, klares Deutsch
            - Halte Antworten kurz (max 3-4 Saetze)
            - Stelle immer nur EINE Frage auf einmal
            - Am Ende des Gespraechs: Gib eine kurze Zusammenfassung was gut war und was verbessert werden kann
            
            Beginne mit einer freundlichen Begruessung und erklaere kurz, dass dies ein Uebungs-Bewerbungsgespraech ist.
            """;

    /**
     * Neue Session fuer einen Schueler starten.
     */
    public Session startSession(String assignmentId) {
        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler duerfen Sessions starten");
        }

        String studentId = userService.getUserId();
        String studentEmail = userService.getEmail().toLowerCase();

        Session.SessionBuilder sessionBuilder = Session.builder()
                .studentId(studentId)
                .studentEmail(studentEmail)
                .status(SessionStatus.OPEN)
                .startedAt(Instant.now())
                .submittedAsAssignment(false);

        String systemPrompt = SYSTEM_PROMPT;

        // Wenn für eine Aufgabe: Lade Aufgaben-Details
        if (assignmentId != null && !assignmentId.isBlank()) {
            Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new NotFoundException("Aufgabe nicht gefunden"));

            // Prüfen ob bereits für diese Aufgabe abgegeben
            if (submissionRepository.existsByAssignmentIdAndStudentEmail(assignmentId, studentEmail)) {
                throw new BadRequestException("Du hast diese Aufgabe bereits abgegeben");
            }

            sessionBuilder
                    .assignmentId(assignmentId)
                    .assignmentTitle(assignment.getTitle())
                    .targetDurationMin(assignment.getDurationMin() != null ? assignment.getDurationMin() : 15);

            // Spezieller System-Prompt für Aufgaben
            String description = assignment.getDescription() != null ? 
                    "Beschreibung: " + assignment.getDescription() : "";
            int duration = assignment.getDurationMin() != null ? assignment.getDurationMin() : 15;
            
            systemPrompt = String.format(ASSIGNMENT_SYSTEM_PROMPT, 
                    assignment.getTitle(), 
                    description,
                    duration);
        }

        // Erste KI-Nachricht generieren
        String initialMessage = getInitialAIMessage(systemPrompt);

        List<SessionMessage> messages = new ArrayList<>();
        messages.add(SessionMessage.builder()
                .role("ASSISTANT")
                .content(initialMessage)
                .createdAt(Instant.now())
                .build());

        sessionBuilder.messages(messages);

        return sessionRepository.save(sessionBuilder.build());
    }

    /**
     * Nachricht hinzufuegen und KI-Antwort erhalten.
     */
    public Session addMessageAndGetAIResponse(String sessionId, String userMessage) {
        String studentId = userService.getUserId();

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session nicht gefunden"));

        if (!studentId.equals(session.getStudentId())) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Session");
        }

        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new BadRequestException("Session ist bereits geschlossen");
        }

        // User-Nachricht hinzufuegen
        session.getMessages().add(SessionMessage.builder()
                .role("USER")
                .content(userMessage)
                .createdAt(Instant.now())
                .build());

        // System-Prompt bestimmen
        String systemPrompt = getSystemPromptForSession(session);

        // KI-Antwort generieren
        String aiResponse = getAIResponse(session.getMessages(), systemPrompt);

        // KI-Antwort hinzufuegen
        session.getMessages().add(SessionMessage.builder()
                .role("ASSISTANT")
                .content(aiResponse)
                .createdAt(Instant.now())
                .build());

        return sessionRepository.save(session);
    }

    /**
     * System-Prompt für Session bestimmen (mit oder ohne Aufgabe).
     */
    private String getSystemPromptForSession(Session session) {
        if (session.getAssignmentId() != null) {
            String description = "";
            if (session.getAssignmentTitle() != null) {
                description = "Aufgabe: " + session.getAssignmentTitle();
            }
            int duration = session.getTargetDurationMin() != null ? session.getTargetDurationMin() : 15;
            
            return String.format(ASSIGNMENT_SYSTEM_PROMPT,
                    session.getAssignmentTitle() != null ? session.getAssignmentTitle() : "Bewerbungstraining",
                    description,
                    duration);
        }
        return SYSTEM_PROMPT;
    }

    /**
     * Initiale KI-Begruessung generieren.
     */
    private String getInitialAIMessage(String systemPrompt) {
        try {
            return chatClient
                    .prompt(systemPrompt + "\n\nBitte begruesse den Schueler freundlich und beginne das Bewerbungstraining.")
                    .call()
                    .content();
        } catch (Exception e) {
            return "Hallo! 👋 Ich bin dein Bewerbungscoach. Ich helfe dir, dich auf Vorstellungsgespraeche vorzubereiten. Fuer welchen Beruf interessierst du dich?";
        }
    }

    /**
     * KI-Antwort basierend auf Konversationshistorie generieren.
     */
    private String getAIResponse(List<SessionMessage> messages, String systemPrompt) {
        try {
            // Konversation aufbauen
            List<Message> chatMessages = new ArrayList<>();
            chatMessages.add(new SystemMessage(systemPrompt));

            for (SessionMessage msg : messages) {
                if ("USER".equals(msg.getRole())) {
                    chatMessages.add(new UserMessage(msg.getContent()));
                } else if ("ASSISTANT".equals(msg.getRole())) {
                    chatMessages.add(new AssistantMessage(msg.getContent()));
                }
            }

            Prompt prompt = new Prompt(chatMessages);
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            System.err.println("KI-Fehler: " + e.getMessage());
            return "Entschuldigung, es gab einen Fehler. Kannst du das bitte nochmal versuchen?";
        }
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
     * Session schliessen UND als Aufgabe abgeben.
     */
    public Session closeAndSubmitAsAssignment(String sessionId) {
        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler duerfen Sessions abgeben");
        }

        String studentId = userService.getUserId();
        String studentEmail = userService.getEmail().toLowerCase();

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session nicht gefunden"));

        if (!studentId.equals(session.getStudentId())) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Session");
        }

        if (session.getAssignmentId() == null) {
            throw new BadRequestException("Diese Session gehoert zu keiner Aufgabe");
        }

        if (session.isSubmittedAsAssignment()) {
            throw new BadRequestException("Diese Session wurde bereits abgegeben");
        }

        // Prüfen ob bereits für diese Aufgabe abgegeben
        if (submissionRepository.existsByAssignmentIdAndStudentEmail(session.getAssignmentId(), studentEmail)) {
            throw new BadRequestException("Du hast diese Aufgabe bereits abgegeben");
        }

        // Session schliessen
        session.setStatus(SessionStatus.CLOSED);
        session.setClosedAt(Instant.now());
        session.setSubmittedAsAssignment(true);

        // Submission erstellen
        Submission submission = Submission.builder()
                .assignmentId(session.getAssignmentId())
                .studentEmail(studentEmail)
                .type(AssignmentType.AI_INTERVIEW)
                .chatSessionId(sessionId)
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(Instant.now())
                .build();

        submissionRepository.save(submission);

        return sessionRepository.save(session);
    }

    /**
     * Eine einzelne Session holen.
     */
    public Session getSessionById(String sessionId) {
        String userId = userService.getUserId();
        boolean isStudent = userService.userHasRole("STUDENT");
        boolean isTeacher = userService.userHasRole("TEACHER");

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session nicht gefunden"));

        if (isStudent && !userId.equals(session.getStudentId())) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Session");
        }

        if (!isStudent && !isTeacher) {
            throw new ForbiddenException("Keine Berechtigung");
        }

        return session;
    }

    /**
     * Alle Sessions fuer einen Schueler.
     */
    public List<Session> getSessionsForStudent(String studentId) {
        List<Session> sessions = sessionRepository.findByStudentId(studentId);
        sessions.sort(Comparator.comparing(Session::getStartedAt).reversed());
        return sessions;
    }
}
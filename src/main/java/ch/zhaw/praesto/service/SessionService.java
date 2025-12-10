package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.Session;
import ch.zhaw.praesto.model.SessionMessage;
import ch.zhaw.praesto.model.SessionStatus;
import ch.zhaw.praesto.repository.SessionRepository;
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

    /**
     * Neue Session fuer einen Schueler starten.
     */
    public Session startSession() {
        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler duerfen Sessions starten");
        }

        String studentId = userService.getUserId();

        // Erste KI-Nachricht generieren
        String initialMessage = getInitialAIMessage();

        List<SessionMessage> messages = new ArrayList<>();
        messages.add(SessionMessage.builder()
                .role("ASSISTANT")
                .content(initialMessage)
                .createdAt(Instant.now())
                .build());

        Session session = Session.builder()
                .studentId(studentId)
                .status(SessionStatus.OPEN)
                .startedAt(Instant.now())
                .messages(messages)
                .build();

        return sessionRepository.save(session);
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

        // KI-Antwort generieren
        String aiResponse = getAIResponse(session.getMessages());

        // KI-Antwort hinzufuegen
        session.getMessages().add(SessionMessage.builder()
                .role("ASSISTANT")
                .content(aiResponse)
                .createdAt(Instant.now())
                .build());

        return sessionRepository.save(session);
    }

    /**
     * Initiale KI-Begruessung generieren.
     */
    private String getInitialAIMessage() {
        try {
            return chatClient
                    .prompt(SYSTEM_PROMPT + "\n\nBitte begruesse den Schueler freundlich und beginne das Bewerbungstraining.")
                    .call()
                    .content();
        } catch (Exception e) {
            return "Hallo! 👋 Ich bin dein Bewerbungscoach. Ich helfe dir, dich auf Vorstellungsgespraeche vorzubereiten. Fuer welchen Beruf interessierst du dich?";
        }
    }

    /**
     * KI-Antwort basierend auf Konversationshistorie generieren.
     */
    private String getAIResponse(List<SessionMessage> messages) {
        try {
            // Konversation aufbauen
            List<Message> chatMessages = new ArrayList<>();
            chatMessages.add(new SystemMessage(SYSTEM_PROMPT));

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
package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.Assignment;
import ch.zhaw.praesto.model.Session;
import ch.zhaw.praesto.model.SessionMessage;
import ch.zhaw.praesto.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewAiService {

    private final ChatClient chatClient;
    private final AssignmentRepository assignmentRepository;
    private final UserService userService;

    /**
     * Intro Nachricht fuer eine neue Session.
     * - Fall A: freies Training (keine assignmentId)
     * - Fall B: Uebung wurde als Aufgabe gestartet (assignmentId gesetzt)
     */
    public SessionMessage buildIntroMessage(Session session) {

        String studentName = userService.getEmail();
        String namePart = (studentName != null && !studentName.isBlank())
                ? "Hi " + studentName + " 👋"
                : "Hi 👋";

        String content;

        if (session.getAssignmentId() == null) {
            // Freies Training
            content = """
                    %s

                    Heute simulieren wir gemeinsam ein Bewerbungsgespraech.
                    Ich stelle dir nacheinander realistische Fragen und gebe dir jeweils Feedback zu deiner Antwort.

                    Lass uns starten:

                    1️⃣ Fuer welchen Beruf interessierst du dich im Moment am meisten?
                    2️⃣ Und falls du schon eine Firma im Kopf hast: Fuer welches Unternehmen moechtest du dich bewerben?

                    Schreibe einfach in ganzen Saetzen – so wie du es in einem echten Gespraech sagen wuerdest.
                    """.formatted(namePart);
        } else {
            // Training aus Assignment gestartet
            Assignment assignment = assignmentRepository.findById(session.getAssignmentId())
                    .orElse(null);

            String assignmentTitle = assignment != null
                    ? assignment.getTitle()
                    : "deine Uebungsaufgabe";

            content = """
                    %s

                    Deine Lehrperson hat dir die Aufgabe "%s" gegeben.
                    Heute kannst du entscheiden, ob du diese Aufgabe jetzt gezielt abschliessen oder einfach frei ueben moechtest.

                    Antworte bitte zunaechst:

                    👉 "Aufgabe machen"  – wenn du die Uebung "%s" jetzt erledigen willst
                    👉 "Einfach ueben"   – wenn du ohne Bezug zur Aufgabe trainieren moechtest

                    Danach stelle ich dir passende Fragen und gebe dir jeweils Feedback.
                    """
                    .formatted(namePart, assignmentTitle, assignmentTitle);
        }

        return SessionMessage.builder()
                .role("ASSISTANT")
                .content(content)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * Erzeugt eine KI Antwort fuer ein Bewerbungsinterview (erweiterte Logik).
     *
     * - Nutzt bisherigen Verlauf (history)
     * - Gibt immer: Feedback, Tipp, naechste Frage
     */
    public String answer(List<SessionMessage> history, String lastUserMessage) {

        StringBuilder verlaufKurz = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            history.forEach(m -> verlaufKurz
                    .append(m.getRole())
                    .append(": ")
                    .append(m.getContent())
                    .append("\n"));
        }

        String systemPrompt = """
                Du bist ein freundlicher Bewerbungscoach fuer Jugendliche in der Schweiz (15 bis 20 Jahre).
                Du simulierst ein echtes Bewerbungsgespraech.

                WICHTIG:
                - Stelle Fragen passend zur Situation (z B Branche, Lehrstelle, Firma).
                - Sprich in einfachem, klaren Deutsch.
                - Sei wertschaetzend, motivierend und konstruktiv.
                - Nach jeder Antwort des Schuelers:
                  1) Kurzes Feedback (2 bis 3 Saetze)
                  2) Konkreter Verbesserungstipp mit Beispielsaetzen
                  3) Genau EINE neue Interviewfrage.

                Antworte IMMER exakt in diesem Format:

                Feedback:
                <dein Feedback>

                Tipp:
                <konkreter Verbesserungstipp mit Beispielsatz>

                Naechste Frage:
                <genau eine neue Frage>
                """;

        String userPrompt = """
                Bisheriger Verlauf (kurz, nur zur Orientierung):
                %s

                Letzte Antwort des Schuelers:
                "%s"

                Bitte gib jetzt Feedback, Tipp und stelle eine neue Frage im vereinbarten Format.
                """
                .formatted(verlaufKurz.toString(), lastUserMessage);

        // WICHTIG: neuer Spring AI Aufruf (kein call(String) mehr)
        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    /**
     * Variante mit zusaetzlichem Aufgaben-Kontext (falls du spaeter brauchst).
     */
    public String answerWithContext(List<SessionMessage> history,
                                    String lastUserMessage,
                                    String assignmentContext) {

        String contextPrefix = "";
        if (assignmentContext != null && !assignmentContext.isBlank()) {
            contextPrefix = "Kontext der Aufgabe von der Lehrperson:\n"
                    + assignmentContext + "\n\n";
        }

        StringBuilder verlaufKurz = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            history.forEach(m -> verlaufKurz
                    .append(m.getRole())
                    .append(": ")
                    .append(m.getContent())
                    .append("\n"));
        }

        String systemPrompt = """
                Du bist ein freundlicher Bewerbungscoach fuer Jugendliche in der Schweiz (15 bis 20 Jahre).
                Du simulierst ein echtes Bewerbungsgespraech und beruecksichtigst den Kontext der Aufgabe.

                WICHTIG:
                - Stelle Fragen passend zur Situation (z B Branche, Lehrstelle, Firma, Aufgabe).
                - Sprich in einfachem, klaren Deutsch.
                - Sei wertschaetzend, motivierend und konstruktiv.
                - Nach jeder Antwort des Schuelers:
                  1) Kurzes Feedback (2 bis 3 Saetze)
                  2) Konkreter Verbesserungstipp mit Beispielsaetzen
                  3) Genau EINE neue Interviewfrage.

                Antworte IMMER exakt in diesem Format:

                Feedback:
                <dein Feedback>

                Tipp:
                <konkreter Verbesserungstipp mit Beispielsatz>

                Naechste Frage:
                <genau eine neue Frage>
                """;

        String userPrompt = """
                %s
                Bisheriger Verlauf (kurz, nur zur Orientierung):
                %s

                Letzte Antwort des Schuelers:
                "%s"

                Bitte gib jetzt Feedback, Tipp und stelle eine neue Frage im vereinbarten Format.
                """
                .formatted(contextPrefix, verlaufKurz.toString(), lastUserMessage);

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }
}
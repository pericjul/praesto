package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.SessionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewAiService {

    private final ChatClient chatClient;

    /**
     * Erzeugt eine KI Antwort fuer ein Bewerbungsinterview.
     *
     * @param history         bisheriger Verlauf der Session (Student + KI)
     * @param lastUserMessage letzte Antwort des Schuelers
     * @return Text der KI Antwort (Feedback + Tipp + naechste Frage)
     */
    public String answer(List<SessionMessage> history, String lastUserMessage) {

        // Verlauf kurz zusammenfassen (optional, nur Kontext)
        StringBuilder verlaufKurz = new StringBuilder();
        if (history != null) {
            history.forEach(m -> verlaufKurz
                    .append(m.getRole()).append(": ")
                    .append(m.getContent()).append("\n"));
        }

        String systemPrompt = """
                Du bist ein freundlicher Bewerbungscoach fuer Jugendliche in der Schweiz (15 bis 20 Jahre).
                Du simulierst ein echtes Bewerbungsgespraech.

                WICHTIG:
                - Stelle Fragen wie in einem realen Bewerbungsgespraech (Lehrstelle / Einstieg ins Berufsleben).
                - Sprich in einfachem, klaren Deutsch.
                - Sei wertschätzend, motivierend und konstruktiv, nicht streng oder abwertend.
                - Nach jeder Antwort des Schuelers machst du IMMER:
                  1) Kurzes Feedback (2 bis 3 Saetze): Was war gut?
                  2) Konkreten Verbesserungsvorschlag mit Beispielsaetzen.
                  3) Dann genau EINE neue passende Interviewfrage.

                Antworte IMMER genau in diesem Format:

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
                """.formatted(verlaufKurz.toString(), lastUserMessage);

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    /**
     * Optionales Overload, falls du spaeter noch Kontext aus Assignment / Beruf mitgeben willst.
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
        if (history != null) {
            history.forEach(m -> verlaufKurz
                    .append(m.getRole()).append(": ")
                    .append(m.getContent()).append("\n"));
        }

        String systemPrompt = """
                Du bist ein freundlicher Bewerbungscoach fuer Jugendliche in der Schweiz (15 bis 20 Jahre).
                Du simulierst ein echtes Bewerbungsgespraech und passt deine Fragen an den Kontext der Aufgabe an.

                WICHTIG:
                - Stelle Fragen passend zur Situation (z.B. Branche, Lehrstelle, Firma).
                - Sprich in einfachem, klaren Deutsch.
                - Sei wertschätzend, motivierend und konstruktiv.
                - Nach jeder Antwort des Schuelers:
                  1) Kurzes Feedback (2 bis 3 Saetze)
                  2) Konkreter Verbesserungstipp mit Beispielsaetzen
                  3) Genau EINE neue Interviewfrage.

                Format:

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
                """.formatted(contextPrefix, verlaufKurz.toString(), lastUserMessage);

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }
}
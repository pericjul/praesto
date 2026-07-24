package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.repository.SessionRepository;
import ch.zhaw.praesto.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String SESSION_NICHT_GEFUNDEN = "Session nicht gefunden";
    private static final String ASSISTANT = "ASSISTANT";
    private static final String STUDENT = "STUDENT";
    private final SessionRepository sessionRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;
    private final ChatClient chatClient;
    private final BadgeService badgeService;
    private final AiQuotaService aiQuotaService;

    // Freies Üben: max. 15 Minuten pro Gespräch (Aufgaben haben ihre eigene Dauer)
    private static final int FREE_PRACTICE_MINUTES = 15;

    // Kostenbremsen (per Azure-Variable überschreibbar):
    // max. Schüler-Nachrichten pro Gespräch (Sicherheitsnetz gegen Endlos-Chat)
    @Value("${praesto.ai.session.max-user-messages:30}")
    private int maxUserMessages;

    // ============================================================
    // SYSTEM-PROMPT: Realistischer HR-Coach für Schüler
    // ============================================================
    
    private static final String SYSTEM_PROMPT = """
            Du bist ein erfahrener HR-Verantwortlicher, der Schüler (15-18 Jahre) auf echte Bewerbungsgespräche vorbereitet.
            Du stellst REALISTISCHE Fragen, die wirklich in Vorstellungsgesprächen für Lehrstellen gestellt werden.
            
            ══════════════════════════════════════════
            MODUS 1: INTERVIEW-TRAINING
            ══════════════════════════════════════════
            
            ABLAUF:
            1. Frag nach dem BERUF für den sie üben wollen
            2. Frag nach der FIRMA (oder "eine typische Firma")
            3. Führe ein realistisches Gespräch mit echten HR-Fragen
            
            ECHTE HR-FRAGEN die du stellen sollst:
            
            Motivation & Berufswahl:
            - "Warum hast du dich für diesen Beruf entschieden?"
            - "Was weisst du über unsere Firma / diesen Beruf?"
            - "Wo siehst du dich in 5 Jahren?"
            - "Warum sollten wir gerade dich nehmen?"
            
            Persönlichkeit & Stärken:
            - "Was sind deine Stärken?"
            - "Was ist eine Schwäche von dir und wie gehst du damit um?"
            - "Wie würden dich deine Freunde beschreiben?"
            - "Worauf bist du stolz?"
            
            Arbeitsweise & Teamfähigkeit:
            - "Wie gehst du mit Stress oder Zeitdruck um?"
            - "Erzähl mir von einer Situation, wo du im Team arbeiten musstest"
            - "Was machst du, wenn du mit einer Aufgabe nicht weiterkommst?"
            - "Wie organisierst du dich für die Schule?"
            
            Praktische Erfahrung:
            - "Hast du schon eine Schnupperlehre gemacht? Was hast du gelernt?"
            - "Welche Fächer liegen dir in der Schule und warum?"
            - "Hast du Hobbys die mit dem Beruf zu tun haben?"
            
            🎲 KREATIVE "CULTURE FIT" FRAGEN (1-2 pro Gespräch einstreuen):
            Diese verraten viel über Persönlichkeit, Kreativität und Denkweise!
            - "Wenn du ein Tier wärst, welches und warum?"
            - "Was würdest du mit 1 Million Franken machen?"
            - "Welche Superkraft hättest du gerne und warum?"
            - "Wenn du eine berühmte Person treffen könntest - wen und warum?"
            - "Was war das Mutigste, das du je gemacht hast?"
            - "Wie würdest du einem Kind erklären, was man in diesem Beruf macht?"
            - "Wenn du morgen Chef wärst, was würdest du ändern?"
            - "Was ist etwas, das die meisten Leute nicht über dich wissen?"
            - "Welches Problem würdest du gerne lösen?"
            - "Was bringt dich zum Lachen?"
            
            → Diese Fragen haben KEINEN falschen Antwort!
            → Sie zeigen Kreativität, Werte und wie jemand denkt
            → Erkläre dem Schüler kurz, was solche Fragen verraten können
            
            FEEDBACK nach jeder Antwort:
            1. War die Antwort überzeugend? Wenn ja, sag warum.
            2. Wenn nicht: Gib einen konkreten Tipp wie es besser geht
            3. Erkläre kurz, was HR-Leute bei dieser Frage hören wollen
            
            BEISPIEL:
            Frage: "Was sind deine Stärken?"
            Schüler: "Ich bin pünktlich"
            
            Feedback: "Pünktlichkeit ist gut, aber das erwarten Arbeitgeber sowieso. 
            Tipp: Nenne eine Stärke die dich von anderen unterscheidet und gib ein Beispiel.
            Z.B. 'Ich kann gut erklären - in der Schule helfe ich oft Mitschülern bei Mathe.'"
            
            ⚠️ NUR EINE FRAGE PRO NACHRICHT
            ⚠️ Halte dich kurz (max 4-5 Sätze)
            
            ══════════════════════════════════════════
            MODUS 2: BEWERBUNGS-BERATUNG
            ══════════════════════════════════════════
            
            Wenn der Schüler Fragen hat, hilf ihm konkret:
            
            Themen wo du helfen kannst:
            - Lebenslauf schreiben (Aufbau, Inhalt, Formulierungen)
            - Bewerbungsschreiben / Motivationsbrief
            - Vorbereitung aufs Gespräch
            - Was anziehen zum Vorstellungsgespräch
            - Wie auf bestimmte Fragen antworten
            - Schnupperlehre anfragen
            - Nach Absage: wie weitermachen
            - Nervosität bekämpfen
            
            Gib praktische, umsetzbare Tipps mit Beispielen.
            
            ══════════════════════════════════════════
            STIL:
            ══════════════════════════════════════════
            - Professionell aber freundlich
            - Klar und verständlich (keine Fachbegriffe)
            - Ermutigend aber ehrlich
            - Du-Form
            
            Beginne mit: "Hallo! Ich bin dein Bewerbungscoach. Möchtest du ein Vorstellungsgespräch üben oder hast du eine Frage zur Bewerbung?"
            """;

    // ============================================================
    // AUFGABEN-SPEZIFISCHER PROMPT (Interview für Aufgaben)
    // ============================================================
    
    private static final String ASSIGNMENT_SYSTEM_PROMPT = """
            Du bist ein HR-Verantwortlicher, der ein Vorstellungsgespräch mit einem Schüler (15-18 Jahre) führt.
            
            AUFGABE: %s
            %s
            Zeitrahmen: ca. %d Minuten
            
            ABLAUF:
            1. Frag nach dem BERUF
            2. Frag nach der FIRMA
            3. Stelle 4-6 realistische Interview-Fragen
            4. Gib am Ende eine Zusammenfassung
            
            ECHTE HR-FRAGEN (wähle passende aus):
            
            Einstieg:
            - "Erzähl mir kurz etwas über dich"
            - "Warum interessierst du dich für diesen Beruf?"
            - "Was weisst du über unsere Firma?"
            
            Hauptteil:
            - "Was sind deine Stärken?"
            - "Wo siehst du bei dir noch Entwicklungspotenzial?"
            - "Wie gehst du mit Kritik um?"
            - "Erzähl von einer Herausforderung die du gemeistert hast"
            - "Warum sollten wir dich nehmen?"
            
            🎲 Kreative Fragen (1-2 einstreuen):
            - "Wenn du ein Tier wärst, welches und warum?"
            - "Was würdest du mit 1 Million Franken machen?"
            - "Welche Superkraft hättest du gerne?"
            - "Was ist das Mutigste das du je gemacht hast?"
            - "Wie würdest du einem Kind erklären was man hier macht?"
            → Diese zeigen Persönlichkeit und wie jemand denkt!
            
            Abschluss:
            - "Hast du noch Fragen an uns?"
            
            FEEDBACK - nach jeder Antwort kurz:
            ✓ Was war gut?
            → Was könnte besser sein?
            💡 Was wollen HR-Leute hier hören?
            
            ⚠️ NUR EINE FRAGE PRO NACHRICHT
            ⚠️ Kurz halten (max 4-5 Sätze)
            
            AM ENDE - Zusammenfassung geben:
            📊 Gesamteindruck (Schulnote 1-6)
            ✅ Das hast du gut gemacht: ...
            📈 Das kannst du noch verbessern: ...
            💪 Tipp fürs echte Gespräch: ...
            
            Starte mit: "Willkommen zum Bewerbungsgespräch! Für welchen Beruf möchtest du heute üben?"
            """;

    // ============================================================
    // ROAST-MODUS: ehrlich-frech, aber immer mit echtem Mehrwert
    // ============================================================

    private static final String ROAST_ADDENDUM = """

            ══════════════════════════════════════════
            STRENGER MODUS AKTIV – DAS ÜBERSCHREIBT DEINEN TON
            ══════════════════════════════════════════
            WICHTIG: Dieser Modus hat VORRANG vor dem freundlichen Standard-Ton oben.
            Du bist jetzt eine SEHR STRENGE, anspruchsvolle Lehrperson bzw. HR-Person, die
            keine Ausreden und keine halbherzigen Antworten akzeptiert. Ernst, direkt, fordernd,
            fast unbequem. KEIN Kumpel-Ton, KEINE Jugendsprache, KEINE Emojis.

            DER UNTERSCHIED MUSS SOFORT SPÜRBAR SEIN: Der Schüler soll schon nach dem ersten Satz
            merken, dass hier ein völlig anderer, strengerer Ton herrscht als beim freundlichen
            Coach. Keine Begrüssungsfloskel, kein Aufwärmen, kein Lob vorweg – geh sofort in die
            Rolle der strengen Prüfungsperson und mach von der ersten Zeile an klar, dass du hohe
            Massstäbe hast und keine Zeit für schwache Antworten.

            REGELN:
            - Reagiere bei schwachen, kurzen, floskelhaften oder ausweichenden Antworten sofort
              scharf und schonungslos ehrlich – wie eine strenge Lehrperson im echten Gespräch.
            - Formuliere JEDES MAL ANDERS. Wiederhole dich nicht, keine Standardsätze. Wechsle
              zwischen kurzer Rüge, rhetorischer Frage und klarer Ansage. Ton-Beispiele (NUR als
              Richtung, niemals wörtlich übernehmen, immer neu formulieren):
                • "Meinst du das ernst? Mit dieser Antwort bist du im echten Gespräch schon draussen."
                • "Wenn du so weitermachst, brauchst du dich gar nicht erst zu bewerben."
                • "Das war nichts. Ein Betrieb erwartet mehr als eine leere Floskel."
                • "Denk nach, bevor du antwortest. So überzeugst du niemanden."
            - DANACH IMMER eine klare, konkrete Anweisung, WIE es besser geht – mit einem Beispiel.
              Streng heisst NICHT nutzlos: Der Schüler muss genau wissen, was er ändern soll.
            - Bei einer wirklich guten Antwort: knapp und sachlich anerkennen ("In Ordnung, das war
              brauchbar."), kein Überschwang – und sofort die nächste, härtere Frage stellen.
            - Auch die allererste Nachricht ist schon streng und fordernd, kein weiches Hallo.
              Beispiel für einen strengen Einstieg (Ton, nicht wörtlich): "Also gut. Ich habe wenig
              Zeit und noch weniger Geduld für auswendig gelernte Floskeln. Für welche Lehrstelle
              bewirbst du dich – und überzeug mich, dass du überhaupt vorbereitet bist."

            ABSOLUTE GRENZEN (niemals überschreiten):
            - Streng ja, aber NIE beleidigend und NIE persönlich: nichts über Aussehen, Herkunft,
              Religion, Familie, Gewicht, Behinderung oder Intelligenz. Es geht AUSSCHLIESSLICH um
              die Qualität der Antworten im Gespräch.
            - Ziel bleibt: Der Schüler nimmt es ernst und wird durch den Druck deutlich besser.

            BEISPIEL:
            Schüler: "Ich bin pünktlich."
            Du: "Pünktlich? Das wird vorausgesetzt, das ist keine Stärke. Damit hebst du dich von
            niemandem ab. Nenne mir eine echte Stärke und belege sie mit einem konkreten Beispiel
            aus Schule, Sport oder Alltag – sonst glaubt dir das im Gespräch kein Mensch."
            """;

    /**
     * Neue Session fuer einen Schueler starten (ohne Roast, Abwärtskompatibilität).
     */
    public Session startSession(String assignmentId) {
        return startSession(assignmentId, false);
    }

    /**
     * Neue Session fuer einen Schueler starten. Roast-Modus wirkt nur beim freien Üben
     * (bei Aufgaben immer der seriöse Modus).
     */
    public Session startSession(String assignmentId, boolean roast) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Sessions starten");
        }

        String studentId = userService.getUserId();
        String studentEmail = userService.getEmail().toLowerCase();

        boolean isAssignment = assignmentId != null && !assignmentId.isBlank();
        // Roast nur beim freien Üben – bei Aufgaben immer der seriöse Modus.
        boolean roastEffective = roast && !isAssignment;

        // KI-Gespräche sind UNBEGRENZT – weder beim freien Üben noch bei Aufgaben gibt es
        // eine Anzahl-Grenze. Begrenzt wird nur die Dauer pro Chat (15 Min, targetDurationMin).
        if (!isAssignment) {
            // Kumulativen Übungszähler hochzählen (für das Lehrer-Cockpit, überlebt Chat-Löschen).
            aiQuotaService.recordUse(studentId, userService.getCurrentSchoolId(), AiFeature.PRACTICE_INTERVIEW);
        }

        Session.SessionBuilder sessionBuilder = Session.builder()
                .schoolId(userService.getCurrentSchoolId())
                .studentId(studentId)
                .studentEmail(studentEmail)
                .status(SessionStatus.OPEN)
                .startedAt(Instant.now())
                .roast(roastEffective)
                .targetDurationMin(FREE_PRACTICE_MINUTES)
                .submittedAsAssignment(false);

        String systemPrompt = roastEffective ? SYSTEM_PROMPT + ROAST_ADDENDUM : SYSTEM_PROMPT;

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
        String initialMessage = getInitialAIMessage(systemPrompt, assignmentId != null);

        List<SessionMessage> messages = new ArrayList<>();
        messages.add(SessionMessage.builder()
                .role(ASSISTANT)
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
                .orElseThrow(() -> new NotFoundException(SESSION_NICHT_GEFUNDEN));

        if (!studentId.equals(session.getStudentId())) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Session");
        }

        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new BadRequestException("Session ist bereits geschlossen");
        }

        // Harter Zeit-Stop für ALLE Gespräche: freies Üben 15 Min, Aufgaben ihre Dauer.
        int limitMin = session.getTargetDurationMin() != null ? session.getTargetDurationMin() : FREE_PRACTICE_MINUTES;
        if (session.getStartedAt() != null
                && Instant.now().isAfter(session.getStartedAt().plus(limitMin, java.time.temporal.ChronoUnit.MINUTES))) {
            throw new BadRequestException(
                    "Die Zeit für dieses Gespräch (" + limitMin + " Min.) ist um. Schliesse es ab, um deine Bewertung zu sehen.");
        }

        // Sicherheitsnetz gegen Endlos-Chat: maximale Anzahl Schüler-Nachrichten pro Gespräch.
        long userMsgCount = session.getMessages().stream().filter(m -> "USER".equals(m.getRole())).count();
        if (userMsgCount >= maxUserMessages) {
            throw new BadRequestException(
                    "Dieses Gespräch hat das Nachrichten-Maximum erreicht. Schliesse es ab, um deine Bewertung zu sehen.");
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
                .role(ASSISTANT)
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
        return session.isRoast() ? SYSTEM_PROMPT + ROAST_ADDENDUM : SYSTEM_PROMPT;
    }

    /**
     * Initiale KI-Begruessung generieren.
     */
    private String getInitialAIMessage(String systemPrompt, boolean isAssignment) {
        try {
            String startPrompt;
            if (isAssignment) {
                startPrompt = systemPrompt + "\n\nBegrüsse kurz und stelle dann EINE Frage: Für welchen Beruf der Schüler üben möchte. Mehr nicht." + langSuffix();
            } else {
                startPrompt = systemPrompt + "\n\nBegrüsse kurz und frag mit EINER Frage, ob der Schüler üben will oder eine Frage hat. Nicht mehr." + langSuffix();
            }
            
            return AiTimeout.call(() -> chatClient
                    .prompt(startPrompt)
                    .call()
                    .content(), 25);
        } catch (Exception e) {
            if (isAssignment) {
                return "Hey! Schön, dass du üben willst. Für welchen Beruf möchtest du dich vorbereiten?";
            } else {
                return "Hey! 👋 Ich bin dein Bewerbungscoach. Was kann ich für dich tun - möchtest du ein Vorstellungsgespräch üben oder hast du eine Frage?";
            }
        }
    }

    /**
     * KI-Antwort basierend auf Konversationshistorie generieren.
     */
    private String getAIResponse(List<SessionMessage> messages, String systemPrompt) {
        try {
            // Konversation aufbauen
            List<Message> chatMessages = new ArrayList<>();
            chatMessages.add(new SystemMessage(systemPrompt + langSuffix()));

            for (SessionMessage msg : messages) {
                if ("USER".equals(msg.getRole())) {
                    chatMessages.add(new UserMessage(msg.getContent()));
                } else if (ASSISTANT.equals(msg.getRole())) {
                    chatMessages.add(new AssistantMessage(msg.getContent()));
                }
            }

            Prompt prompt = new Prompt(chatMessages);
            return AiTimeout.call(() -> chatClient.prompt(prompt).call().content(), 25);
        } catch (Exception e) {
            log.error("KI-Fehler/Timeout: {}", e.getMessage());
            return "Entschuldigung, es gab einen technischen Fehler. Kannst du das bitte nochmal versuchen?";
        }
    }

    /**
     * Sprach-Anweisung für die KI anhand der UI-Sprache des Requests (Header X-Locale,
     * vom Frontend gesetzt). So antwortet Coach + Bewertung in der Sprache der Schüler:in.
     * Ohne Request-Kontext (z.B. Auto-Close-Job) oder bei Deutsch: keine Zusatzanweisung.
     */
    private String langSuffix() {
        String lang = "de";
        try {
            var attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sra) {
                String h = sra.getRequest().getHeader("X-Locale");
                if (h != null && !h.isBlank()) lang = h.trim().toLowerCase(java.util.Locale.ROOT);
            }
        } catch (Exception ignored) {
            // kein Request-Kontext -> Deutsch
        }
        String name = switch (lang.length() >= 2 ? lang.substring(0, 2) : lang) {
            case "en" -> "Englisch";
            case "fr" -> "Französisch";
            case "it" -> "Italienisch";
            default -> "Deutsch";   // de (und Fallback) -> explizit Deutsch erzwingen
        };
        return "\n\nWICHTIG: Antworte ausschliesslich auf " + name
                + ". Verwende KEINE andere Sprache, mische keine Sprachen, keine Übersetzung.";
    }

    /**
     * Session schliessen.
     */
    public Session closeSession(String sessionId) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Sessions schliessen");
        }

        String studentId = userService.getUserId();

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException(SESSION_NICHT_GEFUNDEN));

        if (!studentId.equals(session.getStudentId())) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Session");
        }

        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new BadRequestException("Session ist bereits geschlossen");
        }

        session.setStatus(SessionStatus.CLOSED);
        session.setClosedAt(Instant.now());
        evaluateAndScore(session);

        Session saved = sessionRepository.save(session);

        // Badge-Check NACH dem Speichern
        badgeService.checkAndAwardBadges(studentId);

        return saved;
    }

    /**
     * Session schliessen UND als Aufgabe abgeben.
     */
    public Session closeAndSubmitAsAssignment(String sessionId) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Sessions abgeben");
        }

        String studentId = userService.getUserId();
        String studentEmail = userService.getEmail().toLowerCase();

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException(SESSION_NICHT_GEFUNDEN));

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
        evaluateAndScore(session);

        // Submission erstellen
        Submission submission = Submission.builder()
                .schoolId(session.getSchoolId())
                .assignmentId(session.getAssignmentId())
                .studentId(studentId)
                .studentEmail(studentEmail)
                .type(AssignmentType.AI_INTERVIEW)
                .chatSessionId(sessionId)
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(Instant.now())
                .build();

        submissionRepository.save(submission);

        Session saved = sessionRepository.save(session);

        // Badge-Check NACH dem Speichern
        badgeService.checkAndAwardBadges(studentId);

        return saved;
    }

    // ============================================================
    // Auto-Close: abgelaufene Sessions server-seitig schliessen
    // ============================================================

    /**
     * Schliesst regelmässig OPEN-Sessions, deren Zeit abgelaufen ist – auch wenn der
     * Schüler den Tab einfach zugemacht hat, ohne auf "Beenden" zu drücken. So bleibt
     * keine Session ewig offen. Läuft standardmässig alle 2 Minuten.
     */
    @org.springframework.scheduling.annotation.Scheduled(
            fixedDelayString = "${praesto.ai.session.autoclose-interval-ms:120000}")
    public void autoCloseExpiredSessions() {
        List<Session> open;
        try {
            open = sessionRepository.findByStatus(SessionStatus.OPEN);
        } catch (Exception e) {
            log.error("Auto-Close: offene Sessions konnten nicht geladen werden: {}", e.getMessage());
            return;
        }
        Instant now = Instant.now();
        for (Session s : open) {
            try {
                if (s.getStartedAt() == null) {
                    continue;
                }
                int limitMin = s.getTargetDurationMin() != null ? s.getTargetDurationMin() : FREE_PRACTICE_MINUTES;
                // 1 Min. Kulanz nach Ablauf; Senden ist ab dem Limit ohnehin gesperrt.
                Instant deadline = s.getStartedAt().plus(limitMin + 1L, java.time.temporal.ChronoUnit.MINUTES);
                if (now.isAfter(deadline)) {
                    finalizeExpiredSession(s);
                }
            } catch (Exception e) {
                log.error("Auto-Close fehlgeschlagen für Session {}: {}", s.getId(), e.getMessage());
            }
        }
    }

    /** Schliesst eine abgelaufene Session, bewertet sie und gibt Aufgaben-Gespräche ggf. ab. */
    private void finalizeExpiredSession(Session session) {
        if (session.getStatus() == SessionStatus.CLOSED) {
            return;
        }
        session.setStatus(SessionStatus.CLOSED);
        session.setClosedAt(Instant.now());
        evaluateAndScore(session);

        long userMessages = session.getMessages().stream()
                .filter(m -> "USER".equals(m.getRole()))
                .count();

        // Aufgaben-Gespräch mit echter Teilnahme automatisch abgeben (Zeit ist um = fertig),
        // sofern noch keine Abgabe existiert – so sieht die Lehrperson das Ergebnis.
        if (session.getAssignmentId() != null
                && !session.isSubmittedAsAssignment()
                && userMessages >= 1
                && session.getStudentEmail() != null
                && !submissionRepository.existsByAssignmentIdAndStudentEmail(
                        session.getAssignmentId(), session.getStudentEmail())) {
            session.setSubmittedAsAssignment(true);
            submissionRepository.save(Submission.builder()
                    .schoolId(session.getSchoolId())
                    .assignmentId(session.getAssignmentId())
                    .studentId(session.getStudentId())
                    .studentEmail(session.getStudentEmail())
                    .type(AssignmentType.AI_INTERVIEW)
                    .chatSessionId(session.getId())
                    .status(SubmissionStatus.SUBMITTED)
                    .submittedAt(Instant.now())
                    .build());
        }

        sessionRepository.save(session);

        try {
            badgeService.checkAndAwardBadges(session.getStudentId(), session.getStudentEmail());
        } catch (Exception e) {
            log.warn("Badge-Check beim Auto-Close übersprungen: {}", e.getMessage());
        }

        log.info("Session {} automatisch geschlossen (Zeit abgelaufen)", session.getId());
    }

    /**
     * Eine einzelne Session holen.
     */
    public Session getSessionById(String sessionId) {
        String userId = userService.getUserId();
        boolean isStudent = userService.userHasRole(STUDENT);
        boolean isTeacher = userService.userHasRole("TEACHER");

        // Mandanten-sicher: Session muss in der eigenen Schule liegen
        Session session = sessionRepository
                .findByIdAndSchoolId(sessionId, userService.getCurrentSchoolId())
                .orElseThrow(() -> new NotFoundException(SESSION_NICHT_GEFUNDEN));

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

    /**
     * Eine eigene Session löschen (nur Schüler, nur eigene).
     */
    public void deleteSession(String sessionId) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Sessions loeschen");
        }

        String studentId = userService.getUserId();
        Session session = sessionRepository
                .findByIdAndSchoolId(sessionId, userService.getCurrentSchoolId())
                .orElseThrow(() -> new NotFoundException(SESSION_NICHT_GEFUNDEN));

        if (!studentId.equals(session.getStudentId())) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Session");
        }

        sessionRepository.delete(session);
    }

    // ============================================================
    // BEWERTUNG: Einstellungs-Chance in % beim Schliessen
    // ============================================================

    private static final String SCORE_PROMPT = """
            Bewerte dieses geübte Bewerbungsgespräch eines Schülers (15-18 Jahre).
            Schätze die Einstellungs-Chance in Prozent (0-100) NUR anhand der Antworten des Schülers (die "Schüler:"-Zeilen).
            Antworte in GENAU EINER Zeile im Format: ZAHL|GRUND
            - ZAHL = ganze Zahl 0 bis 100
            - GRUND = ein kurzer, ehrlicher, motivierender Satz in Du-Form (max. 15 Wörter)
            Beispiel: 62|Solide Antworten – gib aber mehr konkrete Beispiele aus deinem Leben.
            """;

    /**
     * Lässt die KI das Gespräch bewerten und setzt score (0-100) + scoreReason.
     * Schlägt es fehl (z.B. kein KI-Key), bleibt der Score leer – kein harter Fehler.
     */
    private void evaluateAndScore(Session session) {
        long userMessages = session.getMessages().stream()
                .filter(m -> "USER".equals(m.getRole()))
                .count();
        if (userMessages < 1) {
            return;   // ohne Antworten des Schülers gibt es nichts zu bewerten
        }
        try {
            String result = AiTimeout.call(() -> chatClient
                    .prompt(SCORE_PROMPT + langSuffix() + "\n\nGespräch:\n" + buildTranscript(session.getMessages()))
                    .call()
                    .content(), 25);
            parseScore(session, result);
        } catch (Exception e) {
            log.warn("Bewertung fehlgeschlagen: {}", e.getMessage());
        }
    }

    private String buildTranscript(List<SessionMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (SessionMessage m : messages) {
            String who = "USER".equals(m.getRole()) ? "Schüler" : "Coach";
            sb.append(who).append(": ").append(m.getContent()).append("\n");
        }
        return sb.toString();
    }

    private void parseScore(Session session, String result) {
        if (result == null || result.isBlank()) {
            return;
        }
        String line = result.strip().lines()
                .filter(l -> l.contains("|"))
                .findFirst()
                .orElse(result.strip());
        int sep = line.indexOf('|');
        String numberPart = sep >= 0 ? line.substring(0, sep) : line;
        String reason = sep >= 0 ? line.substring(sep + 1).trim() : null;

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d{1,3}").matcher(numberPart);
        if (matcher.find()) {
            int score = Math.max(0, Math.min(100, Integer.parseInt(matcher.group())));
            session.setScore(score);
            if (reason != null && !reason.isBlank()) {
                session.setScoreReason(reason.length() > 200 ? reason.substring(0, 200) : reason);
            }
        }
    }
}
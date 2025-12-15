package ch.zhaw.praesto.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.praesto.service.UserService;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final UserService userService;
    private final ChatClient chatClient;

    public ChatController(UserService userService, ChatClient chatClient) {
        this.userService = userService;
        this.chatClient = chatClient;
    }

    private static final String SYSTEM_PROMPT = """
            Du bist ein erfahrener Bewerbungscoach für Schüler (15-18 Jahre) in der Schweiz.
            
            ══════════════════════════════════════════
            MODUS 1: INTERVIEW-TRAINING
            ══════════════════════════════════════════
            
            Wenn jemand üben will, stelle einen MIX aus:
            
            Klassische HR-Fragen:
            - "Warum hast du dich für diesen Beruf entschieden?"
            - "Was sind deine Stärken?"
            - "Wie gehst du mit Stress um?"
            - "Warum sollten wir dich nehmen?"
            - "Wo siehst du dich in 5 Jahren?"
            
            🎲 Kreative "Culture Fit" Fragen (1-2 pro Gespräch):
            - "Wenn du ein Tier wärst, welches und warum?"
            - "Was würdest du mit 1 Million Franken machen?"
            - "Welche Superkraft hättest du gerne?"
            - "Was ist das Mutigste das du je gemacht hast?"
            - "Wie würdest du einem Kind erklären was man hier macht?"
            → Diese zeigen Persönlichkeit und ob jemand zur Firma passt!
            → Es gibt keine falschen Antworten - erkläre was sie verraten
            
            Nach jeder Antwort:
            ✓ Was war gut?
            → Was könnte besser sein?
            💡 Was HR-Leute hier erfahren wollen
            
            ══════════════════════════════════════════
            MODUS 2: BEWERBUNGS-BERATUNG
            ══════════════════════════════════════════
            
            Hilf bei allen Fragen rund um Bewerbungen:
            - Lebenslauf schreiben
            - Bewerbungsschreiben / Motivationsbrief
            - Vorbereitung aufs Gespräch
            - Kleidung fürs Vorstellungsgespräch
            - Schnupperlehre anfragen
            - Nach Absagen weitermachen
            - Nervosität bekämpfen
            
            Gib konkrete Tipps mit Beispielen.
            
            ══════════════════════════════════════════
            STIL:
            ══════════════════════════════════════════
            - Freundlich und verständlich
            - Kurze Antworten (max 4-5 Sätze)
            - Du-Form
            - Nur EINE Frage pro Nachricht
            """;

    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam(required = true) String message) {
        // Nur eingeloggte User duerfen chatten
        if (!userService.userHasRole("STUDENT") && !userService.userHasRole("TEACHER")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            String content = chatClient
                    .prompt(SYSTEM_PROMPT)
                    .user(message)
                    .call()
                    .content();

            return ResponseEntity.ok(content);
        } catch (Exception e) {
            System.err.println("Chat error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler bei der KI-Anfrage: " + e.getMessage());
        }
    }
}
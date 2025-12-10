package ch.zhaw.praesto.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    UserService userService;

    @Autowired
    ChatClient chatClient;

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

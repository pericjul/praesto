package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Plattform-Benutzer. Ersetzt Auth0 vollständig: Authentifizierung läuft über
 * email + bcrypt-Passwort, Autorisierung über {@link UserRole}.
 * Jeder User (ausser SUPER_ADMIN) gehört über {@code schoolId} zu genau einer Schule.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;          // lowercase, trimmed

    private String passwordHash;   // bcrypt, strength >= 12

    private String firstName;
    private String lastName;

    private UserRole role;

    @Indexed
    private String schoolId;       // null nur für SUPER_ADMIN

    @Builder.Default
    private boolean isActive = true;

    private Instant createdAt;
    private Instant lastLoginAt;

    /**
     * Voller Anzeigename. Fällt auf den Email-Localpart zurück, wenn kein Name gesetzt ist.
     */
    public String getFullName() {
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        String full = (first + " " + last).trim();
        if (!full.isBlank()) {
            return full;
        }
        if (email != null && !email.isBlank()) {
            return email.split("@")[0];
        }
        return "User";
    }
}

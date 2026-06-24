package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Plattform-Benutzer. Authentifizierung über email + bcrypt-Passwort,
 * Autorisierung über {@link UserRole}. Jeder User (ausser SUPER_ADMIN) gehört
 * über {@code schoolId} zu genau einer Schule.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_school", columnList = "schoolId")
})
public class User {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    @Column(unique = true, nullable = false)
    private String email;          // lowercase, trimmed

    private String passwordHash;   // bcrypt, strength >= 12

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String schoolId;       // null nur für SUPER_ADMIN

    @Builder.Default
    private boolean isActive = true;

    private Instant createdAt;
    private Instant lastLoginAt;

    /**
     * Voller Anzeigename. Fällt auf den Email-Localpart zurück, wenn kein Name gesetzt ist.
     */
    @Transient
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

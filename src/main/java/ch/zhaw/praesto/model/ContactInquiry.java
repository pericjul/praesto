package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Anfrage einer interessierten Schule über das öffentliche Kontaktformular.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "contact_inquiries")
public class ContactInquiry {

    @Id
    private String id;

    private String name;            // Ansprechperson
    private String email;           // Antwort-Adresse
    private String organisation;    // Schule
    private String role;            // Funktion (z.B. Schulleitung)
    private String interest;        // Anliegen / Lizenzmodell
    private Integer classes;        // geschätzte Klassen
    private Integer students;       // geschätzte Schüler:innen
    private boolean wantsMeeting;   // wünscht Gespräch
    private String message;         // Freitext

    @Builder.Default
    private boolean handled = false;

    private Instant createdAt;
}

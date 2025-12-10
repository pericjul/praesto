package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "classes")
public class SchoolClass {

    @Id
    private String id;

    private String name;            // z.B. "INF2024a", "KV2023b"
    private String teacherId;       // Auth0-ID des Lehrers

    @Builder.Default
    private List<String> studentEmails = new ArrayList<>();  // Emails der Schüler

    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Schüler zur Klasse hinzufügen (per Email).
     */
    public void addStudent(String email) {
        if (studentEmails == null) {
            studentEmails = new ArrayList<>();
        }
        String normalizedEmail = email.toLowerCase().trim();
        if (!studentEmails.contains(normalizedEmail)) {
            studentEmails.add(normalizedEmail);
        }
    }

    /**
     * Schüler aus Klasse entfernen.
     */
    public void removeStudent(String email) {
        if (studentEmails != null) {
            studentEmails.remove(email.toLowerCase().trim());
        }
    }

    /**
     * Prüfen ob Schüler in Klasse ist.
     */
    public boolean hasStudent(String email) {
        return studentEmails != null && studentEmails.contains(email.toLowerCase().trim());
    }

    /**
     * Anzahl Schüler.
     */
    public int getStudentCount() {
        return studentEmails != null ? studentEmails.size() : 0;
    }
}
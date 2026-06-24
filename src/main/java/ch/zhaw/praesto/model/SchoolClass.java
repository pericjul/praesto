package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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

    @Indexed
    private String schoolId;         // Mandanten-Isolation (Pflichtfeld)

    private String name;            // z.B. "INF2024a", "KV2023b"
    private String teacherId;       // User.id des Lehrers

    @Builder.Default
    private List<String> studentIds = new ArrayList<>();  // User.id der Schüler

    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Schüler zur Klasse hinzufügen (per User-Id).
     */
    public void addStudent(String userId) {
        if (studentIds == null) {
            studentIds = new ArrayList<>();
        }
        if (userId != null && !studentIds.contains(userId)) {
            studentIds.add(userId);
        }
    }

    /**
     * Schüler aus Klasse entfernen.
     */
    public void removeStudent(String userId) {
        if (studentIds != null) {
            studentIds.remove(userId);
        }
    }

    /**
     * Prüfen ob Schüler in Klasse ist.
     */
    public boolean hasStudent(String userId) {
        return studentIds != null && studentIds.contains(userId);
    }

    /**
     * Anzahl Schüler.
     */
    public int getStudentCount() {
        return studentIds != null ? studentIds.size() : 0;
    }
}

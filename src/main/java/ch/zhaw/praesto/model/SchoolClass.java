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
    private List<String> studentIds = new ArrayList<>();  // Auth0-IDs der Schüler

    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Schüler zur Klasse hinzufügen.
     */
    public void addStudent(String studentId) {
        if (studentIds == null) {
            studentIds = new ArrayList<>();
        }
        if (!studentIds.contains(studentId)) {
            studentIds.add(studentId);
        }
    }

    /**
     * Schüler aus Klasse entfernen.
     */
    public void removeStudent(String studentId) {
        if (studentIds != null) {
            studentIds.remove(studentId);
        }
    }

    /**
     * Prüfen ob Schüler in Klasse ist.
     */
    public boolean hasStudent(String studentId) {
        return studentIds != null && studentIds.contains(studentId);
    }

    /**
     * Anzahl Schüler.
     */
    public int getStudentCount() {
        return studentIds != null ? studentIds.size() : 0;
    }
}
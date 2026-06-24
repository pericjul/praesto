package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "classes", indexes = {
        @Index(name = "idx_class_school", columnList = "schoolId")
})
public class SchoolClass {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String schoolId;         // Mandanten-Isolation (Pflichtfeld)

    private String name;            // z.B. "INF2024a"
    private String teacherId;       // User.id des Lehrers

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "class_student_ids", joinColumns = @JoinColumn(name = "class_id"))
    @Column(name = "student_id")
    @Builder.Default
    private List<String> studentIds = new ArrayList<>();  // User.id der Schüler

    private Instant createdAt;
    private Instant updatedAt;

    public void addStudent(String userId) {
        if (studentIds == null) {
            studentIds = new ArrayList<>();
        }
        if (userId != null && !studentIds.contains(userId)) {
            studentIds.add(userId);
        }
    }

    public void removeStudent(String userId) {
        if (studentIds != null) {
            studentIds.remove(userId);
        }
    }

    public boolean hasStudent(String userId) {
        return studentIds != null && studentIds.contains(userId);
    }

    @Transient
    public int getStudentCount() {
        return studentIds != null ? studentIds.size() : 0;
    }
}

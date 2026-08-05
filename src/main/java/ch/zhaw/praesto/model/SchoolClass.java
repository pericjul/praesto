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

    // Weitere Lehrpersonen (neben teacherId = Ersteller:in), die die Klasse gleichberechtigt
    // verwalten dürfen. Eigene Tabelle -> unkritisch fürs bestehende Schema.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "class_co_teacher_ids", joinColumns = @JoinColumn(name = "class_id"))
    @Column(name = "teacher_id")
    @Builder.Default
    private List<String> coTeacherIds = new ArrayList<>();

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

    // ----- Lehrpersonen der Klasse -----

    /** Darf diese:r Nutzer:in die Klasse verwalten? (Ersteller:in ODER Co-Lehrperson) */
    public boolean canManage(String userId) {
        if (userId == null) {
            return false;
        }
        return userId.equals(teacherId)
                || (coTeacherIds != null && coTeacherIds.contains(userId));
    }

    public void addCoTeacher(String userId) {
        if (coTeacherIds == null) {
            coTeacherIds = new ArrayList<>();
        }
        // Ersteller:in ist ohnehin schon Verwalter:in – nicht doppelt.
        if (userId != null && !userId.equals(teacherId) && !coTeacherIds.contains(userId)) {
            coTeacherIds.add(userId);
        }
    }

    public void removeCoTeacher(String userId) {
        if (coTeacherIds != null) {
            coTeacherIds.remove(userId);
        }
    }
}

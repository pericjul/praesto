package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "assignments", indexes = {
        @Index(name = "idx_assignment_school", columnList = "schoolId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String schoolId;        // Mandanten-Isolation (Pflichtfeld)

    private String title;

    @Column(columnDefinition = "text")
    private String description;

    private Integer durationMin;
    private Instant dueDate;

    @Enumerated(EnumType.STRING)
    private AssignmentType type;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    private String classId;
    private String createdByTeacherId;

    private Instant createdAt;
}

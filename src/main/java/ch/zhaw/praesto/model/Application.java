package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "applications", indexes = {
        @Index(name = "idx_application_student", columnList = "studentId")
})
public class Application {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String studentId;
    private String companyName;
    private String position;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private LocalDate appliedAt;
    private LocalDate interviewDate;

    @Column(columnDefinition = "text")
    private String notes;

    private Instant createdAt;
    private Instant updatedAt;
}

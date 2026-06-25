package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "submissions", indexes = {
        @Index(name = "idx_submission_school", columnList = "schoolId"),
        @Index(name = "idx_submission_assignment", columnList = "assignmentId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String schoolId;         // Mandanten-Isolation (Pflichtfeld)

    private String assignmentId;
    private String studentId;        // User.id
    private String studentEmail;     // Denormalisierung

    @Enumerated(EnumType.STRING)
    private AssignmentType type;

    @Column(columnDefinition = "text")
    private String textContent;      // Für SELF_REFLECTION, RESEARCH

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "submission_links", joinColumns = @JoinColumn(name = "submission_id"))
    @Column(name = "link")
    private List<String> links;      // Für RESEARCH

    private String fileUrl;          // gespeicherter Dateiname
    private String fileName;         // Original-Dateiname

    @Column(columnDefinition = "text")
    private String comment;

    private String chatSessionId;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    private Instant submittedAt;

    @Column(columnDefinition = "text")
    private String teacherFeedback;
    private Integer grade;
    private Instant reviewedAt;
    private String reviewedByTeacherId;
}

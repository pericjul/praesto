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
@Table(name = "sessions", indexes = {
        @Index(name = "idx_session_school", columnList = "schoolId"),
        @Index(name = "idx_session_student", columnList = "studentId")
})
public class Session {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String schoolId;      // Mandanten-Isolation (Pflichtfeld)

    private String studentId;     // User.id
    private String studentEmail;  // für Submission
    private String assignmentId;  // optional - wenn für Aufgabe

    private String assignmentTitle;
    private Integer targetDurationMin;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_messages", joinColumns = @JoinColumn(name = "session_id"))
    @OrderColumn(name = "msg_order")
    @Builder.Default
    private List<SessionMessage> messages = new ArrayList<>();

    private Integer score;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private Instant startedAt;
    private Instant closedAt;

    private boolean submittedAsAssignment;
}

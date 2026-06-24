package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_badges", uniqueConstraints = {
        @UniqueConstraint(name = "uq_student_badge", columnNames = {"studentId", "badgeId"})
}, indexes = {
        @Index(name = "idx_userbadge_student", columnList = "studentId")
})
public class UserBadge {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String studentId;   // User ID
    private String badgeId;     // Referenz zum Badge

    private Instant earnedAt;
}

package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Gemeinsames Klassenziel (z.B. „zusammen 100 Übungsgespräche"). Pro Klasse ist
 * höchstens eine Challenge aktiv. Fortschritt = Gespräche der Klasse seit createdAt.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "class_challenges", indexes = {
        @Index(name = "idx_challenge_class", columnList = "classId")
})
public class ClassChallenge {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String schoolId;
    private String classId;
    private String title;              // optional, sonst baut das Frontend einen Default
    private int target;                // Zielanzahl Gespräche
    private String createdByTeacherId;
    private Instant createdAt;

    @Builder.Default
    private boolean active = true;
}

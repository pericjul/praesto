package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Verbrauchszähler pro Schüler:in und KI-Funktion. Ein Datensatz je (userId, feature).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_usage", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "feature"}))
public class AiUsage {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String userId;
    private String schoolId;

    @Enumerated(EnumType.STRING)
    private AiFeature feature;

    @Builder.Default
    private int usedTotal = 0;

    private Instant updatedAt;
}

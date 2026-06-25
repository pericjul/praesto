package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Einmal- bzw. Mehrfach-Einladungslink. Wird über die /join/{token} Route eingelöst.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invite_tokens", indexes = {
        @Index(name = "idx_invite_school", columnList = "schoolId")
})
public class InviteToken {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    @Column(unique = true, nullable = false)
    private String token;          // UUID.randomUUID().toString()

    private String schoolId;

    @Enumerated(EnumType.STRING)
    private InviteType type;

    private String classId;        // nur bei type=CLASS

    private String createdByUserId;

    private Instant expiresAt;
    private Integer maxUses;       // null = unbegrenzt

    @Builder.Default
    private int usedCount = 0;

    @Builder.Default
    private boolean isActive = true;

    private Instant createdAt;

    /**
     * Ist der Token aktuell einlösbar? (aktiv, nicht abgelaufen, Nutzungslimit nicht erreicht)
     */
    @Transient
    public boolean isUsable(Instant now) {
        if (!isActive) {
            return false;
        }
        if (expiresAt != null && now.isAfter(expiresAt)) {
            return false;
        }
        return maxUses == null || usedCount < maxUses;
    }
}

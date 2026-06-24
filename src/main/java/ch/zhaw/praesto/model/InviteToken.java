package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Einmal- bzw. Mehrfach-Einladungslink. Wird über die /join/{token} Route eingelöst.
 * Lehrer-Links sind 30 Tage gültig, Klassen-Links 14 Tage, beide unbegrenzt nutzbar
 * (maxUses = null).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "invite_tokens")
public class InviteToken {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token;          // UUID.randomUUID().toString()

    @Indexed
    private String schoolId;

    private InviteType type;
    private String classId;        // nur bei type=CLASS

    private String createdByUserId;

    private Instant expiresAt;
    private Integer maxUses;       // null = unbegrenzt

    @Builder.Default
    private int usedCount = 0;

    @Builder.Default
    private boolean isActive = true;   // manuell deaktivierbar

    private Instant createdAt;

    /**
     * Ist der Token aktuell einlösbar? (aktiv, nicht abgelaufen, Nutzungslimit nicht erreicht)
     */
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

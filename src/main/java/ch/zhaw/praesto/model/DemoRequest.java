package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Anfrage einer Schule für eine Demo mit Schreibzugriff an einem bestimmten Tag.
 * Geht über das öffentliche Formular ein und landet beim SUPER_ADMIN. Bei Freigabe
 * wird eine zeitlich begrenzte Demo-Schule + Einladungslink erzeugt.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "demo_requests")
public class DemoRequest {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String schoolName;
    private String contactName;
    private String email;

    private LocalDate preferredDate;   // Wunschtag der Schule

    @Column(columnDefinition = "text")
    private String message;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DemoRequestStatus status = DemoRequestStatus.NEW;

    private Instant createdAt;

    // ----- bei Freigabe gefüllt -----
    private String approvedSchoolId;   // erzeugte Demo-Schule
    private String inviteToken;        // erzeugter Einladungs-Token
    private LocalDate approvedDate;    // freigegebener Tag
}

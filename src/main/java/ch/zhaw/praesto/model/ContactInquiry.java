package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Anfrage einer interessierten Schule über das öffentliche Kontaktformular.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contact_inquiries")
public class ContactInquiry {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String name;
    private String email;
    private String organisation;
    private String role;
    private String interest;
    private Integer classes;
    private Integer students;
    private boolean wantsMeeting;

    @Column(columnDefinition = "text")
    private String message;

    @Builder.Default
    private boolean handled = false;

    private Instant createdAt;
}

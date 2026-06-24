package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Eine Schule = ein Mandant. Datenisolation erfolgt über die {@code schoolId}
 * in allen mandantenbezogenen Tabellen.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "schools")
public class School {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String name;          // "Kantonsschule Aarau"
    private String canton;        // "AG"
    private String city;          // "Aarau"

    @Builder.Default
    private boolean isActive = true;

    private Instant createdAt;
}

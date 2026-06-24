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
@Table(name = "notes", indexes = {
        @Index(name = "idx_note_student", columnList = "studentId")
})
public class Note {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String studentId;
    private String companyName;
    private String position;

    @Column(columnDefinition = "text")
    private String text;

    private Instant createdAt;
    private Instant lastUpdated;
}

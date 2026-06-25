package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Ein Dokument im Bewerbungsdossier einer Schüler:in (hochgeladen oder generiert).
 * Die Datei selbst liegt im Upload-Ordner; {@code fileUrl} ist der gespeicherte Name.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_document_student", columnList = "studentId")
})
public class Document {

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String studentId;
    private String schoolId;

    @Enumerated(EnumType.STRING)
    private DocumentCategory category;

    private String title;        // Anzeigename
    private String fileUrl;      // gespeicherter Dateiname (für /api/files/{name})
    private String fileName;     // Original-/Download-Name

    @Builder.Default
    private boolean generated = false;   // true = von der KI erzeugt

    private Instant createdAt;
}

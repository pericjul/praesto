package ch.zhaw.praesto.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Eine globale Wissensquelle für den KI-Coach (vom Super-Admin gepflegt). Der extrahierte
 * Text ({@code content}) wird bei KI-Gesprächen als zusätzlicher Kontext mitgegeben.
 * Eigene Tabelle -> unkritisch fürs bestehende Schema.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "knowledge_sources")
public class KnowledgeSource {

    public enum Type { LINK, DOCUMENT, TEXT }

    @Id
    private String id;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    @Enumerated(EnumType.STRING)
    private Type type;

    private String title;

    @Column(length = 2048)
    private String url;              // nur bei LINK

    private String fileName;         // nur bei DOCUMENT (Original-Name)

    // Kein @Lob: auf PostgreSQL würde @Lob den String als Large Object (oid) binden,
    // die Spalte ist aber "text" -> jedes INSERT scheitert. columnDefinition="text"
    // erzeugt die Spalte und bindet den Wert als normalen Text (H2 + Postgres).
    @Column(columnDefinition = "text")
    private String content;          // extrahierter Text (was die KI nutzt)

    @Builder.Default
    private boolean enabled = true;

    private Instant createdAt;
}

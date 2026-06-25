package ch.zhaw.praesto.model;

import java.time.Instant;

/**
 * Dokument fürs Frontend. Download läuft über {@code /files/{fileUrl}} (Proxy mit JWT).
 */
public record DocumentDTO(
        String id,
        String category,
        String title,
        String fileUrl,
        String fileName,
        boolean generated,
        Instant createdAt) {

    public static DocumentDTO from(Document d) {
        return new DocumentDTO(
                d.getId(),
                d.getCategory() != null ? d.getCategory().name() : DocumentCategory.SONSTIGES.name(),
                d.getTitle(),
                d.getFileUrl(),
                d.getFileName(),
                d.isGenerated(),
                d.getCreatedAt());
    }
}

package ch.zhaw.praesto.model;

import java.time.Instant;

/**
 * Ein von einer Nutzer:in gemeldeter Bug. Bewusst als einfaches Record (kein JPA-Entity):
 * die Tabelle wird per {@code CREATE TABLE IF NOT EXISTS} im BugReportService angelegt,
 * damit das Feature unabhängig von der ddl-auto-Einstellung der Prod-DB funktioniert und
 * den App-Start nicht gefährden kann.
 */
public record BugReport(
        String id,
        String schoolId,
        String reporterId,
        String reporterName,
        String reporterEmail,
        String reporterRole,
        String title,
        String description,
        String area,
        String severity,
        String steps,
        String device,
        String status,
        Instant createdAt) {
}

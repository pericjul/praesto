package ch.zhaw.praesto.model;

/**
 * Kontingent-Stand einer KI-Funktion für die Anzeige im Frontend.
 */
public record AiQuotaStatus(
        String feature,
        int limit,
        int used,
        int remaining) {
}

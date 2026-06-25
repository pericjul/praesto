package ch.zhaw.praesto.model;

import java.time.Instant;

/**
 * Anstehender Termin für das Schüler-Dashboard – z.B. eine Aufgaben-Deadline
 * oder ein bevorstehendes Bewerbungsgespräch.
 */
public record UpcomingEvent(
        String type,    // "ASSIGNMENT" | "INTERVIEW"
        String title,   // Aufgabentitel bzw. Firmenname
        Instant date) {
}

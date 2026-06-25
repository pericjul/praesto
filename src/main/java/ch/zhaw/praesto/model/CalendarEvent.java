package ch.zhaw.praesto.model;

import java.time.Instant;

/**
 * Ein Termin im Kalender-Dashboard. {@code href} ist der Frontend-Pfad zum Detail
 * (z.B. /student/assignments/{id}), {@code type} steuert Farbe/Icon im Frontend.
 */
public record CalendarEvent(
        String type,      // ASSIGNMENT | INTERVIEW
        String title,
        Instant date,
        String href) {
}

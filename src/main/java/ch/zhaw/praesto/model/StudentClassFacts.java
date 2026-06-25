package ch.zhaw.praesto.model;

/**
 * Anonyme Aggregat-Zahlen über die eigene Klasse (ohne sich selbst). Das Frontend
 * baut daraus den rotierenden „Fakt des Tages" für das Schüler-Dashboard.
 */
public record StudentClassFacts(
        int classmateCount,        // Mitschüler:innen (ohne dich)
        int practicedCount,        // mit mindestens 1 Übungsgespräch
        int practicedTodayCount,   // haben heute geübt
        int threePlusCount,        // mit mindestens 3 Gesprächen
        int withApplicationCount   // mit mindestens 1 erfassten Bewerbung
) {
}

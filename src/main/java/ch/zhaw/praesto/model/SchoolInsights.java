package ch.zhaw.praesto.model;

import java.util.List;

/**
 * Erweiterte Kennzahlen für das Schulleitungs-Dashboard ("Braucht Aufmerksamkeit").
 */
public record SchoolInsights(
        List<UserDTO> recentUsers,        // zuletzt registrierte Nutzer:innen
        int classesWithoutAssignments,    // Klassen ohne Aufgabe
        int inactiveUsers,                // deaktivierte Konten
        long totalSessions,               // KI-Trainings gesamt
        long totalSubmissions) {          // Abgaben gesamt
}

package ch.zhaw.praesto.model;

public enum AssignmentStatus {
    ASSIGNED,       // Aufgabe zugewiesen, noch nicht begonnen
    IN_PROGRESS,    // Schüler arbeitet daran
    SUBMITTED,      // Abgegeben
    REVIEWED,       // Vom Lehrer bewertet
    CLOSED          // Aufgabe geschlossen (keine Abgaben mehr möglich)
}
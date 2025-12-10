package ch.zhaw.praesto.model;

public enum ApplicationStatus {
    PLANNED,        // Geplant, noch nicht beworben
    APPLIED,        // Bewerbung abgeschickt
    INVITED,        // Zum Gespräch eingeladen
    INTERVIEW_DONE, // Gespräch absolviert
    ACCEPTED,       // Zusage erhalten 🎉
    REJECTED,       // Absage erhalten
    WITHDRAWN       // Zurückgezogen
}
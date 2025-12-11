package ch.zhaw.praesto.model;

public enum BadgeRuleType {
    SESSIONS_COMPLETED,      // Anzahl abgeschlossene Sessions
    NOTES_CREATED,           // Anzahl erstellte Notizen
    APPLICATIONS_CREATED,    // Anzahl erstellte Bewerbungen
    APPLICATION_STATUS       // Bewerbung mit bestimmtem Status (threshold = ordinal des Status)
}
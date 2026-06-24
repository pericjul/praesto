package ch.zhaw.praesto.model;

/**
 * Art eines Invite-Links.
 * SCHOOL_ADMIN: Schulleiter-Einladung (von SuperAdmin erstellt).
 * TEACHER:      Lehrer-Einladung (von Schulleitung erstellt).
 * CLASS:        Klassen-Einladung für Schüler (von Lehrer erstellt).
 */
public enum InviteType {
    SCHOOL_ADMIN,
    TEACHER,
    CLASS
}

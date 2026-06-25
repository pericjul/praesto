package ch.zhaw.praesto.model;

/**
 * Rollen-Hierarchie der Multi-Tenant-Plattform.
 * SUPER_ADMIN (Praesto-Betreiber) → SCHOOL_ADMIN (Schulleitung) →
 * TEACHER (Lehrer) → STUDENT (Schüler). DEMO_USER ist ein read-only
 * Testaccount für die Schulleitung.
 */
public enum UserRole {
    SUPER_ADMIN,
    SCHOOL_ADMIN,
    TEACHER,
    STUDENT,
    DEMO_USER
}

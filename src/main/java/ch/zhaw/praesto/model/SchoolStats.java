package ch.zhaw.praesto.model;

/**
 * Kennzahlen einer Schule für das Schulleitungs-Dashboard.
 */
public record SchoolStats(long teachers, long students, long classes, long assignments) {
}

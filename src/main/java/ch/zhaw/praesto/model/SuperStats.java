package ch.zhaw.praesto.model;

import java.util.List;

/**
 * Gesamt-Kennzahlen über alle Schulen für das Super-Admin-Dashboard.
 */
public record SuperStats(
        long totalSchools,
        long activeSchools,
        long totalUsers,
        long totalStudents,
        long totalTeachers,
        long totalSessions,
        long totalSubmissions,
        List<SchoolStat> schools) {
}

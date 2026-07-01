package ch.zhaw.praesto.model;

import java.time.Instant;

/**
 * Kennzahlen einer einzelnen Schule für das Super-Admin-Statistik-Dashboard.
 * {@code lastActivityAt} = jüngster Login irgendeiner Person dieser Schule (null = noch nie).
 */
public record SchoolStat(
        String id,
        String name,
        String city,
        String canton,
        boolean active,
        long userCount,
        long studentCount,
        long teacherCount,
        long sessionCount,
        long submissionCount,
        Instant lastActivityAt) {
}

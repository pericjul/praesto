package ch.zhaw.praesto.model;

import java.time.Instant;

/**
 * Schule inkl. Nutzerzahl für das SuperAdmin-Dashboard.
 */
public record SchoolSummary(
        String id,
        String name,
        String canton,
        String city,
        boolean active,
        long userCount,
        Instant createdAt) {

    public static SchoolSummary from(School school, long userCount) {
        return new SchoolSummary(
                school.getId(),
                school.getName(),
                school.getCanton(),
                school.getCity(),
                school.isActive(),
                userCount,
                school.getCreatedAt());
    }
}

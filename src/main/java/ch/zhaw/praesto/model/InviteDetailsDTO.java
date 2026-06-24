package ch.zhaw.praesto.model;

import java.time.Instant;

/**
 * Öffentliche Detail-Sicht auf einen Invite-Token, damit die /join Seite den
 * passenden Kontext anzeigen kann.
 */
public record InviteDetailsDTO(
        String type,        // SCHOOL_ADMIN | TEACHER | CLASS
        String schoolName,
        String className,   // nur bei type=CLASS
        Instant expiresAt) {
}

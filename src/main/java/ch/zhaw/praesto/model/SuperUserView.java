package ch.zhaw.praesto.model;

import java.time.Instant;

/**
 * Zeile der Super-Admin-Nutzerübersicht: alle Nutzer:innen aller Schulen mit
 * Schulname und „zuletzt eingeloggt" (für Filter nach Schule + Inaktivität).
 */
public record SuperUserView(
        String id,
        String name,
        String email,
        String role,
        String schoolId,
        String schoolName,
        boolean active,
        Instant lastLoginAt,
        Instant createdAt) {
}

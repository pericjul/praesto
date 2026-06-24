package ch.zhaw.praesto.model;

import java.time.Instant;

/**
 * Antwort beim Erstellen eines Invite-Links.
 */
public record InviteCreatedDTO(
        String id,
        String token,
        String url,
        Instant expiresAt,
        String className) {
}

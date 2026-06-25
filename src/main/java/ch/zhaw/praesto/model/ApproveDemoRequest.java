package ch.zhaw.praesto.model;

import java.time.LocalDate;

/**
 * Freigabe einer Demo-Anfrage durch den SUPER_ADMIN. {@code date} ist optional –
 * fehlt es, wird das Wunschdatum der Anfrage verwendet.
 */
public record ApproveDemoRequest(LocalDate date) {
}

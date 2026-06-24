package ch.zhaw.praesto.model;

import java.time.LocalDate;

/**
 * Öffentliche Demo-Terminanfrage einer Schule.
 */
public record DemoRequestSubmitRequest(
        String schoolName,
        String contactName,
        String email,
        LocalDate preferredDate,
        String message) {
}

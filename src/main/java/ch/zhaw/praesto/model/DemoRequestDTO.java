package ch.zhaw.praesto.model;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Demo-Anfrage für die Super-Admin-Ansicht. Bei freigegebenen Anfragen enthält
 * {@code inviteUrl} den zeitlich begrenzten Einladungslink zum Weiterleiten.
 */
public record DemoRequestDTO(
        String id,
        String schoolName,
        String contactName,
        String email,
        LocalDate preferredDate,
        String message,
        String status,
        Instant createdAt,
        LocalDate approvedDate,
        String inviteUrl) {

    public static DemoRequestDTO from(DemoRequest r, String inviteUrl) {
        return new DemoRequestDTO(
                r.getId(),
                r.getSchoolName(),
                r.getContactName(),
                r.getEmail(),
                r.getPreferredDate(),
                r.getMessage(),
                r.getStatus() != null ? r.getStatus().name() : DemoRequestStatus.NEW.name(),
                r.getCreatedAt(),
                r.getApprovedDate(),
                inviteUrl);
    }
}

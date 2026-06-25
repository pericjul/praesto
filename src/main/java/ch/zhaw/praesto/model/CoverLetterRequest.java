package ch.zhaw.praesto.model;

/**
 * Antworten für das Bewerbungsschreiben. Leere Felder werden ignoriert.
 */
public record CoverLetterRequest(
        String fullName,
        String senderAddress,
        String companyName,
        String companyAddress,
        String targetJob,
        String whyCompany,
        String strengths,
        String startDate,
        String extra) {
}

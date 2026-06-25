package ch.zhaw.praesto.model;

/**
 * Antworten der Lebenslauf-Umfrage. Leere Felder werden ignoriert.
 */
public record CvRequest(
        String fullName,
        String birthDate,
        String address,
        String phone,
        String email,
        String targetJob,
        String school,
        String experience,
        String skills,
        String languages,
        String hobbies,
        String extra) {
}

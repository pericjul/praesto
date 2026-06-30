package ch.zhaw.praesto.model;

/**
 * Eingaben des Lebenslauf-Formulars. Wird OHNE KI direkt strukturiert ins
 * Word-Dokument geschrieben. Leere Felder werden weggelassen. Mehrzeilige Felder
 * (education, experience, languages, references): eine Zeile = ein Eintrag.
 */
public record CvRequest(
        // Personalien
        String firstName,
        String lastName,
        String address,
        String zipCity,
        String phone,
        String email,
        String birthDate,     // optional
        String hometown,      // optional (Heimat-/Geburtsort)
        String nationality,   // optional
        String parents,       // optional, mehrzeilig: "Mutter · Name · Beruf"
        String siblings,      // optional, mehrzeilig: "Name · Beruf/Schule"
        String photoUrl,      // optional, gespeicherter Dateiname des Bewerbungsfotos
        // Inhalt
        String aboutMe,
        String targetJob,
        String education,     // mehrzeilig: "2021–2025 · Sek B, Zürich"
        String experience,    // mehrzeilig: "Mai 2024 · Schnupperlehre Kaufmann, Muster AG — Büroarbeit"
        String skills,
        String languages,     // mehrzeilig: "Deutsch · Muttersprache"
        String hobbies,
        String references     // mehrzeilig: "Frau Muster · Klassenlehrerin · 0791234567"
) {
}

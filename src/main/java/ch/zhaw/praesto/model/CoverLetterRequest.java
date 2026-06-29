package ch.zhaw.praesto.model;

/**
 * Eingaben fürs Bewerbungsschreiben. Die KI wandelt diese Angaben in einen
 * sauberen CH-Brief um. Leere Felder werden weggelassen / nie erfunden.
 */
public record CoverLetterRequest(
        String fullName,
        String senderAddress,
        String city,                 // Ort für die Datumszeile
        String companyName,
        String companyAddress,
        String contactPerson,        // Ansprechperson (Anrede + Name), z.B. "Frau Muster"
        String targetJob,            // Beruf (EFZ/EBA)
        String applicationSource,    // wo gefunden (yousty, Inserat, Website …)
        String startDate,            // Lehrbeginn
        String whyCompany,           // warum genau diese Firma
        String strengths,            // Stärken (idealerweise mit Beispiel)
        String schnupperExperience,  // Schnupperlehre + was gefallen hat
        String extra) {
}

package ch.zhaw.praesto.model;

/**
 * Eingaben fürs Bewerbungsschreiben. Die KI formuliert daraus den Brieftext; das
 * Layout (Absender, Empfänger, Betreff, Anrede, Gruss) wird strukturiert erzeugt.
 * Fast alles ist optional – leere Felder werden weggelassen, nie erfunden.
 */
public record CoverLetterRequest(
        // Absender (Schüler:in)
        String fullName,
        String senderStreet,       // Strasse Nr.
        String senderZipCity,      // PLZ Ort
        String phone,
        String email,

        // Empfänger (Firma) – Adresse optional
        String companyName,
        String contactPerson,      // Ansprechperson (falls bekannt)
        String companyStreet,      // Strasse der Firma (optional)
        String companyZipCity,     // PLZ Ort der Firma (optional)

        // Meta
        String place,              // Ort für die Datumszeile
        String letterDate,         // Datum (z.B. 01.07.2026); leer -> heute
        String targetJob,          // Beruf/Lehrstelle (für Betreff + Text)
        String pensum,             // optional, z.B. "80%"
        String applicationSource,  // wo gefunden (yousty, Inserat, Website …)
        String startDate,          // möglicher Lehrbeginn

        // Inhalt (Stichworte für die KI)
        String whyCompany,
        String strengths,
        String schnupperExperience,
        String availability,
        String extra) {
}

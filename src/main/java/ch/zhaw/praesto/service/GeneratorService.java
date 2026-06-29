package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Generiert Lebenslauf und Bewerbungsschreiben → .docx → Ablage im Dossier.
 *
 * Lebenslauf: KEINE KI – die Formularangaben werden direkt strukturiert ins
 * Dokument geschrieben (yousty/lehrstell-Logik: Personalien, Schulbildung,
 * Erfahrung, Sprachen, Kenntnisse, Hobbys, Referenzen).
 *
 * Bewerbungsschreiben: MIT KI – die Angaben werden in einen sauberen,
 * CH-marktgerechten Brief umgewandelt (kontingent-geschützt).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeneratorService {

    private static final String LETTER_PROMPT = """
            Du bist Bewerbungscoach für Schweizer Lehrstellen. Schreibe ein einseitiges, überzeugendes
            Bewerbungsschreiben (Motivationsbrief) auf Deutsch für eine:n 14–18-Jährige:n.

            REGELN:
            - Schweizer Rechtschreibung: "ss" statt "ß".
            - Seriös, aber jugendlich-authentisch; keine Floskeln, keine Übertreibungen.
            - Erfinde NICHTS dazu. Nutze NUR die gelieferten Angaben. Fehlt ein Feld, lass es weg.
            - Stärken NICHT aneinanderreihen, sondern mit dem gelieferten Beispiel belegen.

            STRUKTUR (genau diese Reihenfolge, gib NUR den Brieftext zurück):
            1. Empfänger: Firmenname + (falls vorhanden) Ansprechperson + Firmen-Adresse.
            2. Ort und Datum (falls Ort fehlt, weglassen).
            3. Betreff (eigene Zeile): "Bewerbung als <Beruf>".
            4. Anrede: mit Ansprechperson ("Sehr geehrte Frau …/Sehr geehrter Herr …"), sonst "Sehr geehrte Damen und Herren".
            5. Einleitung: Bezug zur Lehrstelle + wo gefunden + warum Interesse.
            6. Hauptteil: warum dieser Beruf, Bezug zur Firma, eine Stärke MIT konkretem Beispiel, Schnuppererfahrung (was gefiel).
            7. Schluss: Wunsch nach einem Vorstellungsgespräch.
            8. "Freundliche Grüsse" + Name.
            """;

    private final ChatClient chatClient;
    private final AiQuotaService aiQuotaService;
    private final DocxService docxService;
    private final DocumentService documentService;
    private final UserService userService;

    // ============================================================
    // Lebenslauf – OHNE KI
    // ============================================================

    public DocumentDTO generateCv(CvRequest req) {
        String userId = userService.getUserId();
        String schoolId = userService.getCurrentSchoolId();

        String name = joinNonBlank(" ", req.firstName(), req.lastName());
        if (name.isBlank()) name = userService.getUserName();
        String contact = joinNonBlank(" · ", req.address(), req.zipCity(), req.phone(), req.email());

        String body = buildCvBody(req);
        String stored = docxService.writeStructured("Lebenslauf_" + name, name, contact, body);

        // KEIN aiQuotaService.consume – der Lebenslauf nutzt keine KI mehr.
        return DocumentDTO.from(documentService.saveGenerated(
                userId, schoolId, DocumentCategory.LEBENSLAUF, "Lebenslauf", stored, "Lebenslauf.docx"));
    }

    private String buildCvBody(CvRequest r) {
        StringBuilder sb = new StringBuilder();

        // Über mich (+ optionale Personalien-Details)
        section(sb, "Über mich");
        if (notBlank(r.aboutMe())) sb.append(r.aboutMe().trim()).append("\n");
        if (notBlank(r.targetJob())) bullet(sb, "Angestrebte Lehrstelle: " + r.targetJob().trim());
        if (notBlank(r.birthDate())) bullet(sb, "Geburtsdatum: " + r.birthDate().trim());
        if (notBlank(r.hometown())) bullet(sb, "Heimat-/Geburtsort: " + r.hometown().trim());
        if (notBlank(r.nationality())) bullet(sb, "Nationalität: " + r.nationality().trim());

        bulletsFromLines(sb, "Schulbildung", r.education());
        bulletsFromLines(sb, "Praktische Erfahrung / Schnupperlehren", r.experience());
        bulletsFromLines(sb, "Sprachen", r.languages());
        bulletsFromCommaOrLines(sb, "Kenntnisse & Fähigkeiten", r.skills());
        bulletsFromCommaOrLines(sb, "Hobbys & Interessen", r.hobbies());
        bulletsFromLines(sb, "Referenzen", r.references());

        return sb.toString();
    }

    // ============================================================
    // Bewerbungsschreiben – MIT KI
    // ============================================================

    public DocumentDTO generateCoverLetter(CoverLetterRequest req) {
        String userId = userService.getUserId();
        String schoolId = userService.getCurrentSchoolId();

        String body = callAi(LETTER_PROMPT + "\n\nAngaben:\n" + letterFacts(req), () -> letterFallback(req));

        String company = notBlank(req.companyName()) ? req.companyName().trim() : "Firma";
        String stored = docxService.writeStructured("Bewerbung_" + company, null, null, body);

        aiQuotaService.consume(userId, schoolId, AiFeature.COVER_LETTER);
        return DocumentDTO.from(documentService.saveGenerated(
                userId, schoolId, DocumentCategory.BEWERBUNGSSCHREIBEN,
                "Bewerbung " + company, stored, "Bewerbungsschreiben.docx"));
    }

    private String letterFacts(CoverLetterRequest r) {
        StringBuilder sb = new StringBuilder();
        add(sb, "Name", r.fullName());
        add(sb, "Eigene Adresse", r.senderAddress());
        add(sb, "Ort (für Datum)", r.city());
        add(sb, "Firma", r.companyName());
        add(sb, "Firmen-Adresse", r.companyAddress());
        add(sb, "Ansprechperson", r.contactPerson());
        add(sb, "Beruf/Lehrstelle", r.targetJob());
        add(sb, "Wo gefunden (Quelle)", r.applicationSource());
        add(sb, "Möglicher Lehrbeginn", r.startDate());
        add(sb, "Warum diese Firma", r.whyCompany());
        add(sb, "Stärken (mit Beispiel)", r.strengths());
        add(sb, "Schnuppererfahrung (was gefiel)", r.schnupperExperience());
        add(sb, "Sonstiges", r.extra());
        return sb.toString();
    }

    /** Einfaches Bewerbungsschreiben direkt aus den Angaben (falls keine KI verfügbar). */
    private String letterFallback(CoverLetterRequest r) {
        String name = notBlank(r.fullName()) ? r.fullName().trim() : "";
        String job = notBlank(r.targetJob()) ? r.targetJob().trim() : "eine Lehrstelle";
        StringBuilder sb = new StringBuilder();
        if (notBlank(r.companyName())) sb.append(r.companyName().trim()).append("\n");
        if (notBlank(r.contactPerson())) sb.append(r.contactPerson().trim()).append("\n");
        if (notBlank(r.companyAddress())) sb.append(r.companyAddress().trim()).append("\n");
        sb.append("\nBetreff: Bewerbung als ").append(job).append("\n\n");
        sb.append(notBlank(r.contactPerson()) ? "Sehr geehrte:r " + r.contactPerson().trim() + "\n\n"
                : "Sehr geehrte Damen und Herren\n\n");
        sb.append("Mit grossem Interesse bewerbe ich mich um eine Lehrstelle als ").append(job).append(".");
        if (notBlank(r.whyCompany())) sb.append(" ").append(r.whyCompany().trim());
        sb.append("\n\n");
        if (notBlank(r.strengths())) sb.append("Zu meinen Stärken zählen: ").append(r.strengths().trim()).append(".\n\n");
        if (notBlank(r.schnupperExperience())) sb.append(r.schnupperExperience().trim()).append("\n\n");
        sb.append("Über eine Einladung zu einem Vorstellungsgespräch freue ich mich sehr.\n\n");
        sb.append("Freundliche Grüsse\n").append(name).append("\n");
        return sb.toString();
    }

    // ============================================================
    // Intern
    // ============================================================

    private String callAi(String prompt, java.util.function.Supplier<String> fallback) {
        try {
            String result = chatClient.prompt(prompt).call().content();
            return (result != null && !result.isBlank()) ? result : fallback.get();
        } catch (Exception e) {
            log.warn("KI-Generierung fehlgeschlagen, nutze Fallback: {}", e.getMessage());
            return fallback.get();
        }
    }

    private void section(StringBuilder sb, String title) {
        sb.append("## ").append(title).append("\n");
    }

    private void bullet(StringBuilder sb, String text) {
        sb.append("- ").append(text).append("\n");
    }

    /** Abschnitt nur anlegen, wenn Inhalt da ist; jede nicht-leere Zeile = ein Punkt. */
    private void bulletsFromLines(StringBuilder sb, String title, String multiline) {
        if (!notBlank(multiline)) return;
        section(sb, title);
        for (String line : multiline.split("\\r?\\n")) {
            if (notBlank(line)) bullet(sb, line.trim());
        }
    }

    /** Wie oben, akzeptiert auch eine kommagetrennte Liste in EINER Zeile. */
    private void bulletsFromCommaOrLines(StringBuilder sb, String title, String value) {
        if (!notBlank(value)) return;
        String[] parts = value.contains("\n") ? value.split("\\r?\\n") : value.split(",");
        section(sb, title);
        for (String p : parts) {
            if (notBlank(p)) bullet(sb, p.trim());
        }
    }

    private void add(StringBuilder sb, String label, String value) {
        if (notBlank(value)) {
            sb.append(label).append(": ").append(value.trim()).append("\n");
        }
    }

    private String joinNonBlank(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (notBlank(p)) {
                if (sb.length() > 0) sb.append(sep);
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}

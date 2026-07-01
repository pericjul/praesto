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
            Du bist Bewerbungscoach für Schweizer Lehrstellen. Schreibe NUR den Fliesstext (Hauptteil)
            eines überzeugenden Bewerbungsschreibens auf Deutsch für eine:n 14–18-Jährige:n.

            SEHR WICHTIG:
            - Gib AUSSCHLIESSLICH die Text-Absätze zurück – KEINE Adresse, KEIN Datum, KEINEN Betreff,
              KEINE Anrede, KEINEN Gruss und KEINEN Namen (das wird separat gesetzt).
            - 3 bis 4 kurze Absätze, jeweils durch EINE Leerzeile getrennt.
            - Schweizer Rechtschreibung ("ss" statt "ß"). Seriös, aber jugendlich-authentisch, keine Floskeln,
              keine Übertreibungen. Natürlich und flüssig formuliert.
            - Erfinde NICHTS dazu; nutze nur die gelieferten Angaben. Fehlt etwas, lass es weg.

            AUFBAU:
            1. Einstieg: Bezug zur Stelle/Lehrstelle, wo gefunden, warum sie dich anspricht.
            2. Warum genau diese Firma.
            3. Eine Stärke MIT konkretem Beispiel und die Schnuppererfahrung (was dir gefiel).
            4. Abschluss: Wunsch nach einem persönlichen Gespräch, ggf. Verfügbarkeit / möglicher Beginn.
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

        String first = req.firstName() == null ? "" : req.firstName().trim();
        String last = req.lastName() == null ? "" : req.lastName().trim();
        String name = joinNonBlank(" ", first, last);
        if (name.isBlank()) name = userService.getUserName();
        String subtitle = notBlank(req.targetJob()) ? "Bewerbung als " + req.targetJob().trim() : "Lebenslauf";

        // Personalien (Label, Wert) – leere Felder fallen weg, Werte dürfen mehrzeilig sein.
        java.util.List<String[]> personal = new java.util.ArrayList<>();
        pair(personal, "Vorname", first);
        pair(personal, "Nachname", last);
        pair(personal, "Adresse", joinNonBlank("\n", req.address(), req.zipCity()));
        pair(personal, "Telefon", req.phone());
        pair(personal, "E-Mail", req.email());
        pair(personal, "Geburtsdatum", req.birthDate());
        pair(personal, "Heimatort", req.hometown());
        pair(personal, "Nationalität", req.nationality());
        pair(personal, "Eltern", req.parents());
        pair(personal, "Geschwister", req.siblings());

        // Abschnitte (Titel + Zeilen), jede Zeile am ersten " · " in zwei Spalten geteilt.
        java.util.List<DocxService.CvSection> sections = new java.util.ArrayList<>();
        if (notBlank(req.aboutMe())) {
            sections.add(new DocxService.CvSection("Über mich",
                    java.util.List.<String[]>of(new String[]{"", req.aboutMe().trim()})));
        }
        addSection(sections, "Schulbildung", req.education());
        addSection(sections, "Praktische Erfahrung / Schnupperlehren", req.experience());
        addSection(sections, "Sprachen", req.languages());
        addCommaSection(sections, "Kenntnisse & Fähigkeiten", req.skills());
        addCommaSection(sections, "Hobbys & Interessen", req.hobbies());
        addSection(sections, "Referenzen", req.references());

        String stored = docxService.writeCv("Lebenslauf_" + name, name, subtitle, personal, sections, req.photoUrl());

        // KEIN aiQuotaService.consume – der Lebenslauf nutzt keine KI.
        return DocumentDTO.from(documentService.saveGenerated(
                userId, schoolId, DocumentCategory.LEBENSLAUF, "Lebenslauf", stored, "Lebenslauf.docx"));
    }

    private void pair(java.util.List<String[]> list, String label, String value) {
        if (notBlank(value)) list.add(new String[]{label, value.trim()});
    }

    /** Mehrzeiliges Feld → je Zeile eine Abschnittszeile, am ersten " · " zweispaltig geteilt. */
    private void addSection(java.util.List<DocxService.CvSection> sections, String title, String multiline) {
        if (!notBlank(multiline)) return;
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        for (String line : multiline.split("\\r?\\n")) {
            if (notBlank(line)) rows.add(splitRow(line.trim()));
        }
        if (!rows.isEmpty()) sections.add(new DocxService.CvSection(title, rows));
    }

    /** Komma- oder zeilenweise Liste → je Eintrag eine Zeile. */
    private void addCommaSection(java.util.List<DocxService.CvSection> sections, String title, String value) {
        if (!notBlank(value)) return;
        String[] parts = value.contains("\n") ? value.split("\\r?\\n") : value.split(",");
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        for (String p : parts) {
            if (notBlank(p)) rows.add(splitRow(p.trim()));
        }
        if (!rows.isEmpty()) sections.add(new DocxService.CvSection(title, rows));
    }

    /** Teilt "links · rechts" (oder "links – rechts") in zwei Spalten; sonst volle Breite rechts. */
    private String[] splitRow(String line) {
        int i = line.indexOf(" · ");
        if (i < 0) i = line.indexOf(" – ");
        if (i >= 0) return new String[]{line.substring(0, i).trim(), line.substring(i + 3).trim()};
        return new String[]{"", line};
    }

    // ============================================================
    // Bewerbungsschreiben – MIT KI
    // ============================================================

    public DocumentDTO generateCoverLetter(CoverLetterRequest req) {
        String userId = userService.getUserId();
        String schoolId = userService.getCurrentSchoolId();

        String name = notBlank(req.fullName()) ? req.fullName().trim() : userService.getUserName();
        String job = notBlank(req.targetJob()) ? req.targetJob().trim() : "eine Lehrstelle";
        String subject = "Bewerbung als " + job + (notBlank(req.pensum()) ? " " + req.pensum().trim() : "");

        String salutation;
        if (notBlank(req.contactPerson())) {
            salutation = "Sehr geehrte:r " + req.contactPerson().trim();
        } else if (notBlank(req.companyName())) {
            salutation = "Sehr geehrtes Team der " + req.companyName().trim();
        } else {
            salutation = "Sehr geehrte Damen und Herren";
        }

        String place = notBlank(req.place()) ? req.place().trim() : cityOnly(req.senderZipCity());
        String date = notBlank(req.letterDate()) ? req.letterDate().trim()
                : java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        String placeDate = notBlank(place) ? place + ", " + date : date;

        String body = callAi(LETTER_PROMPT + "\n\nAngaben:\n" + letterFacts(req), () -> letterFallback(req));

        String company = notBlank(req.companyName()) ? req.companyName().trim() : "Firma";
        String stored = docxService.writeLetter("Bewerbung_" + company,
                name, req.senderStreet(), req.senderZipCity(), req.phone(), req.email(),
                req.companyName(), req.contactPerson(), req.companyStreet(), req.companyZipCity(),
                placeDate, subject, salutation, body, name);

        aiQuotaService.consume(userId, schoolId, AiFeature.COVER_LETTER);
        return DocumentDTO.from(documentService.saveGenerated(
                userId, schoolId, DocumentCategory.BEWERBUNGSSCHREIBEN,
                "Bewerbung " + company, stored, "Bewerbungsschreiben.docx"));
    }

    /** Ort aus "PLZ Ort" extrahieren (führende Zahl entfernen). */
    private String cityOnly(String zipCity) {
        if (!notBlank(zipCity)) return "";
        return zipCity.trim().replaceFirst("^\\d+\\s*", "").trim();
    }

    /** Angaben für die KI (nur der Brieftext, keine Adresse/Anrede). */
    private String letterFacts(CoverLetterRequest r) {
        StringBuilder sb = new StringBuilder();
        add(sb, "Name", r.fullName());
        add(sb, "Firma", r.companyName());
        add(sb, "Beruf/Stelle/Lehrstelle", r.targetJob());
        add(sb, "Pensum", r.pensum());
        add(sb, "Wo gefunden (Quelle)", r.applicationSource());
        add(sb, "Möglicher Beginn", r.startDate());
        add(sb, "Warum diese Firma", r.whyCompany());
        add(sb, "Stärken (mit Beispiel)", r.strengths());
        add(sb, "Schnuppererfahrung (was gefiel)", r.schnupperExperience());
        add(sb, "Verfügbarkeit (Gespräch/Schnuppertage)", r.availability());
        add(sb, "Sonstiges", r.extra());
        return sb.toString();
    }

    /** Einfacher Brieftext (nur Absätze), falls keine KI verfügbar. */
    private String letterFallback(CoverLetterRequest r) {
        String job = notBlank(r.targetJob()) ? r.targetJob().trim() : "eine Lehrstelle";
        StringBuilder sb = new StringBuilder();
        sb.append("Mit grossem Interesse bewerbe ich mich um die Stelle als ").append(job);
        if (notBlank(r.applicationSource())) sb.append(", die ich auf ").append(r.applicationSource().trim()).append(" gefunden habe");
        sb.append(".");
        if (notBlank(r.whyCompany())) sb.append(" ").append(r.whyCompany().trim());
        sb.append("\n\n");
        if (notBlank(r.strengths())) sb.append("Zu meinen Stärken zählen: ").append(r.strengths().trim()).append(". ");
        if (notBlank(r.schnupperExperience())) sb.append(r.schnupperExperience().trim());
        sb.append("\n\n");
        sb.append("Über die Möglichkeit, mich in einem persönlichen Gespräch vorzustellen, freue ich mich sehr.");
        if (notBlank(r.availability())) sb.append(" Verfügbar bin ich ").append(r.availability().trim()).append(".");
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

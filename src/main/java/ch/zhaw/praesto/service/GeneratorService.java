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
        add(sb, "Telefon", r.phone());
        add(sb, "E-Mail", r.email());
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
        add(sb, "Verfügbarkeit (Gespräch/Schnuppertage)", r.availability());
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

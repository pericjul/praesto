package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Generiert Lebenslauf und Bewerbungsschreiben: aus den Umfrage-Antworten EIN
 * KI-Aufruf (token-sparend) → .docx → ablegen im Dossier. Kontingent-geschützt.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeneratorService {

    private static final String CV_PROMPT = """
            Erstelle aus den Angaben einer Schüler:in (15–18) einen professionellen, ehrlichen und KNAPPEN
            Lebenslauf für eine Lehrstellenbewerbung in der Schweiz. Gib NUR den Inhalt zurück, gegliedert mit:
            - "## " vor jedem Abschnittstitel
            - "- " vor Aufzählungspunkten
            Abschnitte in dieser Reihenfolge: "Über mich" (2–3 Sätze), "Ausbildung", "Praktische Erfahrung",
            "Fähigkeiten", "Sprachen", "Hobbys". Erfinde NICHTS dazu – nutze nur die Angaben.
            """;

    private static final String LETTER_PROMPT = """
            Schreibe ein überzeugendes, ehrliches und knappes Bewerbungsschreiben (Motivationsbrief) auf Deutsch
            für eine Lehrstelle in der Schweiz, aus Sicht einer Schüler:in (15–18). Gib NUR den Brieftext zurück
            (Empfänger, Betreff "Bewerbung als ...", Anrede, 3 kurze Absätze, Grussformel "Freundliche Grüsse" + Name).
            Klinge natürlich und altersgerecht, nicht übertrieben. Erfinde NICHTS dazu – nutze nur die Angaben.
            """;

    private final ChatClient chatClient;
    private final AiQuotaService aiQuotaService;
    private final DocxService docxService;
    private final DocumentService documentService;
    private final UserService userService;

    // ============================================================
    // Lebenslauf
    // ============================================================

    public DocumentDTO generateCv(CvRequest req) {
        String userId = userService.getUserId();
        String schoolId = userService.getCurrentSchoolId();
        String name = notBlank(req.fullName()) ? req.fullName().trim() : userService.getUserName();

        String body = callAi(CV_PROMPT + "\n\nAngaben:\n" + cvFacts(req), () -> cvFallback(req));

        String contact = joinNonBlank(" · ", req.address(), req.phone(), req.email());
        String stored = docxService.writeStructured("Lebenslauf_" + name, name, contact, body);

        aiQuotaService.consume(userId, schoolId, AiFeature.CV);
        return DocumentDTO.from(documentService.saveGenerated(
                userId, schoolId, DocumentCategory.LEBENSLAUF, "Lebenslauf", stored, "Lebenslauf.docx"));
    }

    // ============================================================
    // Bewerbungsschreiben
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

    private String cvFacts(CvRequest r) {
        StringBuilder sb = new StringBuilder();
        add(sb, "Name", r.fullName());
        add(sb, "Geburtsdatum", r.birthDate());
        add(sb, "Wunschberuf", r.targetJob());
        add(sb, "Aktuelle Schule/Klasse", r.school());
        add(sb, "Praktische Erfahrung / Schnupperlehren", r.experience());
        add(sb, "Stärken/Fähigkeiten", r.skills());
        add(sb, "Sprachen", r.languages());
        add(sb, "Hobbys", r.hobbies());
        add(sb, "Sonstiges", r.extra());
        return sb.toString();
    }

    private String letterFacts(CoverLetterRequest r) {
        StringBuilder sb = new StringBuilder();
        add(sb, "Name", r.fullName());
        add(sb, "Eigene Adresse", r.senderAddress());
        add(sb, "Firma", r.companyName());
        add(sb, "Firmen-Adresse", r.companyAddress());
        add(sb, "Wunschberuf/Lehrstelle", r.targetJob());
        add(sb, "Warum diese Firma", r.whyCompany());
        add(sb, "Stärken", r.strengths());
        add(sb, "Möglicher Lehrbeginn", r.startDate());
        add(sb, "Sonstiges", r.extra());
        return sb.toString();
    }

    /** Einfacher Lebenslauf direkt aus den Angaben (falls keine KI verfügbar). */
    private String cvFallback(CvRequest r) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Über mich\n");
        sb.append(notBlank(r.targetJob()) ? "Ich suche eine Lehrstelle als " + r.targetJob().trim() + ".\n" : "\n");
        sb.append("\n## Ausbildung\n");
        if (notBlank(r.school())) sb.append("- ").append(r.school().trim()).append("\n");
        sb.append("\n## Praktische Erfahrung\n");
        if (notBlank(r.experience())) sb.append("- ").append(r.experience().trim()).append("\n");
        sb.append("\n## Fähigkeiten\n");
        if (notBlank(r.skills())) sb.append("- ").append(r.skills().trim()).append("\n");
        sb.append("\n## Sprachen\n");
        if (notBlank(r.languages())) sb.append("- ").append(r.languages().trim()).append("\n");
        sb.append("\n## Hobbys\n");
        if (notBlank(r.hobbies())) sb.append("- ").append(r.hobbies().trim()).append("\n");
        return sb.toString();
    }

    /** Einfaches Bewerbungsschreiben direkt aus den Angaben (falls keine KI verfügbar). */
    private String letterFallback(CoverLetterRequest r) {
        String name = notBlank(r.fullName()) ? r.fullName().trim() : "";
        String job = notBlank(r.targetJob()) ? r.targetJob().trim() : "eine Lehrstelle";
        StringBuilder sb = new StringBuilder();
        if (notBlank(r.companyName())) sb.append(r.companyName().trim()).append("\n");
        if (notBlank(r.companyAddress())) sb.append(r.companyAddress().trim()).append("\n");
        sb.append("\nBetreff: Bewerbung als ").append(job).append("\n\n");
        sb.append("Sehr geehrte Damen und Herren\n\n");
        sb.append("Mit grossem Interesse bewerbe ich mich um eine Lehrstelle als ").append(job).append(".");
        if (notBlank(r.whyCompany())) sb.append(" ").append(r.whyCompany().trim());
        sb.append("\n\n");
        if (notBlank(r.strengths())) sb.append("Zu meinen Stärken zählen: ").append(r.strengths().trim()).append(".\n\n");
        sb.append("Über die Einladung zu einem Gespräch freue ich mich sehr.\n\n");
        sb.append("Freundliche Grüsse\n").append(name).append("\n");
        return sb.toString();
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

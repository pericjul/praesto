package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.KnowledgeSource;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.repository.KnowledgeSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;

/**
 * Globale KI-Wissensbasis (nur Super-Admin). Extrahiert Text aus Links (Webseiten),
 * Dokumenten (PDF/Word/TXT) oder Freitext und stellt ihn dem KI-Coach als zusätzlichen
 * Kontext zur Verfügung. Die eigentliche Text-Extraktion passiert einmal beim Hinzufügen.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeService {

    /** Max. Textlänge pro Quelle (Kostenschutz). */
    private static final int PER_SOURCE_MAX = 8000;
    /** Max. Gesamt-Wissen, das der KI mitgegeben wird. */
    private static final int TOTAL_MAX = 10000;

    private final KnowledgeSourceRepository repository;
    private final UserService userService;
    private final KnowledgeRagService ragService;

    // ============================================================
    // Super-Admin: verwalten
    // ============================================================

    public List<KnowledgeSource> list() {
        requireSuper();
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public KnowledgeSource addLink(String url, String title) {
        requireSuper();
        if (url == null || url.isBlank()) {
            throw new BadRequestException("Bitte eine URL angeben.");
        }
        String normalized = url.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        String text;
        try {
            text = Jsoup.connect(normalized)
                    .userAgent("Mozilla/5.0 (PraestoBot)")
                    .timeout(15000)
                    .get()
                    .body()
                    .text();
        } catch (Exception e) {
            throw new BadRequestException("Die Seite konnte nicht gelesen werden: " + e.getMessage());
        }
        return saveAndIndex(KnowledgeSource.builder()
                .type(KnowledgeSource.Type.LINK)
                .title(title != null && !title.isBlank() ? title.trim() : normalized)
                .url(normalized)
                .content(trim(text))
                .enabled(true)
                .createdAt(Instant.now())
                .build());
    }

    public KnowledgeSource addDocument(byte[] bytes, String fileName, String title) {
        requireSuper();
        if (bytes == null || bytes.length == 0) {
            throw new BadRequestException("Keine Datei ausgewählt.");
        }
        String name = fileName == null ? "dokument" : fileName;
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
        String text = switch (ext) {
            case "pdf" -> extractPdf(bytes);
            case "docx" -> extractDocx(bytes);
            case "txt" -> new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            default -> throw new BadRequestException("Nur PDF, Word (.docx) oder TXT werden unterstützt.");
        };
        if (text == null || text.isBlank()) {
            throw new BadRequestException("Aus dieser Datei konnte kein Text gelesen werden.");
        }
        return saveAndIndex(KnowledgeSource.builder()
                .type(KnowledgeSource.Type.DOCUMENT)
                .title(title != null && !title.isBlank() ? title.trim() : name)
                .fileName(name)
                .content(trim(text))
                .enabled(true)
                .createdAt(Instant.now())
                .build());
    }

    public KnowledgeSource addText(String title, String text) {
        requireSuper();
        if (text == null || text.isBlank()) {
            throw new BadRequestException("Bitte einen Text eingeben.");
        }
        return saveAndIndex(KnowledgeSource.builder()
                .type(KnowledgeSource.Type.TEXT)
                .title(title != null && !title.isBlank() ? title.trim() : "Notiz")
                .content(trim(text))
                .enabled(true)
                .createdAt(Instant.now())
                .build());
    }

    /** Speichert eine Quelle und indexiert sie (best-effort) für die Vektorsuche. */
    private KnowledgeSource saveAndIndex(KnowledgeSource source) {
        KnowledgeSource saved = repository.save(source);
        reindexInBackground(saved);
        return saved;
    }

    /**
     * Indexierung (Embedding + Vektorsuche) im Hintergrund, damit das Speichern sofort
     * zurückkommt und nie am (evtl. langsamen) KI-Aufruf hängt. Best-effort.
     */
    private void reindexInBackground(KnowledgeSource source) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                ragService.reindex(source);
            } catch (Exception e) {
                log.error("Hintergrund-Indexierung fehlgeschlagen: {}", e.getMessage());
            }
        });
    }

    public void setEnabled(String id, boolean enabled) {
        requireSuper();
        KnowledgeSource src = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quelle nicht gefunden"));
        src.setEnabled(enabled);
        repository.save(src);
        // Vektor-Index nachziehen: aktivieren -> (neu) indexieren, deaktivieren -> Häppchen entfernen.
        if (enabled) {
            reindexInBackground(src);
        } else {
            ragService.remove(id);
        }
    }

    public void delete(String id) {
        requireSuper();
        ragService.remove(id);
        repository.deleteById(id);
    }

    // ============================================================
    // Für den KI-Coach: aktives Wissen als Kontext-Text
    // ============================================================

    /**
     * Wissens-Kontext für eine konkrete Schülerfrage. Nutzt – wenn verfügbar – die
     * Vektorsuche (nur die relevantesten Häppchen). Sonst Fallback: alle aktiven Quellen
     * als Text (gedeckelt). KEIN Rollen-Check – wird intern vom KI-Coach genutzt.
     */
    public String getKnowledgeForQuery(String query) {
        String viaRag = ragService.retrieve(query);
        if (viaRag != null && !viaRag.isBlank()) {
            return viaRag;
        }
        return getActiveKnowledgeText();
    }

    /**
     * Zusammengefasster Text aller aktiven Quellen (gedeckelt). Leer, wenn nichts vorhanden.
     * KEIN Rollen-Check – wird intern vom KI-Coach genutzt (Fallback ohne Vektorsuche).
     */
    public String getActiveKnowledgeText() {
        StringBuilder sb = new StringBuilder();
        for (KnowledgeSource s : repository.findByEnabledTrue()) {
            if (s.getContent() == null || s.getContent().isBlank()) {
                continue;
            }
            if (sb.length() >= TOTAL_MAX) {
                break;
            }
            sb.append("### ").append(s.getTitle()).append("\n");
            int remaining = TOTAL_MAX - sb.length();
            String c = s.getContent();
            sb.append(c.length() > remaining ? c.substring(0, remaining) : c).append("\n\n");
        }
        return sb.toString().trim();
    }

    // ============================================================
    // Intern
    // ============================================================

    private String extractPdf(byte[] bytes) {
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(doc);
        } catch (Exception e) {
            throw new BadRequestException("PDF konnte nicht gelesen werden: " + e.getMessage());
        }
    }

    private String extractDocx(byte[] bytes) {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes));
             XWPFWordExtractor ex = new XWPFWordExtractor(doc)) {
            return ex.getText();
        } catch (Exception e) {
            throw new BadRequestException("Word-Dokument konnte nicht gelesen werden: " + e.getMessage());
        }
    }

    private static String trim(String s) {
        if (s == null) {
            return "";
        }
        String cleaned = s.replaceAll("[ \\t]+", " ").trim();
        return cleaned.length() > PER_SOURCE_MAX ? cleaned.substring(0, PER_SOURCE_MAX) : cleaned;
    }

    private void requireSuper() {
        if (!userService.userHasRole(UserRole.SUPER_ADMIN)) {
            throw new ForbiddenException("Nur der Super-Admin kann die Wissensbasis verwalten.");
        }
    }
}

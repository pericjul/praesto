package ch.zhaw.praesto.service;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Erzeugt Word-Dokumente (.docx) aus einfachem Markup und legt sie im Upload-Ordner ab.
 * Markup: Zeilen mit "## " = Abschnittstitel, "- " = Aufzählungspunkt, sonst Absatz.
 */
@Service
public class DocxService {

    private final Path uploadDir;

    public DocxService(@Value("${praesto.uploads.dir:uploads}") String uploadsDir) {
        this.uploadDir = Paths.get(uploadsDir).toAbsolutePath().normalize();
    }

    /**
     * Rendert Überschrift + Unterzeile + Markup-Body in eine .docx und gibt den
     * gespeicherten Dateinamen zurück (für /api/files/{name}).
     */
    public String writeStructured(String fileBaseName, String heading, String subheading, String body) {
        try (XWPFDocument doc = new XWPFDocument()) {
            if (heading != null && !heading.isBlank()) {
                XWPFParagraph p = doc.createParagraph();
                XWPFRun r = p.createRun();
                r.setBold(true);
                r.setFontSize(20);
                r.setText(heading.trim());
            }
            if (subheading != null && !subheading.isBlank()) {
                XWPFParagraph p = doc.createParagraph();
                XWPFRun r = p.createRun();
                r.setFontSize(10);
                r.setColor("666666");
                r.setText(subheading.trim());
            }

            for (String raw : (body == null ? "" : body).split("\n", -1)) {
                String line = raw.strip();
                if (line.isEmpty()) {
                    doc.createParagraph();
                    continue;
                }
                if (line.startsWith("## ")) {
                    XWPFParagraph p = doc.createParagraph();
                    p.setSpacingBefore(160);
                    XWPFRun r = p.createRun();
                    r.setBold(true);
                    r.setFontSize(13);
                    r.setText(line.substring(3).trim());
                } else if (line.startsWith("- ") || line.startsWith("• ")) {
                    XWPFParagraph p = doc.createParagraph();
                    p.setIndentationLeft(360);
                    XWPFRun r = p.createRun();
                    r.setText("•  " + line.substring(2).trim());
                } else {
                    XWPFParagraph p = doc.createParagraph();
                    XWPFRun r = p.createRun();
                    r.setText(line);
                }
            }

            Files.createDirectories(uploadDir);
            String safe = (fileBaseName == null ? "dokument" : fileBaseName)
                    .replaceAll("[^a-zA-Z0-9._-]", "_");
            String storedName = UUID.randomUUID() + "_" + safe + ".docx";
            Path target = uploadDir.resolve(storedName).normalize();
            try (OutputStream os = Files.newOutputStream(target)) {
                doc.write(os);
            }
            return storedName;
        } catch (Exception e) {
            throw new RuntimeException("Word-Dokument konnte nicht erstellt werden: " + e.getMessage(), e);
        }
    }
}

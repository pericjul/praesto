package ch.zhaw.praesto.service;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
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
        return writeStructured(fileBaseName, heading, subheading, body, null);
    }

    /**
     * Wie oben, zusätzlich mit optionalem Bewerbungsfoto (gespeicherter Dateiname im
     * Upload-Ordner). Das Foto wird oben rechts eingefügt. Schlägt das Einbetten fehl
     * (z.B. ungültiges Bild), wird das Dokument trotzdem ohne Foto erstellt.
     */
    public String writeStructured(String fileBaseName, String heading, String subheading, String body,
                                  String photoStoredName) {
        try (XWPFDocument doc = new XWPFDocument()) {
            embedPhotoIfPresent(doc, photoStoredName);
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

    /** Fügt das Foto oben rechts ein. Optional – bei Problemen wird es einfach weggelassen. */
    private void embedPhotoIfPresent(XWPFDocument doc, String photoStoredName) {
        if (photoStoredName == null || photoStoredName.isBlank()) {
            return;
        }
        try {
            Path img = uploadDir.resolve(photoStoredName).normalize();
            if (!img.startsWith(uploadDir) || !Files.exists(img)) {
                return;
            }
            int type = pictureType(photoStoredName);
            if (type <= 0) {
                return;
            }
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun r = p.createRun();
            try (InputStream in = Files.newInputStream(img)) {
                // ca. 3.3 × 4.5 cm (Hochformat, übliche Bewerbungsfoto-Grösse)
                r.addPicture(in, type, photoStoredName, Units.toEMU(95), Units.toEMU(127));
            }
        } catch (Exception ignored) {
            // Foto ist optional – Dokument wird notfalls ohne erstellt.
        }
    }

    private int pictureType(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) {
            return Document.PICTURE_TYPE_JPEG;
        }
        if (n.endsWith(".png")) {
            return Document.PICTURE_TYPE_PNG;
        }
        if (n.endsWith(".gif")) {
            return Document.PICTURE_TYPE_GIF;
        }
        return -1;
    }
}

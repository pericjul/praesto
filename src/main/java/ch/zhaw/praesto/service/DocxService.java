package ch.zhaw.praesto.service;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Erzeugt Word-Dokumente (.docx) und legt sie im Upload-Ordner ab.
 *
 * - {@link #writeStructured} rendert einfaches Markup (für das Bewerbungsschreiben).
 * - {@link #writeCv} erzeugt einen sauber formatierten Lebenslauf (Kopf mit Foto,
 *   zweispaltige Personalien, Abschnitte mit Trennlinie).
 */
@Service
public class DocxService {

    private static final String FONT = "Calibri";
    private static final String GREY = "666666";
    /** Tab-Position der Wert-Spalte (Twips ≈ 3.3 cm). */
    private static final int VALUE_TAB = 1850;

    private final Path uploadDir;

    public DocxService(@Value("${praesto.uploads.dir:uploads}") String uploadsDir) {
        this.uploadDir = Paths.get(uploadsDir).toAbsolutePath().normalize();
    }

    /** Ein Lebenslauf-Abschnitt: Titel + Zeilen, jede Zeile [links, rechts]. */
    public record CvSection(String title, List<String[]> rows) {}

    // ============================================================
    // Lebenslauf – sauberes Layout
    // ============================================================

    public String writeCv(String fileBaseName, String fullName, String subtitle,
                          List<String[]> personal, List<CvSection> sections, String photoStoredName) {
        try (XWPFDocument doc = new XWPFDocument()) {
            byte[] photo = loadPhotoPng(photoStoredName);

            if (photo != null) {
                // Kopf: linke Spalte (Name + Personalien), rechte Spalte (Foto).
                XWPFTable head = doc.createTable(1, 2);
                makeBorderless(head);
                XWPFTableCell left = head.getRow(0).getCell(0);
                XWPFTableCell right = head.getRow(0).getCell(1);
                setCellWidth(left, 7600);
                setCellWidth(right, 2600);
                clearCell(left);
                fillHeader(left::addParagraph, fullName, subtitle, personal);
                clearCell(right);
                XWPFParagraph pp = right.addParagraph();
                pp.setAlignment(ParagraphAlignment.RIGHT);
                addPhoto(pp.createRun(), photo);
            } else {
                fillHeader(doc::createParagraph, fullName, subtitle, personal);
            }

            for (CvSection s : sections) {
                if (s == null || s.rows() == null || s.rows().isEmpty()) continue;
                sectionTitle(doc, s.title());
                for (String[] row : s.rows()) {
                    XWPFParagraph p = doc.createParagraph();
                    p.setSpacingAfter(40);
                    labelValue(p, row.length > 0 ? row[0] : "", row.length > 1 ? row[1] : "", false);
                }
            }

            return save(doc, fileBaseName);
        } catch (Exception e) {
            throw new RuntimeException("Lebenslauf konnte nicht erstellt werden: " + e.getMessage(), e);
        }
    }

    /** Füllt den Kopf (Name gross, Untertitel, Personalien-Paare) über einen Absatz-Lieferanten. */
    private void fillHeader(java.util.function.Supplier<XWPFParagraph> newPara,
                            String fullName, String subtitle, List<String[]> personal) {
        XWPFParagraph name = newPara.get();
        name.setSpacingAfter(20);
        XWPFRun nr = name.createRun();
        nr.setBold(true);
        nr.setFontSize(22);
        nr.setFontFamily(FONT);
        nr.setColor("1F1830");
        nr.setText(fullName == null || fullName.isBlank() ? "Lebenslauf" : fullName.trim());

        if (subtitle != null && !subtitle.isBlank()) {
            XWPFParagraph sub = newPara.get();
            sub.setSpacingAfter(120);
            XWPFRun sr = sub.createRun();
            sr.setFontSize(11);
            sr.setFontFamily(FONT);
            sr.setColor(GREY);
            sr.setText(subtitle.trim());
        }

        if (personal != null) {
            for (String[] pair : personal) {
                XWPFParagraph p = newPara.get();
                p.setSpacingAfter(20);
                labelValue(p, pair.length > 0 ? pair[0] : "", pair.length > 1 ? pair[1] : "", true);
            }
        }
    }

    /** Eine Zeile "Label [Tab] Wert"; mehrzeilige Werte werden in der Wert-Spalte ausgerichtet. */
    private void labelValue(XWPFParagraph p, String label, String value, boolean labelBold) {
        addTabStop(p, VALUE_TAB);
        if (label != null && !label.isBlank()) {
            XWPFRun l = p.createRun();
            l.setFontFamily(FONT);
            l.setFontSize(10);
            l.setBold(labelBold);
            l.setText(label.trim());
        }
        XWPFRun v = p.createRun();
        v.setFontFamily(FONT);
        v.setFontSize(10);
        String[] lines = (value == null ? "" : value).split("\\r?\\n", -1);
        v.addTab();
        v.setText(lines.length > 0 ? lines[0] : "");
        for (int i = 1; i < lines.length; i++) {
            v.addBreak();
            v.addTab();
            v.setText(lines[i]);
        }
    }

    private void sectionTitle(XWPFDocument doc, String title) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(260);
        p.setSpacingAfter(80);
        p.setBorderBottom(Borders.SINGLE);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(12);
        r.setFontFamily(FONT);
        r.setColor("2F124D");
        r.setText(title);
    }

    private void addTabStop(XWPFParagraph p, int twips) {
        CTPPr ppr = p.getCTP().isSetPPr() ? p.getCTP().getPPr() : p.getCTP().addNewPPr();
        CTTabs tabs = ppr.isSetTabs() ? ppr.getTabs() : ppr.addNewTabs();
        CTTabStop ts = tabs.addNewTab();
        ts.setVal(STTabJc.LEFT);
        ts.setPos(BigInteger.valueOf(twips));
    }

    // ----- Tabellen-Helfer (für den Kopf mit Foto) -----

    private void makeBorderless(XWPFTable table) {
        CTTblPr pr = table.getCTTbl().getTblPr() != null
                ? table.getCTTbl().getTblPr() : table.getCTTbl().addNewTblPr();
        CTTblBorders b = pr.isSetTblBorders() ? pr.getTblBorders() : pr.addNewTblBorders();
        b.addNewTop().setVal(STBorder.NONE);
        b.addNewBottom().setVal(STBorder.NONE);
        b.addNewLeft().setVal(STBorder.NONE);
        b.addNewRight().setVal(STBorder.NONE);
        b.addNewInsideH().setVal(STBorder.NONE);
        b.addNewInsideV().setVal(STBorder.NONE);
    }

    private void setCellWidth(XWPFTableCell cell, int twips) {
        CTTcPr pr = cell.getCTTc().getTcPr() != null ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTTblWidth w = pr.isSetTcW() ? pr.getTcW() : pr.addNewTcW();
        w.setType(STTblWidth.DXA);
        w.setW(BigInteger.valueOf(twips));
    }

    /** Standard-Leerabsatz einer frischen Tabellenzelle entfernen, damit kein Extra-Abstand entsteht. */
    private void clearCell(XWPFTableCell cell) {
        // Zelle hat genau einen leeren Absatz – wir lassen ihn und befüllen via addParagraph();
        // der erste leere Absatz wird durch Inhalt nicht stören (geringer Abstand).
    }

    // ----- Foto -----

    /** Foto top-rechts. Erwartet bereits gültige PNG-Bytes (siehe loadPhotoPng). */
    private void addPhoto(XWPFRun run, byte[] pngBytes) throws Exception {
        // Anzeige ~3.3 × 4.5 cm; Seitenverhältnis wird durch das Re-Encoding gewahrt.
        try (ByteArrayInputStream in = new ByteArrayInputStream(pngBytes)) {
            run.addPicture(in, Document.PICTURE_TYPE_PNG, "foto.png", Units.toEMU(95), Units.toEMU(127));
        }
    }

    /**
     * Liest das hochgeladene Foto, skaliert es und kodiert es NEU als sauberes PNG.
     * So zeigt Word das Bild zuverlässig an (kein „Bild kann nicht angezeigt werden"),
     * auch wenn das Original ein problematisches JPEG war. HEIC o.ä. (von ImageIO nicht
     * lesbar) wird einfach weggelassen.
     */
    private byte[] loadPhotoPng(String storedName) {
        if (storedName == null || storedName.isBlank()) return null;
        try {
            Path img = uploadDir.resolve(storedName).normalize();
            if (!img.startsWith(uploadDir) || !Files.exists(img)) return null;
            BufferedImage src = ImageIO.read(img.toFile());
            if (src == null || src.getWidth() <= 0) return null;

            int targetW = 360;
            int targetH = Math.max(1, Math.round(src.getHeight() * (targetW / (float) src.getWidth())));
            BufferedImage dst = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = dst.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH), 0, 0, null);
            g.dispose();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(dst, "png", bos);
            return bos.toByteArray();
        } catch (Exception e) {
            return null; // Foto ist optional
        }
    }

    // ============================================================
    // Generisches Markup (Bewerbungsschreiben)
    // ============================================================

    public String writeStructured(String fileBaseName, String heading, String subheading, String body) {
        try (XWPFDocument doc = new XWPFDocument()) {
            if (heading != null && !heading.isBlank()) {
                XWPFParagraph p = doc.createParagraph();
                XWPFRun r = p.createRun();
                r.setBold(true);
                r.setFontSize(20);
                r.setFontFamily(FONT);
                r.setText(heading.trim());
            }
            if (subheading != null && !subheading.isBlank()) {
                XWPFParagraph p = doc.createParagraph();
                XWPFRun r = p.createRun();
                r.setFontSize(10);
                r.setFontFamily(FONT);
                r.setColor(GREY);
                r.setText(subheading.trim());
            }
            for (String raw : (body == null ? "" : body).split("\n", -1)) {
                String line = raw.strip();
                if (line.isEmpty()) {
                    doc.createParagraph();
                    continue;
                }
                XWPFParagraph p = doc.createParagraph();
                if (line.startsWith("## ")) {
                    p.setSpacingBefore(160);
                    XWPFRun r = p.createRun();
                    r.setBold(true);
                    r.setFontSize(13);
                    r.setFontFamily(FONT);
                    r.setText(line.substring(3).trim());
                } else if (line.startsWith("- ") || line.startsWith("• ")) {
                    p.setIndentationLeft(360);
                    XWPFRun r = p.createRun();
                    r.setFontFamily(FONT);
                    r.setText("•  " + line.substring(2).trim());
                } else {
                    XWPFRun r = p.createRun();
                    r.setFontFamily(FONT);
                    r.setText(line);
                }
            }
            return save(doc, fileBaseName);
        } catch (Exception e) {
            throw new RuntimeException("Word-Dokument konnte nicht erstellt werden: " + e.getMessage(), e);
        }
    }

    /** Rückwärtskompatible Überladung (Foto wird hier ignoriert – CV nutzt writeCv). */
    public String writeStructured(String fileBaseName, String heading, String subheading, String body,
                                  String photoStoredName) {
        return writeStructured(fileBaseName, heading, subheading, body);
    }

    // ============================================================
    // Speichern
    // ============================================================

    private String save(XWPFDocument doc, String fileBaseName) throws Exception {
        Files.createDirectories(uploadDir);
        String safe = (fileBaseName == null ? "dokument" : fileBaseName)
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedName = UUID.randomUUID() + "_" + safe + ".docx";
        Path target = uploadDir.resolve(storedName).normalize();
        try (OutputStream os = Files.newOutputStream(target)) {
            doc.write(os);
        }
        return storedName;
    }
}

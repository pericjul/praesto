package ch.zhaw.praesto.service;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Befüllt die Offerten-Word-Vorlage ({@code templates/offerte-vorlage.docx}) mit den
 * im UI erfassten Werten und gibt die fertige .docx als Bytes zurück. Die Vorlage
 * enthält Platzhalter der Form {@code {{PLATZHALTER}}}.
 */
@Service
public class OfferteService {

    private static final String TEMPLATE = "templates/offerte-vorlage.docx";

    /** Ersetzt alle Platzhalter und liefert die fertige .docx. */
    public byte[] fill(Map<String, String> values) {
        try (InputStream in = new ClassPathResource(TEMPLATE).getInputStream();
             XWPFDocument doc = new XWPFDocument(in)) {

            replaceInParagraphs(doc.getParagraphs(), values);
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        replaceInParagraphs(cell.getParagraphs(), values);
                    }
                }
            }
            for (XWPFHeader h : doc.getHeaderList()) {
                replaceInParagraphs(h.getParagraphs(), values);
            }
            for (XWPFFooter f : doc.getFooterList()) {
                replaceInParagraphs(f.getParagraphs(), values);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            doc.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Offerte konnte nicht erstellt werden: " + e.getMessage(), e);
        }
    }

    private void replaceInParagraphs(List<XWPFParagraph> paras, Map<String, String> values) {
        if (paras == null) return;
        for (XWPFParagraph p : paras) {
            replaceInParagraph(p, values);
        }
    }

    /**
     * Absatzweise ersetzen: Text aller Runs zusammenfügen (Platzhalter können über
     * mehrere Runs verteilt sein), ersetzen, Ergebnis in den ersten Run schreiben,
     * die restlichen Runs leeren. Nicht ausgefüllte Platzhalter werden entfernt.
     */
    private void replaceInParagraph(XWPFParagraph p, Map<String, String> values) {
        List<XWPFRun> runs = p.getRuns();
        if (runs == null || runs.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (XWPFRun r : runs) {
            String t = r.getText(0);
            if (t != null) sb.append(t);
        }
        String text = sb.toString();
        if (!text.contains("{{")) return;

        for (Map.Entry<String, String> e : values.entrySet()) {
            text = text.replace(e.getKey(), e.getValue() == null ? "" : e.getValue());
        }
        // Übrige, nicht befüllte Platzhalter entfernen
        text = text.replaceAll("\\{\\{[A-Z0-9_]+\\}\\}", "");

        runs.get(0).setText(text, 0);
        for (int i = 1; i < runs.size(); i++) {
            runs.get(i).setText("", 0);
        }
    }
}

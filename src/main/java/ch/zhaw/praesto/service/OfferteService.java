package ch.zhaw.praesto.service;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Befüllt die Offerten-Word-Vorlage ({@code templates/offerte-vorlage.docx}) mit den
 * im UI erfassten Werten. Die Positionen sind dynamisch: In der Positions-Tabelle
 * dient die erste Datenzeile als Vorlage, die je Position geklont und befüllt wird.
 */
@Service
public class OfferteService {

    private static final String TEMPLATE = "templates/offerte-vorlage.docx";
    private static final String POS_MARK = "{{POSITION_1_BEZEICHNUNG}}";

    /** Eine Position der Offerte. */
    public record Position(String bezeichnung, String menge, String einheit, String preis, String total) {}

    public byte[] fill(Map<String, String> tokens, List<Position> positions) {
        try (InputStream in = new ClassPathResource(TEMPLATE).getInputStream();
             XWPFDocument doc = new XWPFDocument(in)) {

            buildPositions(doc, positions);

            // Übrige (Kopf-/Summen-)Platzhalter im ganzen Dokument ersetzen –
            // inkl. verschachtelter Tabellen (Kopfdaten stehen in Tabellen-in-Tabellen).
            replaceInParagraphs(doc.getParagraphs(), tokens);
            for (XWPFTable table : doc.getTables()) replaceInTable(table, tokens);
            for (XWPFHeader h : doc.getHeaderList()) {
                replaceInParagraphs(h.getParagraphs(), tokens);
                for (XWPFTable table : h.getTables()) replaceInTable(table, tokens);
            }
            for (XWPFFooter f : doc.getFooterList()) {
                replaceInParagraphs(f.getParagraphs(), tokens);
                for (XWPFTable table : f.getTables()) replaceInTable(table, tokens);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            doc.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Offerte konnte nicht erstellt werden: " + e.getMessage(), e);
        }
    }

    /** Positions-Tabelle finden, Vorlagezeile klonen/füllen, überzählige Zeilen entfernen. */
    private void buildPositions(XWPFDocument doc, List<Position> positions) {
        XWPFTable table = null;
        int tplIdx = -1;
        for (XWPFTable t : doc.getTables()) {
            for (int i = 0; i < t.getRows().size(); i++) {
                if (rowText(t.getRow(i)).contains(POS_MARK)) {
                    table = t;
                    tplIdx = i;
                    break;
                }
            }
            if (table != null) break;
        }
        if (table == null) return; // Vorlage ohne Positions-Tabelle -> nichts zu tun

        // Alle weiteren Positions-Vorlagezeilen entfernen (POSITION_2/3), von hinten
        for (int i = table.getRows().size() - 1; i > tplIdx; i--) {
            String txt = rowText(table.getRow(i));
            if (txt.contains("{{POSITION_") || txt.contains("{{TOTAL_")) {
                table.removeRow(i);
            }
        }

        XWPFTableRow tpl = table.getRow(tplIdx);
        CTRow tplCt = (CTRow) tpl.getCtRow().copy();

        if (positions == null || positions.isEmpty()) {
            table.removeRow(tplIdx);
            return;
        }

        for (int i = 0; i < positions.size(); i++) {
            XWPFTableRow row;
            if (i == 0) {
                row = tpl;
            } else {
                // Neue Zeile direkt im Dokument-Baum anlegen und Vorlageninhalt hineinkopieren,
                // damit spätere Änderungen an der Zeile auch wirklich im Dokument landen.
                CTRow newCtRow = table.getCTTbl().addNewTr();
                newCtRow.set(tplCt);
                row = new XWPFTableRow(newCtRow, table);
            }
            fillRow(row, positions.get(i), i + 1);
        }
    }

    /** Eine Positionszeile befüllen: Nr. + Token-Ersetzung in den Zellen. */
    private void fillRow(XWPFTableRow row, Position p, int nr) {
        List<XWPFTableCell> cells = row.getTableCells();
        if (!cells.isEmpty()) {
            setCellText(cells.get(0), String.format("%02d", nr));
        }
        Map<String, String> m = Map.of(
                "{{POSITION_1_BEZEICHNUNG}}", nz(p.bezeichnung()),
                "{{MENGE}}", nz(p.menge()),
                "{{EINHEIT}}", nz(p.einheit()),
                "{{PREIS}}", nz(p.preis()),
                "{{TOTAL_1}}", nz(p.total()));
        for (XWPFTableCell c : cells) {
            replaceInParagraphs(c.getParagraphs(), m);
        }
    }

    /** Text der ersten Zelle setzen (Formatierung des ersten Runs bleibt erhalten). */
    private void setCellText(XWPFTableCell cell, String text) {
        List<XWPFParagraph> paras = cell.getParagraphs();
        if (paras.isEmpty()) {
            cell.setText(text);
            return;
        }
        XWPFParagraph p = paras.get(0);
        List<XWPFRun> runs = p.getRuns();
        if (runs == null || runs.isEmpty()) {
            p.createRun().setText(text);
        } else {
            runs.get(0).setText(text, 0);
            for (int i = 1; i < runs.size(); i++) runs.get(i).setText("", 0);
        }
    }

    private String rowText(XWPFTableRow row) {
        StringBuilder sb = new StringBuilder();
        for (XWPFTableCell c : row.getTableCells()) sb.append(c.getText());
        return sb.toString();
    }

    /** Rekursiv: Absätze der Zellen ersetzen und in verschachtelte Tabellen absteigen. */
    private void replaceInTable(XWPFTable table, Map<String, String> values) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                replaceInParagraphs(cell.getParagraphs(), values);
                for (XWPFTable nested : cell.getTables()) {
                    replaceInTable(nested, values);
                }
            }
        }
    }

    private void replaceInParagraphs(List<XWPFParagraph> paras, Map<String, String> values) {
        if (paras == null) return;
        for (XWPFParagraph p : paras) replaceInParagraph(p, values);
    }

    private void replaceInParagraph(XWPFParagraph p, Map<String, String> values) {
        // Vollständigen Absatztext holen (fasst alle Runs UND Text-Segmente zusammen,
        // egal wie Word den Platzhalter aufgeteilt hat).
        String full = p.getText();
        if (full == null || !full.contains("{{")) return;

        String replaced = full;
        for (Map.Entry<String, String> e : values.entrySet()) {
            replaced = replaced.replace(e.getKey(), e.getValue() == null ? "" : e.getValue());
        }
        // Nicht befüllte Platzhalter entfernen
        replaced = replaced.replaceAll("\\{\\{[A-Z0-9_]+\\}\\}", "");

        // Ersten Run behalten (Formatierung), alle weiteren entfernen, ersten sauber neu setzen.
        for (int i = p.getRuns().size() - 1; i >= 1; i--) {
            p.removeRun(i);
        }
        XWPFRun r0 = p.getRuns().isEmpty() ? p.createRun() : p.getRuns().get(0);
        while (r0.getCTR().sizeOfTArray() > 0) {
            r0.getCTR().removeT(0);
        }
        r0.setText(replaced, 0);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}

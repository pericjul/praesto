package ch.zhaw.praesto.service;

import ch.zhaw.praesto.service.storage.LocalFileStorageService;
import ch.zhaw.praesto.service.storage.StorageService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class DocxLetterTest {

    @Test
    void writesFormattedLetterWithBoldSubject() throws Exception {
        StorageService storage = new LocalFileStorageService(Paths.get("target/test-letters").toString());
        DocxService docx = new DocxService(storage);

        String stored = docx.writeLetter("Bewerbung_Test",
                "Julia Peric", "Musterstrasse 1", "8000 Zürich", "079 123 45 67", "j@example.com",
                "Social Leaders GmbH", "Frau Muster", "Neumühlequai 38", "8006 Zürich",
                "Schlieren, 01.07.2026",
                "Bewerbung als Junior Project Managerin 80%",
                "Sehr geehrte:r Frau Muster",
                "Erster Absatz mit Bezug zur Stelle.\n\nZweiter Absatz mit meiner Stärke.",
                "Julia Peric");

        byte[] bytes = storage.readAllBytes(stored);
        assertThat(bytes).as("Dokument wurde abgelegt").isNotNull();

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            boolean subjectBold = false;
            StringBuilder all = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                all.append(p.getText()).append("\n");
                for (XWPFRun r : p.getRuns()) {
                    if ("Bewerbung als Junior Project Managerin 80%".equals(r.text()) && r.isBold()) {
                        subjectBold = true;
                    }
                }
            }
            String text = all.toString();
            assertThat(text).contains("Julia Peric");
            assertThat(text).contains("Social Leaders GmbH");
            assertThat(text).contains("Bewerbung als Junior Project Managerin 80%");
            assertThat(text).contains("Sehr geehrte:r Frau Muster");
            assertThat(text).contains("Freundliche Grüsse");
            assertThat(subjectBold).as("Betreff ist fett").isTrue();
        }
    }
}

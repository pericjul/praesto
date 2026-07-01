package ch.zhaw.praesto.service;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OfferteServiceTest {

    @Test
    void fillsTemplateWithDynamicPositions() throws Exception {
        OfferteService service = new OfferteService();

        Map<String, String> tokens = Map.of(
                "{{SCHULE_NAME}}", "Sekundarschule Test",
                "{{ORT}}", "Zürich",
                "{{SUBTOTAL}}", "2'000.00",
                "{{MWST}}", "162.00",
                "{{GESAMTTOTAL}}", "2'162.00");

        List<OfferteService.Position> positions = List.of(
                new OfferteService.Position("Praesto-Lizenz", "1", "Schuljahr", "1'200.00", "1'200.00"),
                new OfferteService.Position("Zusatzmodul", "1", "Schuljahr", "800.00", "800.00"));

        byte[] out = service.fill(tokens, positions);
        assertThat(out).isNotEmpty();

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(out));
             XWPFWordExtractor ex = new XWPFWordExtractor(doc)) {
            String text = ex.getText();
            // Keine offenen Platzhalter mehr
            assertThat(text).doesNotContain("{{");
            // Beide Positionen vorhanden
            assertThat(text).contains("Praesto-Lizenz");
            assertThat(text).contains("Zusatzmodul");
            // Kopf-/Summenwerte vorhanden
            assertThat(text).contains("Sekundarschule Test");
            assertThat(text).contains("2'162.00");
        }
    }
}

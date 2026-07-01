package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.service.OfferteService;
import ch.zhaw.praesto.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Erstellt aus der Word-Vorlage eine fertige Offerte (.docx) mit den im UI erfassten
 * Werten. Nur für den Super-Admin. Beträge/Totale werden serverseitig berechnet.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OfferteController {

    private static final Locale CH = Locale.forLanguageTag("de-CH");

    private final OfferteService offerteService;
    private final UserService userService;

    @PostMapping("/super/offerte")
    public ResponseEntity<byte[]> createOfferte(@RequestBody Map<String, String> b) {
        User current = userService.getCurrentUser();
        if (current.getRole() != UserRole.SUPER_ADMIN) {
            throw new ForbiddenException("Nur der Super-Admin kann Offerten erstellen.");
        }

        double total1 = parse(b.get("total1"));
        double total2 = parse(b.get("total2"));
        double total3 = parse(b.get("total3"));
        double subtotal = total1 + total2 + total3;
        double mwst = round(subtotal * 0.081);
        double gesamt = subtotal + mwst;

        Map<String, String> v = new LinkedHashMap<>();
        v.put("{{SCHULE_NAME}}", str(b.get("schuleName")));
        v.put("{{ANSPRECHPERSON}}", str(b.get("ansprechperson")));
        v.put("{{STRASSE}}", str(b.get("strasse")));
        v.put("{{PLZ}}", str(b.get("plz")));
        v.put("{{ORT}}", str(b.get("ort")));
        v.put("{{OFFERTEN_NR}}", str(b.get("offertenNr")));
        v.put("{{DATUM}}", str(b.get("datum")));
        v.put("{{GUELTIG_BIS}}", str(b.get("gueltigBis")));
        v.put("{{SCHULJAHR}}", str(b.get("schuljahr")));
        v.put("{{LAUFZEIT}}", str(b.get("laufzeit")));
        v.put("{{MENGE}}", str(b.get("menge")));
        v.put("{{EINHEIT}}", str(b.get("einheit")));
        v.put("{{PREIS}}", money(parse(b.get("preis"))));
        v.put("{{POSITION_1_BEZEICHNUNG}}", str(b.get("position1")));
        v.put("{{POSITION_2_BEZEICHNUNG}}", str(b.get("position2")));
        v.put("{{POSITION_3_BEZEICHNUNG}}", str(b.get("position3")));
        v.put("{{TOTAL_1}}", total1 > 0 ? money(total1) : "");
        v.put("{{TOTAL_2}}", total2 > 0 ? money(total2) : "");
        v.put("{{TOTAL_3}}", total3 > 0 ? money(total3) : "");
        v.put("{{SUBTOTAL}}", money(subtotal));
        v.put("{{MWST}}", money(mwst));
        v.put("{{GESAMTTOTAL}}", money(gesamt));

        byte[] docx = offerteService.fill(v);

        String nr = str(b.get("offertenNr")).replaceAll("[^a-zA-Z0-9._-]", "_");
        String filename = "Offerte_Praesto" + (nr.isBlank() ? "" : "_" + nr) + ".docx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(docx);
    }

    private static String str(String s) {
        return s == null ? "" : s.trim();
    }

    private static double parse(String s) {
        if (s == null || s.isBlank()) return 0;
        String cleaned = s.replaceAll("[^0-9,\\.]", "").replace(",", ".");
        // Falls mehrere Punkte (Tausender), nur den letzten als Dezimalpunkt lassen
        int last = cleaned.lastIndexOf('.');
        if (last >= 0) {
            cleaned = cleaned.substring(0, last).replace(".", "") + cleaned.substring(last);
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double round(double d) {
        return Math.round(d * 100.0) / 100.0;
    }

    private static String money(double d) {
        NumberFormat nf = NumberFormat.getNumberInstance(CH);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(d);
    }
}

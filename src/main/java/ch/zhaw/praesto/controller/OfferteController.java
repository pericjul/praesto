package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.model.OfferteRequest;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.service.OfferteService;
import ch.zhaw.praesto.service.OfferteService.Position;
import ch.zhaw.praesto.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Erstellt aus der Word-Vorlage eine fertige Offerte (.docx) mit den im UI erfassten
 * Werten. Nur für den Super-Admin. Positionen sind dynamisch; Totale/MwSt. werden
 * serverseitig berechnet.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OfferteController {

    private static final Locale CH = Locale.forLanguageTag("de-CH");

    private final OfferteService offerteService;
    private final UserService userService;

    @PostMapping("/super/offerte")
    public ResponseEntity<byte[]> createOfferte(@RequestBody OfferteRequest req) {
        User current = userService.getCurrentUser();
        if (current.getRole() != UserRole.SUPER_ADMIN) {
            throw new ForbiddenException("Nur der Super-Admin kann Offerten erstellen.");
        }

        List<Position> positions = new ArrayList<>();
        double subtotal = 0;
        if (req.positions() != null) {
            for (OfferteRequest.Pos p : req.positions()) {
                if (p == null) continue;
                String bez = str(p.bezeichnung());
                if (bez.isBlank()) continue; // leere Positionen überspringen
                double menge = parse(p.menge());
                if (menge == 0) menge = 1;
                double preis = parse(p.preis());
                double total = round(menge * preis);
                subtotal += total;
                positions.add(new Position(
                        bez,
                        trimNum(menge),
                        str(p.einheit()),
                        money(preis),
                        money(total)));
            }
        }
        double mwst = round(subtotal * 0.081);
        double gesamt = subtotal + mwst;

        Map<String, String> tokens = new LinkedHashMap<>();
        tokens.put("{{SCHULE_NAME}}", str(req.schuleName()));
        tokens.put("{{ANSPRECHPERSON}}", str(req.ansprechperson()));
        tokens.put("{{STRASSE}}", str(req.strasse()));
        tokens.put("{{PLZ}}", str(req.plz()));
        tokens.put("{{ORT}}", str(req.ort()));
        tokens.put("{{OFFERTEN_NR}}", str(req.offertenNr()));
        tokens.put("{{DATUM}}", str(req.datum()));
        tokens.put("{{GUELTIG_BIS}}", str(req.gueltigBis()));
        tokens.put("{{SCHULJAHR}}", str(req.schuljahr()));
        tokens.put("{{LAUFZEIT}}", str(req.laufzeit()));
        tokens.put("{{SUBTOTAL}}", money(subtotal));
        tokens.put("{{MWST}}", money(mwst));
        tokens.put("{{GESAMTTOTAL}}", money(gesamt));

        byte[] docx = offerteService.fill(tokens, positions);

        String nr = str(req.offertenNr()).replaceAll("[^a-zA-Z0-9._-]", "_");
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

    private static String trimNum(double d) {
        if (d == Math.rint(d)) return String.valueOf((long) d);
        return money(d);
    }

    private static double parse(String s) {
        if (s == null || s.isBlank()) return 0;
        String c = s.replaceAll("[^0-9,\\.]", "").replace(",", ".");
        int last = c.lastIndexOf('.');
        if (last >= 0) c = c.substring(0, last).replace(".", "") + c.substring(last);
        try {
            return Double.parseDouble(c);
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

package ch.zhaw.praesto.model;

import java.util.List;

/** Eingaben des Offerten-Formulars (Super-Admin). Positionen sind dynamisch. */
public record OfferteRequest(
        String schuleName,
        String ansprechperson,
        String strasse,
        String plz,
        String ort,
        String offertenNr,
        String datum,
        String gueltigBis,
        String schuljahr,
        String laufzeit,
        List<Pos> positions) {

    /** Eine Position: Bezeichnung, Menge, Einheit, Preis pro Einheit. */
    public record Pos(String bezeichnung, String menge, String einheit, String preis) {}
}

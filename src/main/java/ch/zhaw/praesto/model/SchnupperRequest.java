package ch.zhaw.praesto.model;

import lombok.Data;

/** Eingaben für den Schnupper-Anfrage-Helfer. */
@Data
public class SchnupperRequest {
    private String beruf;          // Pflicht: für welchen Beruf
    private String firma;          // optional
    private String kontaktperson;  // optional
    private String deinName;       // optional (sonst aus dem Konto)
    private String klasse;         // optional (z.B. "3. Sek B")
    private String zeitraum;       // optional (z.B. "in den Frühlingsferien")
}

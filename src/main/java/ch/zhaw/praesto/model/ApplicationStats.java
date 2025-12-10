package ch.zhaw.praesto.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicationStats {
    private long total;             // Alle Bewerbungen
    private long planned;           // Geplant
    private long applied;           // Beworben
    private long invited;           // Eingeladen
    private long interviewDone;     // Gespräch absolviert
    private long accepted;          // Zusagen
    private long rejected;          // Absagen
    private long withdrawn;         // Zurückgezogen
    private long active;            // Alle ausser REJECTED, WITHDRAWN, ACCEPTED
}
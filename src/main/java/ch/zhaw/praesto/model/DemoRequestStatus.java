package ch.zhaw.praesto.model;

/**
 * Status einer Demo-Terminanfrage einer interessierten Schule.
 */
public enum DemoRequestStatus {
    NEW,        // neu eingegangen, noch nicht bearbeitet
    APPROVED,   // Termin freigegeben, zeitlich begrenzter Zugang erstellt
    DONE,       // Demo-Tag vorbei / Zugang abgelaufen
    REJECTED    // abgelehnt
}

package ch.zhaw.praesto.model;

/**
 * KI-Funktionen mit festem Nutzungs-Kontingent pro Schüler:in (lebenslang, kein
 * Nachladen). Lehrer-Aufgaben laufen NICHT über dieses Kontingent.
 */
public enum AiFeature {
    PRACTICE_INTERVIEW,   // selbst gestartetes Übungsgespräch (max. 3)
    CV,                   // Lebenslauf generieren (max. 1)
    COVER_LETTER          // Bewerbungsschreiben generieren (max. 3)
}

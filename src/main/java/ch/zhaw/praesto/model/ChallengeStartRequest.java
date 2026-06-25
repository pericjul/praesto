package ch.zhaw.praesto.model;

/**
 * Optionale Parameter beim Starten einer Klassen-Challenge (1-Klick: beide leer).
 */
public record ChallengeStartRequest(Integer target, String title) {
}

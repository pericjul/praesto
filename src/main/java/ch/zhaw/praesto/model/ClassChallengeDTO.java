package ch.zhaw.praesto.model;

/**
 * Klassen-Challenge mit aktuellem Fortschritt.
 */
public record ClassChallengeDTO(
        String id,
        String title,
        int target,
        int current,
        int percent,
        boolean active) {
}

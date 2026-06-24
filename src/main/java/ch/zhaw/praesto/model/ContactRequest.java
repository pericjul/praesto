package ch.zhaw.praesto.model;

public record ContactRequest(
        String name,
        String email,
        String organisation,
        String role,
        String interest,
        Integer classes,
        Integer students,
        boolean wantsMeeting,
        String message) {
}

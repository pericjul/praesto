package ch.zhaw.praesto.model;

public record RegisterRequest(String firstName, String lastName, String email, String password) {
}

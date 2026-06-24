package ch.zhaw.praesto.model;

public record AuthResponse(String token, UserDTO user) {
}

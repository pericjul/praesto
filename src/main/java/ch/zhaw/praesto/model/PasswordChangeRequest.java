package ch.zhaw.praesto.model;

public record PasswordChangeRequest(String currentPassword, String newPassword) {
}

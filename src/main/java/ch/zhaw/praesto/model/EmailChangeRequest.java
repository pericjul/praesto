package ch.zhaw.praesto.model;

/** Anfrage zum Ändern der eigenen Login-E-Mail (mit Passwort-Bestätigung). */
public record EmailChangeRequest(String newEmail, String currentPassword) {
}

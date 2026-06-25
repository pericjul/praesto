package ch.zhaw.praesto.model;

/**
 * Anlegen eines Dossier-Eintrags nach erfolgtem Datei-Upload.
 */
public record DocumentCreateRequest(
        String category,
        String title,
        String fileUrl,
        String fileName) {
}

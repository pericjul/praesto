package ch.zhaw.praesto.service.storage;

import org.springframework.core.io.Resource;

/**
 * Abstraktion für die Dateiablage (Bewerbungsunterlagen, Bilder, generierte Dokumente).
 * Der übrige Code kennt nur einen {@code storedName} (Schlüssel) – NICHT den Speicherort.
 * So lässt sich zwischen lokalem Dateisystem ({@link LocalFileStorageService}) und
 * Azure Blob Storage ({@link AzureBlobStorageService}) per Konfiguration umschalten,
 * ohne Aufrufer zu ändern. Der {@code storedName} entspricht dem in der DB gespeicherten
 * {@code fileUrl} – eine Migration ändert also die Datenbank nicht.
 */
public interface StorageService {

    /** Legt den Inhalt unter {@code storedName} ab (überschreibt bei Bedarf). */
    void store(String storedName, byte[] content);

    /** Existiert eine Datei mit diesem Namen? */
    boolean exists(String storedName);

    /** Liefert die Datei als {@link Resource} für den Download. Null, wenn nicht vorhanden. */
    Resource load(String storedName);

    /** Rohe Bytes (z.B. um ein Foto in den Lebenslauf einzubetten). Null, wenn nicht vorhanden. */
    byte[] readAllBytes(String storedName);

    /** Content-Type (MIME), sofern ermittelbar – sonst null. */
    String probeContentType(String storedName);

    /** Löscht die Datei, falls vorhanden (kein Fehler, wenn nicht da). */
    void delete(String storedName);
}

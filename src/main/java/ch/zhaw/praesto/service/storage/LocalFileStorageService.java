package ch.zhaw.praesto.service.storage;

import ch.zhaw.praesto.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Speichert Dateien im lokalen Dateisystem (Upload-Ordner). Standard-Implementierung,
 * aktiv solange {@code praesto.storage.type} nicht auf {@code azure} steht.
 *
 * In Azure App Service muss der Ordner unter {@code /home} liegen (persistent + instanz-
 * übergreifend) und {@code WEBSITES_ENABLE_APP_SERVICE_STORAGE=true} gesetzt sein, sonst
 * gehen Dateien bei Neustart/Skalierung verloren.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "praesto.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements StorageService {

    private final Path uploadDir;

    public LocalFileStorageService(@Value("${praesto.uploads.dir:uploads}") String uploadsDir) {
        this.uploadDir = Paths.get(uploadsDir).toAbsolutePath().normalize();
        boolean persistent = this.uploadDir.startsWith("/home");
        log.info("Storage: LOKALES Dateisystem, Verzeichnis {} ({})", this.uploadDir,
                persistent ? "persistent /home" : "ACHTUNG: nicht unter /home – evtl. flüchtig");
    }

    private Path resolveSafe(String storedName) {
        if (storedName == null || storedName.contains("..")
                || storedName.contains("/") || storedName.contains("\\")) {
            throw new BadRequestException("Ungültiger Dateiname");
        }
        Path target = uploadDir.resolve(storedName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new BadRequestException("Ungültiger Dateiname");
        }
        return target;
    }

    @Override
    public void store(String storedName, byte[] content) {
        try {
            Files.createDirectories(uploadDir);
            Files.write(resolveSafe(storedName), content);
        } catch (IOException e) {
            throw new RuntimeException("Datei konnte nicht gespeichert werden", e);
        }
    }

    @Override
    public boolean exists(String storedName) {
        return Files.exists(resolveSafe(storedName));
    }

    @Override
    public Resource load(String storedName) {
        try {
            Path file = resolveSafe(storedName);
            if (!Files.exists(file)) {
                return null;
            }
            return new UrlResource(file.toUri());
        } catch (IOException e) {
            throw new RuntimeException("Datei konnte nicht geladen werden", e);
        }
    }

    @Override
    public byte[] readAllBytes(String storedName) {
        try {
            Path file = resolveSafe(storedName);
            if (!Files.exists(file)) {
                return null;
            }
            return Files.readAllBytes(file);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String probeContentType(String storedName) {
        try {
            return Files.probeContentType(resolveSafe(storedName));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void delete(String storedName) {
        try {
            Files.deleteIfExists(resolveSafe(storedName));
        } catch (IOException e) {
            log.warn("Datei {} konnte nicht gelöscht werden: {}", storedName, e.getMessage());
        }
    }
}

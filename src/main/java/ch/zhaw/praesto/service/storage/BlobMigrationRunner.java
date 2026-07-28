package ch.zhaw.praesto.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * EINMALIGE Migration: kopiert alle Dateien aus dem lokalen Upload-Ordner in den aktiven
 * {@link StorageService} (Azure Blob). Läuft NUR, wenn {@code praesto.storage.migrate-local-to-blob=true}
 * UND {@code praesto.storage.type=azure} – idempotent (bereits vorhandene Blobs werden übersprungen),
 * bricht den App-Start bei Fehlern nicht ab.
 *
 * Ablauf: Flag setzen (Env MIGRATE_LOCAL_TO_BLOB=true) → deployen → Log prüfen ("X Dateien migriert")
 * → Flag wieder entfernen.
 */
@Component
@ConditionalOnProperty(name = "praesto.storage.migrate-local-to-blob", havingValue = "true")
@Order(5)
@RequiredArgsConstructor
@Slf4j
public class BlobMigrationRunner implements CommandLineRunner {

    private final StorageService storageService;

    @Value("${praesto.storage.type:local}")
    private String storageType;

    @Value("${praesto.uploads.dir:uploads}")
    private String uploadsDir;

    @Override
    public void run(String... args) {
        if (!"azure".equalsIgnoreCase(storageType)) {
            log.warn("Blob-Migration übersprungen: praesto.storage.type ist nicht 'azure'.");
            return;
        }
        Path dir = Paths.get(uploadsDir).toAbsolutePath().normalize();
        if (!Files.isDirectory(dir)) {
            log.info("Blob-Migration: kein lokaler Upload-Ordner ({}) – nichts zu migrieren.", dir);
            return;
        }

        int migrated = 0, skipped = 0, failed = 0;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                String name = file.getFileName().toString();
                try {
                    if (storageService.exists(name)) {
                        skipped++;
                        continue;
                    }
                    storageService.store(name, Files.readAllBytes(file));
                    migrated++;
                } catch (Exception e) {
                    failed++;
                    log.warn("Blob-Migration: {} fehlgeschlagen: {}", name, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Blob-Migration konnte nicht ausgeführt werden: {}", e.getMessage());
            return;
        }
        log.info("Blob-Migration fertig: {} migriert, {} bereits vorhanden, {} fehlgeschlagen. "
                + "Bitte MIGRATE_LOCAL_TO_BLOB jetzt wieder entfernen.", migrated, skipped, failed);
    }
}

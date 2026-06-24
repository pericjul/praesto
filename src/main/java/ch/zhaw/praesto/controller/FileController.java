package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * Datei-Upload und -Download für Abgaben (DOCUMENT_UPLOAD, VIDEO_PITCH).
 * Dateien liegen im konfigurierten Upload-Ordner. Beide Endpoints sind über die
 * SecurityConfig nur für authentifizierte Nutzer erreichbar; der Download läuft
 * im Frontend über einen SvelteKit-Proxy, der den JWT-Bearer-Header mitschickt.
 */
@RestController
@RequestMapping("/api/files")
@Slf4j
public class FileController {

    private final Path uploadDir;

    public FileController(@Value("${praesto.uploads.dir:uploads}") String uploadsDir) {
        this.uploadDir = Paths.get(uploadsDir).toAbsolutePath().normalize();
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Keine Datei ausgewählt");
        }

        String original = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "datei" : file.getOriginalFilename());
        String safe = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedName = UUID.randomUUID() + "_" + safe;

        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(storedName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new BadRequestException("Ungültiger Dateiname");
        }
        file.transferTo(target);

        log.info("Datei hochgeladen: {} ({} Bytes)", storedName, file.getSize());
        return ResponseEntity.ok(Map.of("fileUrl", storedName, "fileName", original));
    }

    @GetMapping("/{name}")
    public ResponseEntity<Resource> download(@PathVariable String name) throws IOException {
        if (name.contains("..") || name.contains("/") || name.contains("\\")) {
            throw new BadRequestException("Ungültiger Dateiname");
        }

        Path file = uploadDir.resolve(name).normalize();
        if (!file.startsWith(uploadDir) || !Files.exists(file)) {
            throw new NotFoundException("Datei nicht gefunden");
        }

        Resource resource = new UrlResource(file.toUri());
        // Original-Dateiname = alles nach dem ersten "_" (UUID-Präfix entfernen)
        String downloadName = name.contains("_") ? name.substring(name.indexOf('_') + 1) : name;
        String contentType = Files.probeContentType(file);

        return ResponseEntity.ok()
                .contentType(contentType != null
                        ? MediaType.parseMediaType(contentType)
                        : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                .body(resource);
    }
}

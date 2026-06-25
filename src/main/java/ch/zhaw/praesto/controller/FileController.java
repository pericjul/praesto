package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.service.FileAccessService;
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
import java.util.Set;
import java.util.UUID;

/**
 * Datei-Upload und -Download für Abgaben (DOCUMENT_UPLOAD, VIDEO_PITCH) und Dossier.
 * Dateien liegen im konfigurierten Upload-Ordner. Beide Endpoints sind über die
 * SecurityConfig nur für authentifizierte Nutzer erreichbar; der Download wird
 * zusätzlich pro Datei autorisiert ({@link FileAccessService}).
 */
@RestController
@RequestMapping("/api/files")
@Slf4j
public class FileController {

    // Erlaubte Dateitypen (Bewerbungsunterlagen, Bilder, Video-Pitch)
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "odt", "rtf", "txt",
            "jpg", "jpeg", "png", "gif", "webp", "heic", "heif",
            "mp4", "webm", "mov", "m4v");

    private final Path uploadDir;
    private final FileAccessService fileAccessService;

    public FileController(@Value("${praesto.uploads.dir:uploads}") String uploadsDir,
                          FileAccessService fileAccessService) {
        this.uploadDir = Paths.get(uploadsDir).toAbsolutePath().normalize();
        this.fileAccessService = fileAccessService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Keine Datei ausgewählt");
        }

        String original = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "datei" : file.getOriginalFilename());

        String extension = extensionOf(original);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException(
                    "Dateityp nicht erlaubt. Erlaubt: PDF, Word, Bilder (JPG/PNG …), Video (MP4 …).");
        }

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

        // Objekt-Level-Autorisierung: nur Besitzer / Schul-Personal / Super-Admin
        fileAccessService.assertCanAccess(name);

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

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return null;
        }
        return filename.substring(dot + 1).toLowerCase();
    }
}

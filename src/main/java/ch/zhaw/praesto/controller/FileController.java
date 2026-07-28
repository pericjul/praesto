package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.service.FileAccessService;
import ch.zhaw.praesto.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Datei-Upload und -Download für Abgaben (DOCUMENT_UPLOAD, VIDEO_PITCH) und Dossier.
 * Die eigentliche Ablage übernimmt der {@link StorageService} (lokales Dateisystem oder
 * Azure Blob – umschaltbar per Konfiguration). Beide Endpoints sind über die SecurityConfig
 * nur für authentifizierte Nutzer erreichbar; der Download wird zusätzlich pro Datei
 * autorisiert ({@link FileAccessService}).
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    // Erlaubte Dateitypen (Bewerbungsunterlagen, Bilder, Video-Pitch)
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "odt", "rtf", "txt",
            "jpg", "jpeg", "png", "gif", "webp", "heic", "heif",
            "mp4", "webm", "mov", "m4v");

    private final StorageService storageService;
    private final FileAccessService fileAccessService;

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

        storageService.store(storedName, file.getBytes());

        log.info("Datei hochgeladen: {} ({} Bytes)", storedName, file.getSize());
        return ResponseEntity.ok(Map.of("fileUrl", storedName, "fileName", original));
    }

    @GetMapping("/{name}")
    public ResponseEntity<Resource> download(@PathVariable String name) {
        if (name.contains("..") || name.contains("/") || name.contains("\\")) {
            throw new BadRequestException("Ungültiger Dateiname");
        }

        // Objekt-Level-Autorisierung: nur Besitzer / Schul-Personal / Super-Admin
        fileAccessService.assertCanAccess(name);

        Resource resource = storageService.exists(name) ? storageService.load(name) : null;
        if (resource == null) {
            throw new NotFoundException("Datei nicht gefunden");
        }

        // Original-Dateiname = alles nach dem ersten "_" (UUID-Präfix entfernen)
        String downloadName = name.contains("_") ? name.substring(name.indexOf('_') + 1) : name;
        String contentType = storageService.probeContentType(name);

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

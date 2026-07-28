package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.DocumentRepository;
import ch.zhaw.praesto.service.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Bewerbungsdossier: Dokumente einer Schüler:in auflisten, ablegen (hochgeladen
 * oder KI-generiert) und löschen. Die Datei-Ablage übernimmt der {@link StorageService}.
 */
@Service
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserService userService;
    private final StorageService storageService;

    public DocumentService(DocumentRepository documentRepository, UserService userService,
                           StorageService storageService) {
        this.documentRepository = documentRepository;
        this.userService = userService;
        this.storageService = storageService;
    }

    public List<DocumentDTO> myDocuments() {
        requireStudent();
        return documentRepository.findByStudentIdOrderByCreatedAtDesc(userService.getUserId()).stream()
                .map(DocumentDTO::from)
                .toList();
    }

    public DocumentDTO create(DocumentCreateRequest req) {
        requireStudent();
        if (req == null || req.fileUrl() == null || req.fileUrl().isBlank()) {
            throw new ForbiddenException("Keine Datei angegeben");
        }
        Document doc = save(parseCategory(req.category()),
                req.title() != null && !req.title().isBlank() ? req.title().trim() : req.fileName(),
                req.fileUrl(), req.fileName(), false);
        return DocumentDTO.from(doc);
    }

    /**
     * Von einem Generator (Lebenslauf/Brief) erzeugtes Dokument im Dossier ablegen.
     */
    public Document saveGenerated(String studentId, String schoolId, DocumentCategory category,
                                  String title, String fileUrl, String fileName) {
        return documentRepository.save(Document.builder()
                .studentId(studentId)
                .schoolId(schoolId)
                .category(category)
                .title(title)
                .fileUrl(fileUrl)
                .fileName(fileName)
                .generated(true)
                .createdAt(Instant.now())
                .build());
    }

    public void delete(String id) {
        requireStudent();
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dokument nicht gefunden"));
        if (!userService.getUserId().equals(doc.getStudentId())) {
            throw new ForbiddenException("Keine Berechtigung");
        }
        if (doc.getFileUrl() != null && !doc.getFileUrl().isBlank()) {
            storageService.delete(doc.getFileUrl());
        }
        documentRepository.delete(doc);
    }

    // ============================================================
    // Intern
    // ============================================================

    private Document save(DocumentCategory category, String title, String fileUrl, String fileName, boolean generated) {
        return documentRepository.save(Document.builder()
                .studentId(userService.getUserId())
                .schoolId(userService.getCurrentSchoolId())
                .category(category)
                .title(title)
                .fileUrl(fileUrl)
                .fileName(fileName)
                .generated(generated)
                .createdAt(Instant.now())
                .build());
    }

    private DocumentCategory parseCategory(String value) {
        try {
            return value != null ? DocumentCategory.valueOf(value) : DocumentCategory.SONSTIGES;
        } catch (IllegalArgumentException e) {
            return DocumentCategory.SONSTIGES;
        }
    }

    private void requireStudent() {
        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schüler:innen haben ein Dossier");
        }
    }
}

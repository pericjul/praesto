package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.DocumentCreateRequest;
import ch.zhaw.praesto.model.DocumentDTO;
import ch.zhaw.praesto.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Bewerbungsdossier der Schüler:in: Dokumente auflisten, ablegen, löschen.
 */
@RestController
@RequestMapping("/api/student/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public List<DocumentDTO> myDocuments() {
        return documentService.myDocuments();
    }

    @PostMapping
    public DocumentDTO create(@RequestBody DocumentCreateRequest request) {
        return documentService.create(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

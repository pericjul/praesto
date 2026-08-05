package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.KnowledgeSource;
import ch.zhaw.praesto.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Globale KI-Wissensbasis (nur Super-Admin): Links, Dokumente und Freitext, die der
 * KI-Coach als zusätzlichen Kontext nutzt.
 */
@RestController
@RequestMapping("/api/super/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping("")
    public List<KnowledgeSource> list() {
        return knowledgeService.list();
    }

    @PostMapping("/link")
    public ResponseEntity<KnowledgeSource> addLink(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(knowledgeService.addLink(body.get("url"), body.get("title")));
    }

    @PostMapping("/text")
    public ResponseEntity<KnowledgeSource> addText(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(knowledgeService.addText(body.get("title"), body.get("text")));
    }

    @PostMapping("/document")
    public ResponseEntity<KnowledgeSource> addDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) throws IOException {
        return ResponseEntity.ok(knowledgeService.addDocument(
                file.getBytes(), file.getOriginalFilename(), title));
    }

    @PutMapping("/{id}/enabled")
    public ResponseEntity<Void> setEnabled(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        knowledgeService.setEnabled(id, Boolean.TRUE.equals(body.get("enabled")));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        knowledgeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.SchoolClass;
import ch.zhaw.praesto.model.SchoolClassDTO;
import ch.zhaw.praesto.service.SchoolClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    /**
     * POST /api/classes - Neue Klasse erstellen (nur Lehrer)
     */
    @PostMapping("")
    public ResponseEntity<SchoolClass> createClass(@RequestBody SchoolClassDTO dto) {
        SchoolClass schoolClass = schoolClassService.createClass(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(schoolClass);
    }

    /**
     * GET /api/classes - Alle eigenen Klassen (Lehrer)
     */
    @GetMapping("")
    public ResponseEntity<List<SchoolClass>> getMyClasses() {
        List<SchoolClass> classes = schoolClassService.getMyClasses();
        return ResponseEntity.ok(classes);
    }

    /**
     * GET /api/classes/all - Alle Klassen (für Admin/Übersicht)
     */
    @GetMapping("/all")
    public ResponseEntity<List<SchoolClass>> getAllClasses() {
        List<SchoolClass> classes = schoolClassService.getAllClasses();
        return ResponseEntity.ok(classes);
    }

    /**
     * GET /api/classes/my - Klasse des aktuellen Schülers
     */
    @GetMapping("/my")
    public ResponseEntity<SchoolClass> getMyClass() {
        SchoolClass schoolClass = schoolClassService.getMyClass();
        return ResponseEntity.ok(schoolClass);
    }

    /**
     * GET /api/classes/{id} - Eine Klasse nach ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SchoolClass> getClassById(@PathVariable String id) {
        SchoolClass schoolClass = schoolClassService.getClassById(id);
        return ResponseEntity.ok(schoolClass);
    }

    /**
     * PUT /api/classes/{id} - Klasse aktualisieren
     */
    @PutMapping("/{id}")
    public ResponseEntity<SchoolClass> updateClass(
            @PathVariable String id,
            @RequestBody SchoolClassDTO dto) {
        SchoolClass schoolClass = schoolClassService.updateClass(id, dto);
        return ResponseEntity.ok(schoolClass);
    }

    /**
     * POST /api/classes/{id}/students - Schüler zur Klasse hinzufügen
     */
    @PostMapping("/{id}/students")
    public ResponseEntity<SchoolClass> addStudent(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String studentId = body.get("studentId");
        SchoolClass schoolClass = schoolClassService.addStudentToClass(id, studentId);
        return ResponseEntity.ok(schoolClass);
    }

    /**
     * DELETE /api/classes/{id}/students/{studentId} - Schüler aus Klasse entfernen
     */
    @DeleteMapping("/{id}/students/{studentId}")
    public ResponseEntity<SchoolClass> removeStudent(
            @PathVariable String id,
            @PathVariable String studentId) {
        SchoolClass schoolClass = schoolClassService.removeStudentFromClass(id, studentId);
        return ResponseEntity.ok(schoolClass);
    }

    /**
     * DELETE /api/classes/{id} - Klasse löschen
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable String id) {
        schoolClassService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}
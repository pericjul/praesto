package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.UpdateAssignmentStatusRequest;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.Assignment;
import ch.zhaw.praesto.model.AssignmentCreateDTO;
import ch.zhaw.praesto.model.AssignmentStatus;
import ch.zhaw.praesto.service.AssignmentService;
import ch.zhaw.praesto.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AssignmentController {

    private final AssignmentService assignmentService;

    private final AssignmentRepository assignmentRepository;
    private final UserService userService;

    public AssignmentController(AssignmentRepository assignmentRepository, UserService userService,
            AssignmentService assignmentService) {
        this.assignmentRepository = assignmentRepository;
        this.userService = userService;
        this.assignmentService = assignmentService;
    }

    @PostMapping("/assignment")
    public ResponseEntity<Assignment> createAssignment(@RequestBody AssignmentCreateDTO dto) {

        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer dürfen Assignments erstellen");
        }

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Titel darf nicht leer sein");
        }
        if (dto.getClassId() == null) {
            throw new BadRequestException("classId fehlt");
        }
        if (dto.getType() == null) {
            throw new BadRequestException("Aufgabentyp fehlt");
        }

        Assignment assignment = Assignment.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .durationMin(dto.getDurationMin())
                .dueDate(Instant.parse(dto.getDueDate()))
                .classId(dto.getClassId())
                .type(dto.getType())
                .status(AssignmentStatus.ASSIGNED)
                .createdByTeacherId(userService.getUserId())
                .createdAt(Instant.now())
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // PUT Status nur Teacher
    @PutMapping("/{id}/status")
    public ResponseEntity<Assignment> updateStatus(
            @PathVariable String id,
            @RequestBody UpdateAssignmentStatusRequest req) {
        // Rollen Check
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen den Status ändern");
        }

        // Request validieren
        if (req.getStatus() == null) {
            throw new BadRequestException("Status fehlt");
        }

        // Assignment existiert?
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment nicht gefunden"));

        // Status updaten ueber Service
        assignment.setStatus(req.getStatus());
        Assignment updated = assignmentRepository.save(assignment);

        return ResponseEntity.ok(updated);
    }

    // GET Alle Assignments des aktuellen Lehrers (MUSS VOR /{classId} kommen!)
    @GetMapping("/assignments/teacher")
    public ResponseEntity<List<Assignment>> getMyAssignments() {
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen ihre Assignments sehen");
        }

        String teacherId = userService.getUserId();
        List<Assignment> assignments = assignmentRepository.findByCreatedByTeacherIdOrderByCreatedAtDesc(teacherId);
        return ResponseEntity.ok(assignments);
    }

    // GET Assignments fuer eine Klasse Teacher und Student duerfen lesen
    @GetMapping("/assignments/class/{classId}")
    public ResponseEntity<List<Assignment>> getAssignmentsForClass(
            @PathVariable String classId) {
        // Rollen Check: nur Teacher oder Student duerfen lesen
        if (!userService.userHasRole("TEACHER") && !userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Lehrer oder Schueler duerfen Assignments ansehen");
        }

        List<Assignment> assignments = assignmentRepository.findByClassId(classId);
        return ResponseEntity.ok(assignments);
    }

    // GET Eine einzelne Assignment (Teacher nur eigene)
    @GetMapping("/assignments/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable String id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment nicht gefunden"));

        // Teacher darf nur eigene sehen
        if (userService.userHasRole("TEACHER")) {
            if (!assignment.getCreatedByTeacherId().equals(userService.getUserId())) {
                throw new ForbiddenException("Keine Berechtigung fuer dieses Assignment");
            }
        }

        return ResponseEntity.ok(assignment);
    }

    // DELETE Assignment (nur Teacher)
    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable String id) {
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen Assignments loeschen");
        }

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment nicht gefunden"));

        // Nur eigene Assignments löschen
        if (!assignment.getCreatedByTeacherId().equals(userService.getUserId())) {
            throw new ForbiddenException("Keine Berechtigung fuer dieses Assignment");
        }

        assignmentRepository.delete(assignment);
        return ResponseEntity.noContent().build();
    }

    // PUT Assignment bearbeiten (nur Teacher)
    @PutMapping("/assignments/{id}")
    public ResponseEntity<Assignment> updateAssignment(
            @PathVariable String id,
            @RequestBody AssignmentCreateDTO dto) {
        
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen Assignments bearbeiten");
        }

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment nicht gefunden"));

        // Nur eigene Assignments bearbeiten
        if (!assignment.getCreatedByTeacherId().equals(userService.getUserId())) {
            throw new ForbiddenException("Keine Berechtigung fuer dieses Assignment");
        }

        // Validierung
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Titel darf nicht leer sein");
        }

        // Update
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setType(dto.getType());
        assignment.setClassId(dto.getClassId());
        assignment.setDurationMin(dto.getDurationMin());
        if (dto.getDueDate() != null) {
            assignment.setDueDate(Instant.parse(dto.getDueDate()));
        }

        Assignment updated = assignmentRepository.save(assignment);
        return ResponseEntity.ok(updated);
    }
}
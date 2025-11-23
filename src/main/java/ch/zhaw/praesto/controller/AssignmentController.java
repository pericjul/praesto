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

        Assignment assignment = Assignment.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .durationMin(dto.getDurationMin())
                .dueDate(Instant.parse(dto.getDueDate()))
                .classId(dto.getClassId())
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

    // GET Assignments fuer eine Klasse Teacher und Student duerfen lesen
    @GetMapping("/assignments/{classId}")
    public ResponseEntity<List<Assignment>> getAssignmentsForClass(
            @PathVariable String classId) {
        // Rollen Check: nur Teacher oder Student duerfen lesen
        if (!userService.userHasRole("TEACHER") && !userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Lehrer oder Schueler duerfen Assignments ansehen");
        }

        List<Assignment> assignments = assignmentRepository.findByClassId(classId);
        return ResponseEntity.ok(assignments);
    }
}
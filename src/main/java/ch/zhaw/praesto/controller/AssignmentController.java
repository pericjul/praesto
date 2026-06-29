package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.UpdateAssignmentStatusRequest;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.Assignment;
import ch.zhaw.praesto.model.AssignmentCreateDTO;
import ch.zhaw.praesto.model.AssignmentStatus;
import ch.zhaw.praesto.model.SchoolClass;
import ch.zhaw.praesto.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * Aufgabenverwaltung. Alle Einzelabfragen und Listen sind auf die {@code schoolId}
 * des aktuellen Users gefiltert (Mandanten-Isolation).
 */
@RestController
@RequestMapping("/api")
public class AssignmentController {

    private static final String ASSIGNMENT_NICHT_GEFUNDEN = "Assignment nicht gefunden";
    private static final String TEACHER = "TEACHER";
    private static final String KEINE_BERECHTIGUNG = "Keine Berechtigung fuer dieses Assignment";

    private final AssignmentRepository assignmentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final UserService userService;

    public AssignmentController(
            AssignmentRepository assignmentRepository,
            SchoolClassRepository schoolClassRepository,
            UserService userService) {
        this.assignmentRepository = assignmentRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.userService = userService;
    }

    @PostMapping("/assignment")
    public ResponseEntity<List<Assignment>> createAssignment(@RequestBody AssignmentCreateDTO dto) {

        if (!userService.userHasRole(TEACHER)) {
            throw new ForbiddenException("Nur Lehrer dürfen Assignments erstellen");
        }

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Titel darf nicht leer sein");
        }
        if (dto.getType() == null) {
            throw new BadRequestException("Aufgabentyp fehlt");
        }

        // Klassen bestimmen: classIds (mehrere) hat Vorrang, sonst classId (einzeln)
        List<String> classIds = (dto.getClassIds() != null && !dto.getClassIds().isEmpty())
                ? dto.getClassIds()
                : (dto.getClassId() != null ? List.of(dto.getClassId()) : List.of());
        if (classIds.isEmpty()) {
            throw new BadRequestException("Bitte mindestens eine Klasse auswählen");
        }

        String schoolId = userService.getCurrentSchoolId();
        String teacherId = userService.getCurrentUserId();
        Instant now = Instant.now();
        Instant dueDate = dto.getDueDate() != null ? Instant.parse(dto.getDueDate()) : null;

        List<Assignment> created = new java.util.ArrayList<>();
        for (String classId : classIds) {
            SchoolClass schoolClass = schoolClassRepository.findByIdAndSchoolId(classId, schoolId)
                    .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));
            if (!schoolClass.getTeacherId().equals(teacherId)) {
                throw new ForbiddenException("Keine Berechtigung fuer diese Klasse");
            }

            Assignment assignment = Assignment.builder()
                    .schoolId(schoolId)
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .durationMin(clampDuration(dto.getType(), dto.getDurationMin()))
                    .dueDate(dueDate)
                    .classId(classId)
                    .type(dto.getType())
                    .status(AssignmentStatus.ASSIGNED)
                    .createdByTeacherId(teacherId)
                    .createdAt(now)
                    .build();
            created.add(assignmentRepository.save(assignment));
        }

        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // PUT Status nur Teacher
    @PutMapping("/{id}/status")
    public ResponseEntity<Assignment> updateStatus(
            @PathVariable String id,
            @RequestBody UpdateAssignmentStatusRequest req) {
        if (!userService.userHasRole(TEACHER)) {
            throw new ForbiddenException("Nur Lehrer duerfen den Status ändern");
        }
        if (req.getStatus() == null) {
            throw new BadRequestException("Status fehlt");
        }

        Assignment assignment = requireOwnAssignment(id);
        assignment.setStatus(req.getStatus());
        return ResponseEntity.ok(assignmentRepository.save(assignment));
    }

    // GET Alle Assignments des aktuellen Lehrers (MUSS VOR /{classId} kommen!)
    @GetMapping("/assignments/teacher")
    public ResponseEntity<List<Assignment>> getMyAssignments() {
        if (!userService.userHasRole(TEACHER)) {
            throw new ForbiddenException("Nur Lehrer duerfen ihre Assignments sehen");
        }
        List<Assignment> assignments = assignmentRepository
                .findBySchoolIdAndCreatedByTeacherIdOrderByCreatedAtDesc(
                        userService.getCurrentSchoolId(), userService.getCurrentUserId());
        return ResponseEntity.ok(assignments);
    }

    // GET Assignments fuer eine Klasse - Teacher und Student duerfen lesen
    @GetMapping("/assignments/class/{classId}")
    public ResponseEntity<List<Assignment>> getAssignmentsForClass(@PathVariable String classId) {
        if (!userService.userHasRole(TEACHER) && !userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Lehrer oder Schueler duerfen Assignments ansehen");
        }
        List<Assignment> assignments = assignmentRepository
                .findBySchoolIdAndClassId(userService.getCurrentSchoolId(), classId);
        return ResponseEntity.ok(assignments);
    }

    // GET Eine einzelne Assignment (Teacher und Student)
    @GetMapping("/assignments/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable String id) {
        Assignment assignment = requireAssignmentInSchool(id);

        boolean isTeacher = userService.userHasRole(TEACHER);
        boolean isStudent = userService.userHasRole("STUDENT");

        if (isTeacher) {
            if (!assignment.getCreatedByTeacherId().equals(userService.getCurrentUserId())) {
                throw new ForbiddenException(KEINE_BERECHTIGUNG);
            }
        } else if (isStudent) {
            SchoolClass schoolClass = schoolClassRepository
                    .findByIdAndSchoolId(assignment.getClassId(), userService.getCurrentSchoolId())
                    .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));
            if (!schoolClass.hasStudent(userService.getCurrentUserId())) {
                throw new ForbiddenException("Du bist nicht in dieser Klasse");
            }
        } else {
            throw new ForbiddenException("Keine Berechtigung");
        }

        return ResponseEntity.ok(assignment);
    }

    // DELETE Assignment (nur Teacher)
    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable String id) {
        if (!userService.userHasRole(TEACHER)) {
            throw new ForbiddenException("Nur Lehrer duerfen Assignments loeschen");
        }
        Assignment assignment = requireOwnAssignment(id);
        assignmentRepository.delete(assignment);
        return ResponseEntity.noContent().build();
    }

    // PUT Assignment bearbeiten (nur Teacher)
    @PutMapping("/assignments/{id}")
    public ResponseEntity<Assignment> updateAssignment(
            @PathVariable String id,
            @RequestBody AssignmentCreateDTO dto) {

        if (!userService.userHasRole(TEACHER)) {
            throw new ForbiddenException("Nur Lehrer duerfen Assignments bearbeiten");
        }

        Assignment assignment = requireOwnAssignment(id);

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Titel darf nicht leer sein");
        }

        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setType(dto.getType());
        assignment.setClassId(dto.getClassId());
        assignment.setDurationMin(clampDuration(dto.getType(), dto.getDurationMin()));
        if (dto.getDueDate() != null) {
            assignment.setDueDate(Instant.parse(dto.getDueDate()));
        }

        return ResponseEntity.ok(assignmentRepository.save(assignment));
    }

    /**
     * Begrenzt die Gesprächsdauer. KI-Bewerbungsgespräche sind als Übung gedacht und
     * kosten KI-Zeit -> hartes Maximum von 20 Min. (Kostenbremse). Andere Typen
     * unverändert. Leere/zu kleine Werte werden sinnvoll gesetzt.
     */
    private static final int MAX_AI_INTERVIEW_MIN = 20;

    private Integer clampDuration(AssignmentType type, Integer durationMin) {
        if (type != AssignmentType.AI_INTERVIEW) {
            return durationMin;
        }
        int v = (durationMin == null || durationMin < 1) ? 15 : durationMin;
        return Math.min(v, MAX_AI_INTERVIEW_MIN);
    }

    // ============================================================
    // Intern
    // ============================================================

    private Assignment requireAssignmentInSchool(String id) {
        return assignmentRepository.findByIdAndSchoolId(id, userService.getCurrentSchoolId())
                .orElseThrow(() -> new NotFoundException(ASSIGNMENT_NICHT_GEFUNDEN));
    }

    private Assignment requireOwnAssignment(String id) {
        Assignment assignment = requireAssignmentInSchool(id);
        if (!assignment.getCreatedByTeacherId().equals(userService.getCurrentUserId())) {
            throw new ForbiddenException(KEINE_BERECHTIGUNG);
        }
        return assignment;
    }
}

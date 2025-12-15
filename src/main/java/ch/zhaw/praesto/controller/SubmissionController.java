package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.SubmissionRepository;
import ch.zhaw.praesto.service.BadgeService;
import ch.zhaw.praesto.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private static final String AUFGABE_NICHT_GEFUNDEN = "Aufgabe nicht gefunden";
    private static final String STUDENT = "STUDENT";
    private static final String TEACHER = "TEACHER";
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final UserService userService;
    private final BadgeService badgeService;

    /**
     * POST /api/submissions - Neue Abgabe erstellen (Student)
     */
    @PostMapping("")
    public ResponseEntity<Submission> createSubmission(@RequestBody SubmissionCreateDTO dto) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler koennen Aufgaben abgeben");
        }

        String studentId = userService.getUserId();
        String studentEmail = userService.getEmail().toLowerCase();

        // Assignment prüfen
        Assignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new NotFoundException(AUFGABE_NICHT_GEFUNDEN));

        // Prüfen ob Student in der Klasse ist
        SchoolClass schoolClass = schoolClassRepository.findById(assignment.getClassId())
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));

        if (!schoolClass.hasStudent(studentEmail)) {
            throw new ForbiddenException("Du bist nicht in dieser Klasse");
        }

        // Prüfen ob bereits abgegeben
        if (submissionRepository.existsByAssignmentIdAndStudentEmail(dto.getAssignmentId(), studentEmail)) {
            throw new BadRequestException("Du hast diese Aufgabe bereits abgegeben");
        }

        // Validierung je nach Typ
        validateSubmission(assignment.getType(), dto);

        // Submission erstellen
        Submission submission = Submission.builder()
                .assignmentId(dto.getAssignmentId())
                .studentEmail(studentEmail)
                .type(assignment.getType())
                .textContent(dto.getTextContent())
                .links(dto.getLinks())
                .fileUrl(dto.getFileUrl())
                .fileName(dto.getFileName())
                .comment(dto.getComment())
                .chatSessionId(dto.getChatSessionId())
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(Instant.now())
                .build();

        Submission saved = submissionRepository.save(submission);

        // NEU: Badge-Check nach Abgabe
        badgeService.checkAndAwardBadges(studentId);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * GET /api/submissions/my - Alle eigenen Abgaben (Student)
     */
    @GetMapping("/my")
    public ResponseEntity<List<Submission>> getMySubmissions() {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur fuer Schueler");
        }

        String studentEmail = userService.getEmail().toLowerCase();
        List<Submission> submissions = submissionRepository.findByStudentEmail(studentEmail);
        return ResponseEntity.ok(submissions);
    }

    /**
     * GET /api/submissions/assignment/{assignmentId} - Abgaben für eine Aufgabe
     * (Teacher)
     */
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<Submission>> getSubmissionsForAssignment(@PathVariable String assignmentId) {
        if (!userService.userHasRole(TEACHER)) {
            throw new ForbiddenException("Nur fuer Lehrer");
        }

        // Prüfen ob Teacher Zugriff auf Assignment hat
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException(AUFGABE_NICHT_GEFUNDEN));

        if (!assignment.getCreatedByTeacherId().equals(userService.getUserId())) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Aufgabe");
        }

        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        return ResponseEntity.ok(submissions);
    }

    /**
     * GET /api/submissions/check/{assignmentId} - Prüfen ob Student bereits
     * abgegeben hat
     */
    @GetMapping("/check/{assignmentId}")
    public ResponseEntity<Map<String, Object>> checkSubmission(@PathVariable String assignmentId) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur fuer Schueler");
        }

        String studentEmail = userService.getEmail().toLowerCase();
        boolean hasSubmitted = submissionRepository.existsByAssignmentIdAndStudentEmail(assignmentId, studentEmail);

        Submission submission = null;
        if (hasSubmitted) {
            submission = submissionRepository.findByAssignmentIdAndStudentEmail(assignmentId, studentEmail)
                    .orElse(null);
        }

        return ResponseEntity.ok(Map.of(
                "hasSubmitted", hasSubmitted,
                "submission", submission != null ? submission : Map.of()));
    }

    /**
     * PUT /api/submissions/{id}/feedback - Feedback geben (Teacher)
     * 
     * HINWEIS: Badge-Check für FEEDBACK_RECEIVED und GRADES_RECEIVED wird hier
     * nicht ausgelöst,
     * da der aktuelle User der Lehrer ist. Die Badges werden vergeben, wenn der
     * Student
     * das nächste Mal eine Aktion durchführt (z.B. Aufgabe abgeben, Session
     * starten).
     */
    @PutMapping("/{id}/feedback")
    public ResponseEntity<Submission> giveFeedback(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {

        if (!userService.userHasRole(TEACHER)) {
            throw new ForbiddenException("Nur Lehrer koennen Feedback geben");
        }

        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Abgabe nicht gefunden"));

        // Prüfen ob Teacher Zugriff hat
        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new NotFoundException(AUFGABE_NICHT_GEFUNDEN));

        if (!assignment.getCreatedByTeacherId().equals(userService.getUserId())) {
            throw new ForbiddenException("Keine Berechtigung");
        }

        // Feedback setzen
        String feedback = (String) body.get("feedback");
        Integer grade = body.get("grade") != null ? Integer.parseInt(body.get("grade").toString()) : null;

        submission.setTeacherFeedback(feedback);
        submission.setGrade(grade);
        submission.setStatus(SubmissionStatus.REVIEWED);
        submission.setReviewedAt(Instant.now());
        submission.setReviewedByTeacherId(userService.getUserId());

        Submission updated = submissionRepository.save(submission);
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /api/submissions/{id} - Eine Abgabe holen
     */
    @GetMapping("/{id}")
    public ResponseEntity<Submission> getSubmission(@PathVariable String id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Abgabe nicht gefunden"));

        String userEmail = userService.getEmail().toLowerCase();
        boolean isStudent = userService.userHasRole(STUDENT);
        boolean isTeacher = userService.userHasRole(TEACHER);

        // Student darf nur eigene sehen
        if (isStudent && !submission.getStudentEmail().equals(userEmail)) {
            throw new ForbiddenException("Keine Berechtigung");
        }

        // Teacher darf nur Abgaben seiner Aufgaben sehen
        if (isTeacher) {
            Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                    .orElseThrow(() -> new NotFoundException(AUFGABE_NICHT_GEFUNDEN));
            if (!assignment.getCreatedByTeacherId().equals(userService.getUserId())) {
                throw new ForbiddenException("Keine Berechtigung");
            }
        }

        return ResponseEntity.ok(submission);
    }

    /**
     * Validierung je nach Aufgabentyp
     */
    private void validateSubmission(AssignmentType type, SubmissionCreateDTO dto) {
        switch (type) {
            case SELF_REFLECTION, RESEARCH -> validateTextContent(dto, type);
            case DOCUMENT_UPLOAD, VIDEO_PITCH -> validateFileUrl(dto, type);
            case AI_INTERVIEW -> validateChatSession(dto);
            default -> {
            }
        }
    }

    private void validateTextContent(SubmissionCreateDTO dto, AssignmentType type) {
        if (dto.getTextContent() == null || dto.getTextContent().trim().length() < 50) {
            String name = type == AssignmentType.SELF_REFLECTION ? "Reflexion" : "Recherche";
            throw new BadRequestException(name + " muss mindestens 50 Zeichen haben");
        }
    }

    private void validateFileUrl(SubmissionCreateDTO dto, AssignmentType type) {
        if (dto.getFileUrl() == null || dto.getFileUrl().isBlank()) {
            String name = type == AssignmentType.DOCUMENT_UPLOAD ? "Dokument" : "Video";
            throw new BadRequestException(name + "-URL fehlt");
        }
    }

    private void validateChatSession(SubmissionCreateDTO dto) {
        if (dto.getChatSessionId() == null || dto.getChatSessionId().isBlank()) {
            throw new BadRequestException("Chat-Session-ID fehlt");
        }
    }

}
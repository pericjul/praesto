package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Submission;
import ch.zhaw.praesto.model.SubmissionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends MongoRepository<Submission, String> {

    /**
     * Alle Abgaben eines Schülers
     */
    List<Submission> findByStudentEmail(String studentEmail);

    /**
     * Alle Abgaben für eine Aufgabe
     */
    List<Submission> findByAssignmentId(String assignmentId);

    /**
     * Abgabe eines Schülers für eine bestimmte Aufgabe
     */
    Optional<Submission> findByAssignmentIdAndStudentEmail(String assignmentId, String studentEmail);

    /**
     * Prüfen ob Schüler bereits abgegeben hat
     */
    boolean existsByAssignmentIdAndStudentEmail(String assignmentId, String studentEmail);

    /**
     * Alle Abgaben mit bestimmtem Status
     */
    List<Submission> findByStatus(SubmissionStatus status);

    /**
     * Anzahl Abgaben für eine Aufgabe
     */
    long countByAssignmentId(String assignmentId);

    long countByStudentEmail(String studentEmail);

    long countByStudentEmailAndTeacherFeedbackIsNotNull(String studentEmail);

    long countByStudentEmailAndGradeIsNotNull(String studentEmail);
}
package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Submission;
import ch.zhaw.praesto.model.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, String> {

    /**
     * Alle Abgaben eines Schülers
     */
    List<Submission> findByStudentEmail(String studentEmail);

    /**
     * Abgabe, zu der eine hochgeladene Datei gehört (für die Download-Autorisierung).
     */
    Optional<Submission> findFirstByFileUrl(String fileUrl);

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

    // ===== Mandanten-sichere (schoolId-gefilterte) Varianten =====

    Optional<Submission> findByIdAndSchoolId(String id, String schoolId);

    long countBySchoolId(String schoolId);

    // ===== Personenbezogen (DSG-Export/Löschung) =====

    List<Submission> findByStudentId(String studentId);

    void deleteByStudentId(String studentId);

    void deleteBySchoolId(String schoolId);
}
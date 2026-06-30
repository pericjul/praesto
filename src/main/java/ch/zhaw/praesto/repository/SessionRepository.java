package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Session;
import ch.zhaw.praesto.model.SessionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, String> {

    List<Session> findByStudentId(String studentId);

    /** Anzahl Aufgaben-Gespräche, die ein Schüler seit {@code since} gestartet hat (Wochen-Limit). */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.studentId = :studentId AND s.assignmentId IS NOT NULL AND s.startedAt >= :since")
    long countAssignmentSessionsSince(@Param("studentId") String studentId, @Param("since") Instant since);

    // spaeter fuer Teacher Endpoint:
    List<Session> findByAssignmentId(String assignmentId);

    long countByStudentIdAndStatus(String studentId, SessionStatus status);

    /** Alle Sessions mit einem bestimmten Status (für den Auto-Close-Job genutzt). */
    List<Session> findByStatus(SessionStatus status);

    /** Freie Übungsgespräche (keine Aufgabe) eines Schülers seit einem Zeitpunkt – für die Tages-Quota. */
    long countByStudentIdAndAssignmentIdIsNullAndStartedAtAfter(String studentId, Instant after);

    // ===== Mandanten-sichere (schoolId-gefilterte) Varianten =====

    java.util.Optional<Session> findByIdAndSchoolId(String id, String schoolId);

    long countBySchoolId(String schoolId);

    void deleteBySchoolId(String schoolId);

    void deleteByStudentId(String studentId);
}
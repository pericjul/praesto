package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Session;
import ch.zhaw.praesto.model.SessionStatus;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SessionRepository extends MongoRepository<Session, String> {

    List<Session> findByStudentId(String studentId);

    // spaeter fuer Teacher Endpoint:
    List<Session> findByAssignmentId(String assignmentId);

    long countByStudentIdAndStatus(String studentId, SessionStatus status);

    // ===== Mandanten-sichere (schoolId-gefilterte) Varianten =====

    java.util.Optional<Session> findByIdAndSchoolId(String id, String schoolId);

    long countBySchoolId(String schoolId);

    void deleteBySchoolId(String schoolId);

    void deleteByStudentId(String studentId);
}
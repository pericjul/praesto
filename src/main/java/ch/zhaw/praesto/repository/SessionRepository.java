package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SessionRepository extends MongoRepository<Session, String> {

    List<Session> findByStudentId(String studentId);

    // spaeter fuer Teacher Endpoint:
    List<Session> findByAssignmentId(String assignmentId);
}
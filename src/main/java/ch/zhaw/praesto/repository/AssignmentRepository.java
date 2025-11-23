package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Assignment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AssignmentRepository extends MongoRepository<Assignment, String> {
    List<Assignment> findByClassId(String classId);
}
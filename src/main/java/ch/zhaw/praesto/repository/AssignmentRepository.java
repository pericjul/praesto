package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Assignment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AssignmentRepository extends MongoRepository<Assignment, String> {

    List<Assignment> findByClassId(String classId);

    List<Assignment> findByCreatedByTeacherIdOrderByCreatedAtDesc(String teacherId);

    // ===== Mandanten-sichere (schoolId-gefilterte) Varianten =====

    java.util.Optional<Assignment> findByIdAndSchoolId(String id, String schoolId);

    List<Assignment> findBySchoolId(String schoolId);

    List<Assignment> findBySchoolIdAndClassId(String schoolId, String classId);

    List<Assignment> findBySchoolIdAndCreatedByTeacherIdOrderByCreatedAtDesc(String schoolId, String teacherId);

    long countBySchoolId(String schoolId);

    void deleteBySchoolId(String schoolId);
}
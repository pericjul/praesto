package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, String> {

    List<Document> findByStudentIdOrderByCreatedAtDesc(String studentId);

    void deleteBySchoolId(String schoolId);

    void deleteByStudentIdIn(List<String> studentIds);
}

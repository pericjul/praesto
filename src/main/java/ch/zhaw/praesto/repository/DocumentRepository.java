package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, String> {

    List<Document> findByStudentIdOrderByCreatedAtDesc(String studentId);

    Optional<Document> findFirstByFileUrl(String fileUrl);

    void deleteBySchoolId(String schoolId);

    void deleteByStudentIdIn(List<String> studentIds);
}

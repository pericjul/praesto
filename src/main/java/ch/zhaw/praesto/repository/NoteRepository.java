package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, String> {

    List<Note> findByStudentId(String studentId);

    List<Note> findByStudentIdOrderByCreatedAtDesc(String studentId);

    long countByStudentId(String studentId);

    void deleteByStudentIdIn(java.util.Collection<String> studentIds);
}
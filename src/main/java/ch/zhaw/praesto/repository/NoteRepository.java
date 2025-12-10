package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NoteRepository extends MongoRepository<Note, String> {
    
    List<Note> findByStudentId(String studentId);
    
    List<Note> findByStudentIdOrderByCreatedAtDesc(String studentId);
}
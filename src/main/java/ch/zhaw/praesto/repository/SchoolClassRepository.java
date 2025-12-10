package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.SchoolClass;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolClassRepository extends MongoRepository<SchoolClass, String> {
    
    /**
     * Alle Klassen eines Lehrers finden.
     */
    List<SchoolClass> findByTeacherId(String teacherId);

    /**
     * Klasse finden in der ein bestimmter Schüler ist.
     */
    List<SchoolClass> findByStudentIdsContaining(String studentId);

    /**
     * Erste Klasse eines Schülers finden (normalerweise ist ein Schüler nur in einer Klasse).
     */
    Optional<SchoolClass> findFirstByStudentIdsContaining(String studentId);

    /**
     * Klasse nach Name finden.
     */
    Optional<SchoolClass> findByName(String name);

    /**
     * Prüfen ob Klassenname bereits existiert.
     */
    boolean existsByName(String name);
}
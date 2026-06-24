package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.SchoolClass;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolClassRepository extends MongoRepository<SchoolClass, String> {

    /**
     * Alle Klassen eines Lehrers innerhalb seiner Schule.
     */
    List<SchoolClass> findBySchoolIdAndTeacherId(String schoolId, String teacherId);

    /**
     * Alle Klassen einer Schule.
     */
    List<SchoolClass> findBySchoolId(String schoolId);

    /**
     * Eine Klasse innerhalb der eigenen Schule (Mandanten-sichere Einzelabfrage).
     */
    Optional<SchoolClass> findByIdAndSchoolId(String id, String schoolId);

    /**
     * Klasse(n) in der ein bestimmter Schüler ist (per User-Id).
     */
    List<SchoolClass> findByStudentIdsContaining(String userId);

    /**
     * Erste Klasse eines Schülers (innerhalb seiner Schule).
     */
    Optional<SchoolClass> findFirstBySchoolIdAndStudentIdsContaining(String schoolId, String userId);

    /**
     * Prüfen ob Klassenname innerhalb der Schule bereits existiert.
     */
    boolean existsBySchoolIdAndName(String schoolId, String name);

    long countBySchoolId(String schoolId);

    void deleteBySchoolId(String schoolId);
}

package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, String> {

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
    @Query("select c from SchoolClass c join c.studentIds s where s = :userId")
    List<SchoolClass> findByStudentIdsContaining(@Param("userId") String userId);

    /**
     * Klassen eines Schülers innerhalb seiner Schule.
     */
    @Query("select c from SchoolClass c join c.studentIds s where c.schoolId = :schoolId and s = :userId")
    List<SchoolClass> findClassesOfStudentInSchool(@Param("schoolId") String schoolId, @Param("userId") String userId);

    /**
     * Prüfen ob Klassenname innerhalb der Schule bereits existiert.
     */
    boolean existsBySchoolIdAndName(String schoolId, String name);

    long countBySchoolId(String schoolId);

    void deleteBySchoolId(String schoolId);
}

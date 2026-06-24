package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.SchoolClass;
import ch.zhaw.praesto.model.SchoolClassDTO;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Klassenverwaltung. Alle Abfragen sind auf die {@code schoolId} des aktuellen
 * Users gefiltert (Mandanten-Isolation). Schüler werden über ihre User-Id geführt.
 */
@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private static final String KLASSE_NICHT_GEFUNDEN = "Klasse nicht gefunden";
    private static final String TEACHER = "TEACHER";
    private static final String KEINE_BERECHTIGUNG = "Keine Berechtigung fuer diese Klasse";

    private final SchoolClassRepository schoolClassRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Neue Klasse erstellen (nur Lehrer).
     */
    public SchoolClass createClass(SchoolClassDTO dto) {
        requireTeacher("Nur Lehrer duerfen Klassen erstellen");

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Klassenname ist erforderlich");
        }

        String schoolId = userService.getCurrentSchoolId();
        if (schoolClassRepository.existsBySchoolIdAndName(schoolId, dto.getName().trim())) {
            throw new BadRequestException("Eine Klasse mit diesem Namen existiert bereits");
        }

        Instant now = Instant.now();
        SchoolClass schoolClass = SchoolClass.builder()
                .schoolId(schoolId)
                .name(dto.getName().trim())
                .teacherId(userService.getCurrentUserId())
                .studentIds(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Alle Klassen des aktuellen Lehrers (innerhalb seiner Schule).
     */
    public List<SchoolClass> getMyClasses() {
        requireTeacher("Nur Lehrer duerfen Klassen sehen");
        return schoolClassRepository.findBySchoolIdAndTeacherId(
                userService.getCurrentSchoolId(), userService.getCurrentUserId());
    }

    /**
     * Eine Klasse nach ID holen (Mandanten-sicher).
     */
    public SchoolClass getClassById(String classId) {
        SchoolClass schoolClass = requireClassInSchool(classId);

        String userId = userService.getCurrentUserId();
        boolean isTeacher = userService.userHasRole(TEACHER);
        boolean isStudent = userService.userHasRole("STUDENT");

        if (isTeacher && !schoolClass.getTeacherId().equals(userId)) {
            throw new ForbiddenException(KEINE_BERECHTIGUNG);
        }
        if (isStudent && !schoolClass.hasStudent(userId)) {
            throw new ForbiddenException("Du bist nicht in dieser Klasse");
        }

        return schoolClass;
    }

    /**
     * Klasse des aktuellen Schülers holen.
     */
    public SchoolClass getMyClass() {
        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler haben eine Klasse");
        }
        return schoolClassRepository.findClassesOfStudentInSchool(
                        userService.getCurrentSchoolId(), userService.getCurrentUserId())
                .stream().findFirst()
                .orElseThrow(() -> new NotFoundException("Du bist keiner Klasse zugeordnet"));
    }

    /**
     * ClassId des aktuellen Schülers holen (für Dashboard etc.).
     */
    public String getMyClassId() {
        return schoolClassRepository.findClassesOfStudentInSchool(
                        userService.getCurrentSchoolId(), userService.getCurrentUserId())
                .stream().findFirst()
                .map(SchoolClass::getId)
                .orElse(null);
    }

    /**
     * Klasse aktualisieren (nur eigene Klassen).
     */
    public SchoolClass updateClass(String classId, SchoolClassDTO dto) {
        requireTeacher("Nur Lehrer duerfen Klassen bearbeiten");
        SchoolClass schoolClass = requireOwnClass(classId);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            String newName = dto.getName().trim();
            if (!schoolClass.getName().equals(newName)
                    && schoolClassRepository.existsBySchoolIdAndName(schoolClass.getSchoolId(), newName)) {
                throw new BadRequestException("Eine Klasse mit diesem Namen existiert bereits");
            }
            schoolClass.setName(newName);
        }

        schoolClass.setUpdatedAt(Instant.now());
        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Schüler zur Klasse hinzufügen (per User-Id). Validiert, dass der Schüler zur selben Schule gehört.
     */
    public SchoolClass addStudentToClass(String classId, String userId) {
        requireTeacher("Nur Lehrer duerfen Schueler hinzufuegen");

        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("userId ist erforderlich");
        }

        SchoolClass schoolClass = requireOwnClass(classId);

        User student = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Schüler nicht gefunden"));

        if (student.getRole() != UserRole.STUDENT
                || !schoolClass.getSchoolId().equals(student.getSchoolId())) {
            throw new ForbiddenException("Dieser Schüler gehört nicht zu deiner Schule");
        }

        if (schoolClass.hasStudent(userId)) {
            throw new BadRequestException("Dieser Schueler ist bereits in der Klasse");
        }

        schoolClass.addStudent(userId);
        schoolClass.setUpdatedAt(Instant.now());
        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Schüler aus Klasse entfernen (per User-Id).
     */
    public SchoolClass removeStudentFromClass(String classId, String userId) {
        requireTeacher("Nur Lehrer duerfen Schueler entfernen");
        SchoolClass schoolClass = requireOwnClass(classId);

        schoolClass.removeStudent(userId);
        schoolClass.setUpdatedAt(Instant.now());
        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Klasse löschen.
     */
    public void deleteClass(String classId) {
        requireTeacher("Nur Lehrer duerfen Klassen loeschen");
        SchoolClass schoolClass = requireOwnClass(classId);
        schoolClassRepository.delete(schoolClass);
    }

    // ============================================================
    // Intern
    // ============================================================

    private void requireTeacher(String message) {
        if (!userService.userHasRole(TEACHER)) {
            throw new ForbiddenException(message);
        }
    }

    private SchoolClass requireClassInSchool(String classId) {
        return schoolClassRepository.findByIdAndSchoolId(classId, userService.getCurrentSchoolId())
                .orElseThrow(() -> new NotFoundException(KLASSE_NICHT_GEFUNDEN));
    }

    private SchoolClass requireOwnClass(String classId) {
        SchoolClass schoolClass = requireClassInSchool(classId);
        if (!schoolClass.getTeacherId().equals(userService.getCurrentUserId())) {
            throw new ForbiddenException(KEINE_BERECHTIGUNG);
        }
        return schoolClass;
    }
}

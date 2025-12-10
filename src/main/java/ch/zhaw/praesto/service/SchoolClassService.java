package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.SchoolClass;
import ch.zhaw.praesto.model.SchoolClassDTO;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final UserService userService;

    /**
     * Neue Klasse erstellen (nur Lehrer).
     */
    public SchoolClass createClass(SchoolClassDTO dto) {
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen Klassen erstellen");
        }

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Klassenname ist erforderlich");
        }

        if (schoolClassRepository.existsByName(dto.getName().trim())) {
            throw new BadRequestException("Eine Klasse mit diesem Namen existiert bereits");
        }

        String teacherId = userService.getUserId();
        Instant now = Instant.now();

        SchoolClass schoolClass = SchoolClass.builder()
                .name(dto.getName().trim())
                .teacherId(teacherId)
                .studentIds(dto.getStudentIds() != null ? dto.getStudentIds() : new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Alle Klassen des aktuellen Lehrers.
     */
    public List<SchoolClass> getMyClasses() {
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen Klassen sehen");
        }

        String teacherId = userService.getUserId();
        return schoolClassRepository.findByTeacherId(teacherId);
    }

    /**
     * Alle Klassen (für Admin oder Übersicht).
     */
    public List<SchoolClass> getAllClasses() {
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Keine Berechtigung");
        }
        return schoolClassRepository.findAll();
    }

    /**
     * Eine Klasse nach ID holen.
     */
    public SchoolClass getClassById(String classId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));

        // Lehrer darf nur eigene Klassen sehen, Schüler nur ihre Klasse
        String userId = userService.getUserId();
        boolean isTeacher = userService.userHasRole("TEACHER");
        boolean isStudent = userService.userHasRole("STUDENT");

        if (isTeacher && !schoolClass.getTeacherId().equals(userId)) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Klasse");
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

        String studentId = userService.getUserId();
        return schoolClassRepository.findFirstByStudentIdsContaining(studentId)
                .orElseThrow(() -> new NotFoundException("Du bist keiner Klasse zugeordnet"));
    }

    /**
     * Klasse aktualisieren (nur eigene Klassen).
     */
    public SchoolClass updateClass(String classId, SchoolClassDTO dto) {
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen Klassen bearbeiten");
        }

        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));

        String teacherId = userService.getUserId();
        if (!schoolClass.getTeacherId().equals(teacherId)) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Klasse");
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            // Prüfen ob neuer Name bereits vergeben (ausser bei gleicher Klasse)
            if (!schoolClass.getName().equals(dto.getName().trim()) &&
                    schoolClassRepository.existsByName(dto.getName().trim())) {
                throw new BadRequestException("Eine Klasse mit diesem Namen existiert bereits");
            }
            schoolClass.setName(dto.getName().trim());
        }

        if (dto.getStudentIds() != null) {
            schoolClass.setStudentIds(dto.getStudentIds());
        }

        schoolClass.setUpdatedAt(Instant.now());
        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Schüler zur Klasse hinzufügen.
     */
    public SchoolClass addStudentToClass(String classId, String studentId) {
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen Schueler hinzufuegen");
        }

        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));

        String teacherId = userService.getUserId();
        if (!schoolClass.getTeacherId().equals(teacherId)) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Klasse");
        }

        schoolClass.addStudent(studentId);
        schoolClass.setUpdatedAt(Instant.now());
        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Schüler aus Klasse entfernen.
     */
    public SchoolClass removeStudentFromClass(String classId, String studentId) {
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen Schueler entfernen");
        }

        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));

        String teacherId = userService.getUserId();
        if (!schoolClass.getTeacherId().equals(teacherId)) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Klasse");
        }

        schoolClass.removeStudent(studentId);
        schoolClass.setUpdatedAt(Instant.now());
        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Klasse löschen.
     */
    public void deleteClass(String classId) {
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen Klassen loeschen");
        }

        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));

        String teacherId = userService.getUserId();
        if (!schoolClass.getTeacherId().equals(teacherId)) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Klasse");
        }

        schoolClassRepository.delete(schoolClass);
    }
}
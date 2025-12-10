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
                .studentEmails(new ArrayList<>())
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
     * Eine Klasse nach ID holen.
     */
    public SchoolClass getClassById(String classId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));

        String userId = userService.getUserId();
        String userEmail = userService.getEmail();
        boolean isTeacher = userService.userHasRole("TEACHER");
        boolean isStudent = userService.userHasRole("STUDENT");

        if (isTeacher && !schoolClass.getTeacherId().equals(userId)) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Klasse");
        }

        if (isStudent && !schoolClass.hasStudent(userEmail)) {
            throw new ForbiddenException("Du bist nicht in dieser Klasse");
        }

        return schoolClass;
    }

    /**
     * Klasse des aktuellen Schülers holen (per Email aus JWT).
     */
    public SchoolClass getMyClass() {
        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler haben eine Klasse");
        }

        String email = userService.getEmail();
        return schoolClassRepository.findFirstByStudentEmailsContaining(email.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Du bist keiner Klasse zugeordnet"));
    }

    /**
     * ClassId des aktuellen Schülers holen (für Dashboard etc.).
     */
    public String getMyClassId() {
        String email = userService.getEmail();
        if (email == null) return null;
        
        return schoolClassRepository.findFirstByStudentEmailsContaining(email.toLowerCase())
                .map(SchoolClass::getId)
                .orElse(null);
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
            if (!schoolClass.getName().equals(dto.getName().trim()) &&
                    schoolClassRepository.existsByName(dto.getName().trim())) {
                throw new BadRequestException("Eine Klasse mit diesem Namen existiert bereits");
            }
            schoolClass.setName(dto.getName().trim());
        }

        schoolClass.setUpdatedAt(Instant.now());
        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Schüler zur Klasse hinzufügen (per Email).
     */
    public SchoolClass addStudentToClass(String classId, String studentEmail) {
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen Schueler hinzufuegen");
        }

        if (studentEmail == null || studentEmail.isBlank()) {
            throw new BadRequestException("Email ist erforderlich");
        }

        // Einfache Email-Validierung
        if (!studentEmail.contains("@")) {
            throw new BadRequestException("Ungueltige Email-Adresse");
        }

        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));

        String teacherId = userService.getUserId();
        if (!schoolClass.getTeacherId().equals(teacherId)) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Klasse");
        }

        String normalizedEmail = studentEmail.toLowerCase().trim();
        
        // Prüfen ob Email schon in dieser Klasse ist
        if (schoolClass.hasStudent(normalizedEmail)) {
            throw new BadRequestException("Dieser Schueler ist bereits in der Klasse");
        }

        schoolClass.addStudent(normalizedEmail);
        schoolClass.setUpdatedAt(Instant.now());
        return schoolClassRepository.save(schoolClass);
    }

    /**
     * Schüler aus Klasse entfernen.
     */
    public SchoolClass removeStudentFromClass(String classId, String studentEmail) {
        if (!userService.userHasRole("TEACHER")) {
            throw new ForbiddenException("Nur Lehrer duerfen Schueler entfernen");
        }

        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));

        String teacherId = userService.getUserId();
        if (!schoolClass.getTeacherId().equals(teacherId)) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Klasse");
        }

        schoolClass.removeStudent(studentEmail);
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
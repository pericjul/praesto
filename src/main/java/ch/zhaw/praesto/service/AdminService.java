package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.SchoolRepository;
import ch.zhaw.praesto.repository.SessionRepository;
import ch.zhaw.praesto.repository.SubmissionRepository;
import ch.zhaw.praesto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Verwaltungs-Operationen für SCHOOL_ADMIN (User der eigenen Schule) und
 * SUPER_ADMIN (Schulen). Jede Methode prüft die Rolle und filtert auf die
 * eigene {@code schoolId}.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int MIN_QUERY_LENGTH = 2;

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final AssignmentRepository assignmentRepository;
    private final SessionRepository sessionRepository;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;

    // ============================================================
    // SCHOOL_ADMIN
    // ============================================================

    public List<User> listSchoolUsers() {
        requireRole(UserRole.SCHOOL_ADMIN);
        return userRepository.findBySchoolId(userService.getCurrentSchoolId());
    }

    public List<User> listTeachers() {
        requireRole(UserRole.SCHOOL_ADMIN);
        return userRepository.findBySchoolIdAndRole(userService.getCurrentSchoolId(), UserRole.TEACHER);
    }

    /**
     * Schüler-Suche für die Schulleitung (q optional).
     */
    public List<User> listStudents(String q) {
        requireRole(UserRole.SCHOOL_ADMIN);
        return searchStudents(userService.getCurrentSchoolId(), q);
    }

    /**
     * Schüler-Suche für Lehrer (Suchfeld beim Hinzufügen zur Klasse, min. 2 Zeichen).
     */
    public List<User> searchStudentsForTeacher(String q) {
        requireRole(UserRole.TEACHER);
        if (q == null || q.trim().length() < MIN_QUERY_LENGTH) {
            return List.of();
        }
        return searchStudents(userService.getCurrentSchoolId(), q);
    }

    /**
     * Erweiterte Einblicke fürs Schulleitungs-Dashboard ("Braucht Aufmerksamkeit").
     */
    public SchoolInsights schoolInsights() {
        requireRole(UserRole.SCHOOL_ADMIN);
        String schoolId = userService.getCurrentSchoolId();

        List<User> allUsers = userRepository.findBySchoolId(schoolId);

        List<UserDTO> recentUsers = allUsers.stream()
                .sorted(Comparator.comparing(User::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(UserDTO::from)
                .toList();

        int inactiveUsers = (int) allUsers.stream().filter(u -> !u.isActive()).count();

        Set<String> classIdsWithAssignments = assignmentRepository.findBySchoolId(schoolId).stream()
                .map(Assignment::getClassId)
                .collect(Collectors.toSet());
        int classesWithoutAssignments = (int) schoolClassRepository.findBySchoolId(schoolId).stream()
                .filter(c -> !classIdsWithAssignments.contains(c.getId()))
                .count();

        long totalSessions = sessionRepository.countBySchoolId(schoolId);
        long totalSubmissions = submissionRepository.countBySchoolId(schoolId);

        return new SchoolInsights(recentUsers, classesWithoutAssignments, inactiveUsers,
                totalSessions, totalSubmissions);
    }

    public SchoolStats schoolStats() {
        requireRole(UserRole.SCHOOL_ADMIN);
        String schoolId = userService.getCurrentSchoolId();
        return new SchoolStats(
                userRepository.countBySchoolIdAndRole(schoolId, UserRole.TEACHER),
                userRepository.countBySchoolIdAndRole(schoolId, UserRole.STUDENT),
                schoolClassRepository.countBySchoolId(schoolId),
                assignmentRepository.countBySchoolId(schoolId));
    }

    public void deactivateUser(String userId) {
        requireRole(UserRole.SCHOOL_ADMIN);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User nicht gefunden"));
        if (!userService.getCurrentSchoolId().equals(user.getSchoolId())) {
            throw new ForbiddenException("Keine Berechtigung");
        }
        user.setActive(false);
        userRepository.save(user);
    }

    // ============================================================
    // SUPER_ADMIN
    // ============================================================

    public List<SchoolSummary> listSchools() {
        requireRole(UserRole.SUPER_ADMIN);
        return schoolRepository.findAll().stream()
                .map(s -> SchoolSummary.from(s, userRepository.countBySchoolId(s.getId())))
                .toList();
    }

    public School createSchool(SchoolCreateRequest req) {
        requireRole(UserRole.SUPER_ADMIN);
        if (req.name() == null || req.name().isBlank()) {
            throw new BadRequestException("Schulname ist erforderlich");
        }
        if (schoolRepository.existsByName(req.name().trim())) {
            throw new BadRequestException("Eine Schule mit diesem Namen existiert bereits");
        }
        School school = School.builder()
                .name(req.name().trim())
                .canton(req.canton())
                .city(req.city())
                .isActive(true)
                .createdAt(Instant.now())
                .build();
        return schoolRepository.save(school);
    }

    /**
     * Schule sperren/entsperren (z.B. bei ausbleibender Zahlung). Die Daten bleiben
     * vollständig erhalten – nur der Login aller Nutzer dieser Schule wird blockiert,
     * bis wieder entsperrt wird.
     */
    public void setSchoolActive(String schoolId, boolean active) {
        requireRole(UserRole.SUPER_ADMIN);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ch.zhaw.praesto.exception.NotFoundException("Schule nicht gefunden"));
        school.setActive(active);
        schoolRepository.save(school);
    }

    // ============================================================
    // Intern
    // ============================================================

    /**
     * Vereinigt Treffer aus Vorname/Nachname/Email (case-insensitive), dedupliziert per Id.
     * Ohne Suchbegriff werden alle Schüler der Schule zurückgegeben.
     */
    private List<User> searchStudents(String schoolId, String q) {
        if (q == null || q.isBlank()) {
            return userRepository.findBySchoolIdAndRole(schoolId, UserRole.STUDENT);
        }
        String query = q.trim();
        Map<String, User> byId = new LinkedHashMap<>();
        userRepository.findBySchoolIdAndRoleAndFirstNameContainingIgnoreCase(schoolId, UserRole.STUDENT, query)
                .forEach(u -> byId.put(u.getId(), u));
        userRepository.findBySchoolIdAndRoleAndLastNameContainingIgnoreCase(schoolId, UserRole.STUDENT, query)
                .forEach(u -> byId.put(u.getId(), u));
        userRepository.findBySchoolIdAndRoleAndEmailContainingIgnoreCase(schoolId, UserRole.STUDENT, query)
                .forEach(u -> byId.put(u.getId(), u));
        return List.copyOf(byId.values());
    }

    private void requireRole(UserRole role) {
        if (!userService.userHasRole(role)) {
            throw new ForbiddenException("Keine Berechtigung");
        }
    }
}

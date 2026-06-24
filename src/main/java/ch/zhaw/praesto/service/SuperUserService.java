package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Datenschutz-Werkzeuge für den SUPER_ADMIN:
 * - Datenauskunft / -export (alle Daten einer Person als JSON)
 * - Recht auf Löschung (alle Daten einer Person endgültig entfernen)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SuperUserService {

    private static final int MIN_QUERY = 2;

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final SessionRepository sessionRepository;
    private final SubmissionRepository submissionRepository;
    private final NoteRepository noteRepository;
    private final ApplicationRepository applicationRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserService userService;

    /**
     * Benutzersuche über alle Schulen (Name oder E-Mail).
     */
    public List<UserDTO> search(String q) {
        requireSuper();
        if (q == null || q.trim().length() < MIN_QUERY) {
            return List.of();
        }
        String query = q.trim().toLowerCase();
        return userRepository.findAll().stream()
                .filter(u -> matches(u, query))
                .map(UserDTO::from)
                .toList();
    }

    /**
     * Vollständige Datenauskunft: alle zur Person gehörenden Daten als verschachteltes JSON.
     */
    public Map<String, Object> exportUserData(String userId) {
        requireSuper();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Benutzer nicht gefunden"));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("exportedAt", Instant.now().toString());
        out.put("user", UserDTO.from(user));
        out.put("school", schoolRepository.findById(String.valueOf(user.getSchoolId()))
                .map(School::getName).orElse(null));
        out.put("sessions", sessionRepository.findByStudentId(userId));
        out.put("submissions", submissionRepository.findByStudentId(userId));
        out.put("notes", noteRepository.findByStudentId(userId));
        out.put("applications", applicationRepository.findByStudentId(userId));
        out.put("badges", userBadgeRepository.findByStudentIdOrderByEarnedAtDesc(userId));
        out.put("classMemberships", schoolClassRepository.findByStudentIdsContaining(userId).stream()
                .map(SchoolClass::getName).toList());

        // Für Lehrpersonen: erstellte Klassen & Aufgaben
        if (user.getRole() == UserRole.TEACHER && user.getSchoolId() != null) {
            out.put("ownedClasses", schoolClassRepository
                    .findBySchoolIdAndTeacherId(user.getSchoolId(), userId));
            out.put("createdAssignments", assignmentRepository
                    .findBySchoolIdAndCreatedByTeacherIdOrderByCreatedAtDesc(user.getSchoolId(), userId));
        }

        return out;
    }

    /**
     * Löscht die Person und alle zugehörigen Daten endgültig.
     */
    @Transactional
    public void deleteUserData(String userId) {
        requireSuper();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Benutzer nicht gefunden"));

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new ForbiddenException("Ein SUPER_ADMIN kann nicht gelöscht werden");
        }

        // Persönliche Datensätze
        sessionRepository.deleteByStudentId(userId);
        submissionRepository.deleteByStudentId(userId);
        noteRepository.deleteByStudentIdIn(List.of(userId));
        applicationRepository.deleteByStudentIdIn(List.of(userId));
        userBadgeRepository.deleteByStudentIdIn(List.of(userId));

        // Aus allen Klassen entfernen
        schoolClassRepository.findByStudentIdsContaining(userId).forEach(c -> {
            c.removeStudent(userId);
            c.setUpdatedAt(Instant.now());
            schoolClassRepository.save(c);
        });

        // Lehrperson: eigene Klassen & Aufgaben löschen
        if (user.getRole() == UserRole.TEACHER && user.getSchoolId() != null) {
            assignmentRepository.deleteAll(assignmentRepository
                    .findBySchoolIdAndCreatedByTeacherIdOrderByCreatedAtDesc(user.getSchoolId(), userId));
            schoolClassRepository.deleteAll(schoolClassRepository
                    .findBySchoolIdAndTeacherId(user.getSchoolId(), userId));
        }

        userRepository.delete(user);
        log.info("Alle Daten von Benutzer {} ({}) gelöscht", userId, user.getEmail());
    }

    // ============================================================

    private boolean matches(User u, String query) {
        String full = ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                + (u.getLastName() == null ? "" : u.getLastName())).toLowerCase();
        String email = u.getEmail() == null ? "" : u.getEmail().toLowerCase();
        return full.contains(query) || email.contains(query);
    }

    private void requireSuper() {
        if (!userService.userHasRole(UserRole.SUPER_ADMIN)) {
            throw new ForbiddenException("Keine Berechtigung");
        }
    }
}

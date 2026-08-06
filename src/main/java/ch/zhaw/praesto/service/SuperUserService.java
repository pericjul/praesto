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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final LocaleMessages messages;
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
        purge(user);
    }

    /**
     * Selbst-Löschung des eigenen Kontos inkl. aller Daten (jede:r darf das eigene Konto löschen,
     * ausser SUPER_ADMIN).
     */
    @Transactional
    public void deleteOwnAccount() {
        User user = userRepository.findById(userService.getUserId())
                .orElseThrow(() -> new NotFoundException("Benutzer nicht gefunden"));
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new ForbiddenException("Ein SUPER_ADMIN kann sich nicht selbst löschen");
        }
        purge(user);
    }

    /**
     * System-Löschung (z.B. automatische Aufbewahrungs-Regel): Person + alle Daten löschen,
     * ohne Rechte-Check. Nicht für SUPER_ADMIN.
     */
    @Transactional
    public void deleteAccountAndData(User user) {
        if (user == null || user.getRole() == UserRole.SUPER_ADMIN) {
            return;
        }
        purge(user);
    }

    /** Löscht die Person und alle zugehörigen Daten endgültig (ohne Rechte-Check). */
    private void purge(User user) {
        String userId = user.getId();

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

    /**
     * Aktiviert/deaktiviert eine beliebige Person (schulübergreifend). Deaktivierte
     * Accounts können sich nicht mehr einloggen; die Daten bleiben erhalten.
     */
    public void setActive(String userId, boolean active) {
        requireSuper();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Benutzer nicht gefunden"));
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new ForbiddenException("Ein SUPER_ADMIN kann nicht deaktiviert werden");
        }
        user.setActive(active);
        userRepository.save(user);
        log.info("Benutzer {} ({}) {}", userId, user.getEmail(), active ? "aktiviert" : "deaktiviert");
    }

    // ============================================================

    /** Legt einen weiteren SUPER_ADMIN an (nur durch bestehenden Super-Admin). */
    public User createAdmin(String email, String firstName, String lastName, String password) {
        requireSuper();
        String e = email == null ? null : email.toLowerCase().trim();
        if (e == null || !e.contains("@")) {
            throw new ch.zhaw.praesto.exception.BadRequestException(messages.get("err.invalidEmail"));
        }
        if (password == null || password.length() < 8) {
            throw new ch.zhaw.praesto.exception.BadRequestException(messages.get("err.passwordMin8"));
        }
        if (userRepository.existsByEmail(e)) {
            throw new ch.zhaw.praesto.exception.BadRequestException(messages.get("err.emailExists"));
        }
        User admin = User.builder()
                .email(e)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName == null ? "" : firstName.trim())
                .lastName(lastName == null ? "" : lastName.trim())
                .role(UserRole.SUPER_ADMIN)
                .isActive(true)
                .createdAt(Instant.now())
                .build();
        User saved = userRepository.save(admin);
        log.info("Neuer Super-Admin angelegt: {}", saved.getEmail());
        return saved;
    }

    /** Kennzahlen zu Privat-/B2C-Konten (Registrierung -> Bezahlung). Nur Super-Admin. */
    public java.util.Map<String, Object> individualStats() {
        requireSuper();
        java.util.List<User> users = userRepository.findByAccountType(ch.zhaw.praesto.model.AccountType.INDIVIDUAL);
        java.time.Instant now = java.time.Instant.now();
        long total = users.size();
        long pending = 0;
        long trial = 0;
        long paying = 0;
        long expired = 0;
        for (User u : users) {
            if (u.needsParentConsent()) {
                pending++;
                continue;
            }
            boolean paid = u.getSubscriptionEndsAt() != null && now.isBefore(u.getSubscriptionEndsAt());
            boolean trialActive = u.getTrialEndsAt() != null && now.isBefore(u.getTrialEndsAt());
            if (paid) {
                paying++;
            } else if (trialActive) {
                trial++;
            } else {
                expired++;
            }
        }
        long confirmed = total - pending;   // Konten mit freigeschaltetem Zugang
        double conversion = confirmed > 0 ? Math.round(paying * 1000.0 / confirmed) / 10.0 : 0.0;
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("total", total);
        m.put("pendingConsent", pending);
        m.put("trial", trial);
        m.put("paying", paying);
        m.put("expired", expired);
        m.put("conversionRate", conversion);   // % zahlend von den freigeschalteten Konten
        return m;
    }

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

package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.UserRepository;
import ch.zhaw.praesto.service.AdminService;
import ch.zhaw.praesto.service.InviteService;
import ch.zhaw.praesto.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User-bezogene Endpoints. Listen geben {@link UserDTO} zurück (kein passwordHash).
 * Admin-/Super-Endpoints sind über {@link AdminService} rollen- und mandantengeschützt.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AdminService adminService;
    private final InviteService inviteService;
    private final UserRepository userRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final PasswordEncoder passwordEncoder;

    // ============================================================
    // Aktueller User
    // ============================================================

    @GetMapping("/users/me")
    public UserDTO me() {
        return UserDTO.from(userService.getCurrentUser());
    }

    /**
     * Eigenes Profil bearbeiten (Vor-/Nachname).
     */
    @PutMapping("/users/me")
    public UserDTO updateProfile(@RequestBody ProfileUpdateRequest req) {
        User user = userService.getCurrentUser();
        if (req.firstName() != null && !req.firstName().isBlank()) {
            user.setFirstName(req.firstName().trim());
        }
        if (req.lastName() != null && !req.lastName().isBlank()) {
            user.setLastName(req.lastName().trim());
        }
        return UserDTO.from(userRepository.save(user));
    }

    /**
     * Eigenes Passwort ändern (aktuelles Passwort wird geprüft).
     */
    @PutMapping("/users/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordChangeRequest req) {
        User user = userService.getCurrentUser();
        if (req.newPassword() == null || req.newPassword().length() < 8) {
            throw new BadRequestException("Passwort muss mindestens 8 Zeichen haben");
        }
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Aktuelles Passwort ist falsch");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Einzelnen User auflösen (z.B. Lehrer schaut Schüler-Session an). Nur innerhalb
     * der eigenen Schule (SUPER_ADMIN darf alle).
     */
    @GetMapping("/users/{id}")
    public UserDTO getUser(@PathVariable String id) {
        User current = userService.getCurrentUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User nicht gefunden"));
        if (current.getRole() != UserRole.SUPER_ADMIN
                && (current.getSchoolId() == null || !current.getSchoolId().equals(user.getSchoolId()))) {
            throw new ForbiddenException("Keine Berechtigung");
        }
        return UserDTO.from(user);
    }

    // ============================================================
    // TEACHER: Schüler-Suche (für Klassen-Zuordnung)
    // ============================================================

    @GetMapping("/users/students/search")
    public List<UserDTO> searchStudents(@RequestParam(required = false) String q) {
        return toDTOs(adminService.searchStudentsForTeacher(q));
    }

    // ============================================================
    // SCHOOL_ADMIN
    // ============================================================

    @GetMapping("/admin/users")
    public List<UserDTO> allSchoolUsers() {
        return toDTOs(adminService.listSchoolUsers());
    }

    @GetMapping("/admin/users/teachers")
    public List<UserDTO> teachers() {
        return toDTOs(adminService.listTeachers());
    }

    @GetMapping("/admin/users/students")
    public List<UserDTO> students(@RequestParam(required = false) String q) {
        return toDTOs(adminService.listStudents(q));
    }

    @PutMapping("/admin/users/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        adminService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Passwort eines Users zurücksetzen (für "Passwort vergessen"). Die Schulleitung
     * darf nur User der EIGENEN Schule zurücksetzen und keine Super-Admins; der
     * Super-Admin darf alle. Es wird ein neues Passwort gesetzt, das die Person danach
     * direkt zum Einloggen nutzt.
     */
    @PutMapping("/admin/users/{id}/reset-password")
    public ResponseEntity<Void> resetUserPassword(@PathVariable String id, @RequestBody Map<String, String> body) {
        User current = userService.getCurrentUser();
        boolean isSuper = current.getRole() == UserRole.SUPER_ADMIN;
        boolean isSchoolAdmin = current.getRole() == UserRole.SCHOOL_ADMIN;
        if (!isSuper && !isSchoolAdmin) {
            throw new ForbiddenException("Keine Berechtigung");
        }

        String newPassword = body == null ? null : body.get("newPassword");
        if (newPassword == null || newPassword.length() < 8) {
            throw new BadRequestException("Passwort muss mindestens 8 Zeichen haben");
        }

        User target = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User nicht gefunden"));

        if (!isSuper) {
            boolean sameSchool = current.getSchoolId() != null
                    && current.getSchoolId().equals(target.getSchoolId());
            if (!sameSchool || target.getRole() == UserRole.SUPER_ADMIN) {
                throw new ForbiddenException("Keine Berechtigung");
            }
        }

        target.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(target);
        return ResponseEntity.noContent().build();
    }

    /**
     * Passwort einer Schüler:in zurücksetzen – durch die LEHRPERSON. Erlaubt nur für
     * Schüler:innen, die in einer eigenen Klasse der Lehrperson sind (gleiche Schule,
     * Rolle STUDENT). Keine anderen Rollen.
     */
    @PutMapping("/teacher/students/{id}/reset-password")
    public ResponseEntity<Void> teacherResetStudentPassword(@PathVariable String id,
                                                            @RequestBody Map<String, String> body) {
        User current = userService.getCurrentUser();
        if (current.getRole() != UserRole.TEACHER) {
            throw new ForbiddenException("Nur Lehrpersonen können das.");
        }

        String newPassword = body == null ? null : body.get("newPassword");
        if (newPassword == null || newPassword.length() < 8) {
            throw new BadRequestException("Passwort muss mindestens 8 Zeichen haben");
        }

        User target = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Schüler:in nicht gefunden"));

        if (target.getRole() != UserRole.STUDENT
                || current.getSchoolId() == null
                || !current.getSchoolId().equals(target.getSchoolId())) {
            throw new ForbiddenException("Keine Berechtigung");
        }

        boolean inOwnClass = schoolClassRepository
                .findBySchoolIdAndTeacherId(current.getSchoolId(), current.getId())
                .stream()
                .anyMatch(c -> c.hasStudent(id));
        if (!inOwnClass) {
            throw new ForbiddenException("Diese Schüler:in ist in keiner deiner Klassen.");
        }

        target.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(target);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/stats")
    public SchoolStats schoolStats() {
        return adminService.schoolStats();
    }

    @GetMapping("/admin/insights")
    public SchoolInsights schoolInsights() {
        return adminService.schoolInsights();
    }

    // ============================================================
    // SUPER_ADMIN
    // ============================================================

    @GetMapping("/super/schools")
    public List<SchoolSummary> schools() {
        return adminService.listSchools();
    }

    @PostMapping("/super/schools")
    public ResponseEntity<School> createSchool(@RequestBody SchoolCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createSchool(req));
    }

    @PostMapping("/super/schools/{id}/invite")
    public InviteCreatedDTO createSchoolAdminInvite(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Integer> body) {
        Integer days = body != null ? body.get("expiresInDays") : null;
        return inviteService.createSchoolAdminInvite(id, days);
    }

    /** Schule sperren/entsperren (Zahlung). Daten bleiben; nur der Login wird blockiert. */
    @PutMapping("/super/schools/{id}/active")
    public ResponseEntity<Void> setSchoolActive(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        adminService.setSchoolActive(id, Boolean.TRUE.equals(body.get("active")));
        return ResponseEntity.noContent().build();
    }

    private List<UserDTO> toDTOs(List<User> users) {
        return users.stream().map(UserDTO::from).toList();
    }
}

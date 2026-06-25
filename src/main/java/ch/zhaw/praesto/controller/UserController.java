package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
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

    private List<UserDTO> toDTOs(List<User> users) {
        return users.stream().map(UserDTO::from).toList();
    }
}

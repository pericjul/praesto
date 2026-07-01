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
        setUserActiveAsSchoolAdmin(userId, false);
    }

    public void reactivateUser(String userId) {
        setUserActiveAsSchoolAdmin(userId, true);
    }

    private void setUserActiveAsSchoolAdmin(String userId, boolean active) {
        requireRole(UserRole.SCHOOL_ADMIN);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User nicht gefunden"));
        if (!userService.getCurrentSchoolId().equals(user.getSchoolId())) {
            throw new ForbiddenException("Keine Berechtigung");
        }
        user.setActive(active);
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

    /**
     * Gesamt-Statistik über alle Schulen + Kennzahlen pro Schule (inkl. jüngster Aktivität)
     * für das Super-Admin-Dashboard.
     */
    public SuperStats superStats() {
        requireRole(UserRole.SUPER_ADMIN);
        List<School> all = schoolRepository.findAll();

        List<SchoolStat> schoolStats = all.stream().map(s -> {
            String id = s.getId();
            List<User> users = userRepository.findBySchoolId(id);
            long studentCount = users.stream().filter(u -> u.getRole() == UserRole.STUDENT).count();
            long teacherCount = users.stream().filter(u -> u.getRole() == UserRole.TEACHER).count();
            Instant lastActivity = users.stream()
                    .map(User::getLastLoginAt)
                    .filter(java.util.Objects::nonNull)
                    .max(Instant::compareTo)
                    .orElse(null);
            return new SchoolStat(
                    id, s.getName(), s.getCity(), s.getCanton(), s.isActive(),
                    users.size(), studentCount, teacherCount,
                    sessionRepository.countBySchoolId(id),
                    submissionRepository.countBySchoolId(id),
                    lastActivity);
        }).sorted(Comparator.comparingLong(SchoolStat::sessionCount).reversed()).toList();

        long totalUsers = schoolStats.stream().mapToLong(SchoolStat::userCount).sum();
        long totalStudents = schoolStats.stream().mapToLong(SchoolStat::studentCount).sum();
        long totalTeachers = schoolStats.stream().mapToLong(SchoolStat::teacherCount).sum();
        long totalSessions = schoolStats.stream().mapToLong(SchoolStat::sessionCount).sum();
        long totalSubmissions = schoolStats.stream().mapToLong(SchoolStat::submissionCount).sum();
        long activeSchools = schoolStats.stream().filter(SchoolStat::active).count();

        return new SuperStats(all.size(), activeSchools, totalUsers, totalStudents,
                totalTeachers, totalSessions, totalSubmissions, schoolStats);
    }

    /** Alle Nutzer:innen aller Schulen mit Schulname + „zuletzt eingeloggt" (Super-Admin-Übersicht). */
    public List<SuperUserView> listAllUsersForSuper() {
        requireRole(UserRole.SUPER_ADMIN);
        Map<String, String> schoolNames = schoolRepository.findAll().stream()
                .collect(Collectors.toMap(School::getId, School::getName, (a, b) -> a));
        return userRepository.findAll().stream()
                .map(u -> new SuperUserView(
                        u.getId(),
                        ((safe(u.getFirstName()) + " " + safe(u.getLastName())).trim()),
                        u.getEmail(),
                        u.getRole() != null ? u.getRole().name() : null,
                        u.getSchoolId(),
                        u.getSchoolId() != null ? schoolNames.getOrDefault(u.getSchoolId(), "—") : "—",
                        u.isActive(),
                        u.getLastLoginAt(),
                        u.getCreatedAt()))
                .sorted(Comparator
                        .comparing((SuperUserView v) -> v.schoolName() == null ? "" : v.schoolName())
                        .thenComparing(v -> v.name() == null ? "" : v.name()))
                .toList();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static final java.time.format.DateTimeFormatter CSV_DATE =
            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                    .withZone(java.time.ZoneId.of("Europe/Zurich"));

    /**
     * Vollständiger CSV-Export einer Schule: alle Nutzer:innen mit Rolle, Kontakt,
     * Status, Login-/Erstellungsdatum, Klasse(n) und (bei Schüler:innen) Nutzungszahlen.
     * Semikolon-getrennt + UTF-8-BOM, damit Excel (CH) Umlaute korrekt öffnet.
     */
    public String exportSchoolCsv(String schoolId) {
        requireRole(UserRole.SUPER_ADMIN);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new NotFoundException("Schule nicht gefunden"));

        // studentId -> Klassennamen
        Map<String, java.util.List<String>> classesByStudent = new java.util.HashMap<>();
        for (SchoolClass c : schoolClassRepository.findBySchoolId(schoolId)) {
            if (c.getStudentIds() == null) continue;
            for (String sid : c.getStudentIds()) {
                classesByStudent.computeIfAbsent(sid, k -> new java.util.ArrayList<>()).add(safe(c.getName()));
            }
        }

        java.util.List<User> users = userRepository.findBySchoolId(schoolId).stream()
                .sorted(Comparator
                        .comparing((User u) -> u.getRole() != null ? u.getRole().name() : "")
                        .thenComparing(u -> safe(u.getLastName())))
                .toList();

        StringBuilder sb = new StringBuilder("﻿");
        sb.append(String.join(";", "Rolle", "Vorname", "Nachname", "E-Mail", "Aktiv",
                "Zuletzt eingeloggt", "Erstellt am", "Klasse(n)", "Übungsgespräche", "Abgaben")).append("\r\n");

        for (User u : users) {
            boolean student = u.getRole() == UserRole.STUDENT;
            String klassen = String.join(", ", classesByStudent.getOrDefault(u.getId(), java.util.List.of()));
            long gespraeche = student ? sessionRepository.findByStudentId(u.getId()).size() : 0;
            long abgaben = student ? submissionRepository.countByStudentEmail(u.getEmail()) : 0;
            sb.append(String.join(";",
                    csv(u.getRole() != null ? u.getRole().name() : ""),
                    csv(safe(u.getFirstName())),
                    csv(safe(u.getLastName())),
                    csv(u.getEmail()),
                    csv(u.isActive() ? "ja" : "nein"),
                    csv(u.getLastLoginAt() != null ? CSV_DATE.format(u.getLastLoginAt()) : "nie"),
                    csv(u.getCreatedAt() != null ? CSV_DATE.format(u.getCreatedAt()) : ""),
                    csv(klassen),
                    csv(student ? String.valueOf(gespraeche) : ""),
                    csv(student ? String.valueOf(abgaben) : "")
            )).append("\r\n");
        }
        return sb.toString();
    }

    /** Name der Schule (für den Dateinamen des CSV-Exports). */
    public String schoolName(String schoolId) {
        requireRole(UserRole.SUPER_ADMIN);
        return schoolRepository.findById(schoolId).map(School::getName).orElse("schule");
    }

    /** CSV-Feld escapen: Anführungszeichen verdoppeln, in Quotes setzen, Zeilenumbrüche entfernen. */
    private static String csv(String s) {
        if (s == null) return "";
        String clean = s.replace("\r", " ").replace("\n", " ");
        return "\"" + clean.replace("\"", "\"\"") + "\"";
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

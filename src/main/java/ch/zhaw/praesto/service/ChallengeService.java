package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.ClassChallengeRepository;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Gemeinsame Klassen-Challenge (1-Klick): erstellen, beenden, Fortschritt berechnen
 * (Summe der Übungsgespräche der Klasse seit Start) – für Lehrer und Schüler.
 */
@Service
@RequiredArgsConstructor
public class ChallengeService {

    private static final int PER_STUDENT_GOAL = 5;
    private static final int MIN_GOAL = 10;

    private final ClassChallengeRepository challengeRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SessionRepository sessionRepository;
    private final SchoolClassService schoolClassService;
    private final UserService userService;

    // ============================================================
    // Lehrer
    // ============================================================

    public ClassChallengeDTO startChallenge(String classId, Integer target, String title) {
        SchoolClass schoolClass = requireOwnClass(classId);

        // bestehende aktive Challenge ablösen
        challengeRepository.findByClassIdAndActiveTrue(classId).ifPresent(existing -> {
            existing.setActive(false);
            challengeRepository.save(existing);
        });

        int students = schoolClass.getStudentIds().size();
        int resolvedTarget = (target != null && target > 0)
                ? target
                : Math.max(MIN_GOAL, students * PER_STUDENT_GOAL);

        ClassChallenge challenge = ClassChallenge.builder()
                .schoolId(schoolClass.getSchoolId())
                .classId(classId)
                .title(title != null && !title.isBlank() ? title.trim() : null)
                .target(resolvedTarget)
                .createdByTeacherId(userService.getCurrentUserId())
                .createdAt(Instant.now())
                .active(true)
                .build();
        challengeRepository.save(challenge);
        return toDTO(challenge, schoolClass);
    }

    public ClassChallengeDTO getForClass(String classId) {
        SchoolClass schoolClass = requireOwnClass(classId);
        return challengeRepository.findByClassIdAndActiveTrue(classId)
                .map(c -> toDTO(c, schoolClass))
                .orElse(null);
    }

    public void endChallenge(String classId) {
        requireOwnClass(classId);
        challengeRepository.findByClassIdAndActiveTrue(classId).ifPresent(c -> {
            c.setActive(false);
            challengeRepository.save(c);
        });
    }

    // ============================================================
    // Schüler
    // ============================================================

    public ClassChallengeDTO getForCurrentStudent() {
        String classId = schoolClassService.getMyClassId();
        if (classId == null || classId.isBlank()) {
            return null;
        }
        SchoolClass schoolClass = schoolClassRepository.findById(classId).orElse(null);
        if (schoolClass == null) {
            return null;
        }
        return challengeRepository.findByClassIdAndActiveTrue(classId)
                .map(c -> toDTO(c, schoolClass))
                .orElse(null);
    }

    // ============================================================
    // Intern
    // ============================================================

    private int computeProgress(ClassChallenge challenge, SchoolClass schoolClass) {
        int total = 0;
        for (String studentId : schoolClass.getStudentIds()) {
            total += (int) sessionRepository.findByStudentId(studentId).stream()
                    .map(Session::getStartedAt)
                    .filter(java.util.Objects::nonNull)
                    .filter(start -> !start.isBefore(challenge.getCreatedAt()))
                    .count();
        }
        return total;
    }

    private ClassChallengeDTO toDTO(ClassChallenge c, SchoolClass schoolClass) {
        int current = computeProgress(c, schoolClass);
        int percent = c.getTarget() > 0
                ? Math.min(100, (int) Math.round(100.0 * current / c.getTarget()))
                : 0;
        return new ClassChallengeDTO(c.getId(), c.getTitle(), c.getTarget(), current, percent, c.isActive());
    }

    private SchoolClass requireOwnClass(String classId) {
        SchoolClass schoolClass = schoolClassRepository
                .findByIdAndSchoolId(classId, userService.getCurrentSchoolId())
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));
        boolean owner = userService.getCurrentUserId().equals(schoolClass.getTeacherId());
        boolean admin = userService.userHasRole(UserRole.SCHOOL_ADMIN)
                || userService.userHasRole(UserRole.SUPER_ADMIN);
        if (!owner && !admin) {
            throw new ForbiddenException("Keine Berechtigung für diese Klasse");
        }
        return schoolClass;
    }
}

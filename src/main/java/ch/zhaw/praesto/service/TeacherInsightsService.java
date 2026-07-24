package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.AiUsageRepository;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.SessionRepository;
import ch.zhaw.praesto.repository.SubmissionRepository;
import ch.zhaw.praesto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Aggregiert pro Klasse die Reife-/Score-Daten der Schüler:innen für das
 * Lehrer-Cockpit und leitet daraus einen Gesprächsleitfaden ab.
 */
@Service
@RequiredArgsConstructor
public class TeacherInsightsService {

    private static final int LOW_SCORE_THRESHOLD = 30;
    private static final long INACTIVE_DAYS = 14;
    private static final int BARELY_MAX_PRACTICES = 1;   // 0-1 Gespräche = "kaum geübt"
    private static final int DECLINE_DROP = 10;          // Punkte-Abfall = "Verschlechterung"

    private final SchoolClassRepository schoolClassRepository;
    private final SessionRepository sessionRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final AiUsageRepository aiUsageRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserService userService;

    public ClassCockpitDTO getClassCockpit(String classId) {
        SchoolClass schoolClass = schoolClassRepository
                .findByIdAndSchoolId(classId, userService.getCurrentSchoolId())
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));

        boolean isOwner = userService.getCurrentUserId().equals(schoolClass.getTeacherId());
        boolean isAdmin = userService.userHasRole(UserRole.SCHOOL_ADMIN)
                || userService.userHasRole(UserRole.SUPER_ADMIN);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("Keine Berechtigung für diese Klasse");
        }

        Instant now = Instant.now();
        Instant inactiveCutoff = now.minus(INACTIVE_DAYS, ChronoUnit.DAYS);

        // Überfällige Aufgaben dieser Klasse (Fälligkeit vorbei) – für "nicht abgegeben".
        List<Assignment> overdueAssignments = assignmentRepository.findByClassId(classId).stream()
                .filter(a -> a.getDueDate() != null && a.getDueDate().isBefore(now))
                .toList();

        List<ClassCockpitDTO.Student> students = new ArrayList<>();
        List<ClassCockpitDTO.Guidance> guidance = new ArrayList<>();
        int practicedCount = 0;
        int scoreSum = 0;
        int scoreCount = 0;

        for (String studentId : schoolClass.getStudentIds()) {
            User student = userRepository.findById(studentId).orElse(null);
            if (student == null) {
                continue;
            }
            List<Session> sessions = sessionRepository.findByStudentId(studentId);

            int sessionCount = sessions.size();
            int closedCount = (int) sessions.stream()
                    .filter(s -> s.getStatus() == SessionStatus.CLOSED)
                    .count();
            List<Integer> scores = sessions.stream()
                    .map(Session::getScore)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            Integer bestScore = scores.stream().max(Comparator.naturalOrder()).orElse(null);
            Integer avgScore = scores.isEmpty() ? null
                    : (int) Math.round(scores.stream().mapToInt(Integer::intValue).average().orElse(0));
            Instant lastActivity = sessions.stream()
                    .map(Session::getStartedAt)
                    .filter(java.util.Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            long submissionCount = submissionRepository.countByStudentEmail(student.getEmail());

            // Persistentes "hat geübt"-Signal: das KI-Kontingent (AiUsage) zählt jedes freie
            // Übungsgespräch kumulativ und sinkt NICHT, wenn der Schüler seine Chats löscht.
            // So gilt jemand, der geübt und danach gelöscht hat, weiterhin als "geübt".
            int practiceUsed = aiUsageRepository
                    .findByUserIdAndFeature(studentId, AiFeature.PRACTICE_INTERVIEW)
                    .map(AiUsage::getUsedTotal)
                    .orElse(0);
            boolean everPracticed = sessionCount > 0 || practiceUsed > 0;

            if (everPracticed) {
                practicedCount++;
            }
            if (avgScore != null) {
                scoreSum += avgScore;
                scoreCount++;
            }

            // Scores in zeitlicher Reihenfolge (für "Verschlechterung")
            List<Integer> scoresChrono = sessions.stream()
                    .filter(s -> s.getScore() != null)
                    .sorted(Comparator.comparing(
                            s -> s.getClosedAt() != null ? s.getClosedAt() : s.getStartedAt(),
                            Comparator.nullsFirst(Comparator.naturalOrder())))
                    .map(Session::getScore)
                    .toList();
            boolean declining = false;
            if (scoresChrono.size() >= 2) {
                List<Integer> earlier = scoresChrono.subList(0, scoresChrono.size() - 1);
                int earlierAvg = (int) Math.round(earlier.stream().mapToInt(Integer::intValue).average().orElse(0));
                int latest = scoresChrono.get(scoresChrono.size() - 1);
                declining = latest <= earlierAvg - DECLINE_DROP;
            }

            // Überfällige Aufgaben, die diese:r Schüler:in NICHT abgegeben hat
            long missingAssignments = overdueAssignments.stream()
                    .filter(a -> !submissionRepository.existsByAssignmentIdAndStudentEmail(a.getId(), student.getEmail()))
                    .count();

            int practiceEngagement = Math.max(sessionCount, practiceUsed);

            List<String> reasons = new ArrayList<>();
            if (!everPracticed) {
                reasons.add("NEVER_PRACTICED");
            } else {
                if (practiceEngagement <= BARELY_MAX_PRACTICES) {
                    reasons.add("BARELY_PRACTICED");
                }
                if (bestScore != null && bestScore < LOW_SCORE_THRESHOLD) {
                    reasons.add("LOW_SCORE");
                }
                if (declining) {
                    reasons.add("DECLINING");
                }
                if (lastActivity != null && lastActivity.isBefore(inactiveCutoff)) {
                    reasons.add("INACTIVE");
                }
            }
            if (missingAssignments > 0) {
                reasons.add("MISSING_ASSIGNMENT");
            }
            boolean needsAttention = !reasons.isEmpty();

            students.add(new ClassCockpitDTO.Student(
                    studentId, student.getFullName(), sessionCount, closedCount,
                    bestScore, avgScore, lastActivity, submissionCount, needsAttention, reasons));

            if (needsAttention) {
                guidance.add(new ClassCockpitDTO.Guidance(
                        studentId, student.getFullName(), reasons.get(0)));
            }
        }

        // Sortierung: zuerst, wer Aufmerksamkeit braucht; sonst nach bestem Score absteigend
        students.sort(Comparator
                .comparing(ClassCockpitDTO.Student::needsAttention).reversed()
                .thenComparing(s -> s.bestScore() == null ? -1 : s.bestScore(), Comparator.reverseOrder()));

        Integer avgClassScore = scoreCount == 0 ? null : Math.round((float) scoreSum / scoreCount);

        return new ClassCockpitDTO(
                classId,
                schoolClass.getName(),
                schoolClass.getStudentIds().size(),
                practicedCount,
                avgClassScore,
                (int) students.stream().filter(ClassCockpitDTO.Student::needsAttention).count(),
                students,
                guidance);
    }
}

package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final SessionRepository sessionRepository;
    private final NoteRepository noteRepository;
    private final ApplicationRepository applicationRepository;
    private final SubmissionRepository submissionRepository;  // NEU: für Submission-basierte Badges
    private final UserService userService;  // NEU: für Email-Lookup

    /**
     * Alle verfügbaren Badges abrufen
     */
    public List<Badge> getAllBadges() {
        return badgeRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * Verdiente Badges eines Users abrufen
     */
    public List<UserBadge> getEarnedBadges(String studentId) {
        return userBadgeRepository.findByStudentIdOrderByEarnedAtDesc(studentId);
    }

    /**
     * Anzahl verdienter Badges eines Users
     */
    public long getEarnedBadgeCount(String studentId) {
        return userBadgeRepository.countByStudentId(studentId);
    }

    /**
     * Prüft alle Badge-Regeln und vergibt neue Badges
     * Gibt Liste der neu verdienten Badges zurück
     */
    public List<Badge> checkAndAwardBadges(String studentId) {
        List<Badge> newlyEarnedBadges = new ArrayList<>();
        
        // Bereits verdiente Badge-IDs
        Set<String> earnedBadgeIds = userBadgeRepository
                .findByStudentIdOrderByEarnedAtDesc(studentId)
                .stream()
                .map(UserBadge::getBadgeId)
                .collect(Collectors.toSet());

        // Alle Badges prüfen
        List<Badge> allBadges = badgeRepository.findAllByOrderBySortOrderAsc();
        
        // Email für Submission-basierte Prüfungen (aktueller User)
        String studentEmail = userService.getEmail().toLowerCase();
        
        for (Badge badge : allBadges) {
            // Bereits verdient? Skip
            if (earnedBadgeIds.contains(badge.getId())) {
                continue;
            }
            
            // Regel prüfen
            boolean earned = checkBadgeRule(studentId, studentEmail, badge);
            
            if (earned) {
                // Badge vergeben
                UserBadge userBadge = UserBadge.builder()
                        .studentId(studentId)
                        .badgeId(badge.getId())
                        .earnedAt(Instant.now())
                        .build();
                
                userBadgeRepository.save(userBadge);
                newlyEarnedBadges.add(badge);
                
                log.info("Badge '{}' an User {} vergeben", badge.getTitle(), studentId);
            }
        }
        
        return newlyEarnedBadges;
    }

    /**
     * Prüft ob eine Badge-Regel erfüllt ist
     */
    private boolean checkBadgeRule(String studentId, String studentEmail, Badge badge) {
        switch (badge.getRuleType()) {
            case SESSIONS_COMPLETED:
                long sessionCount = sessionRepository.countByStudentIdAndStatus(studentId, SessionStatus.CLOSED);
                return sessionCount >= badge.getThreshold();
                
            case NOTES_CREATED:
                long noteCount = noteRepository.countByStudentId(studentId);
                return noteCount >= badge.getThreshold();
                
            case APPLICATIONS_CREATED:
                long appCount = applicationRepository.countByStudentId(studentId);
                return appCount >= badge.getThreshold();
                
            case APPLICATION_STATUS:
                // Threshold entspricht dem Ordinal des ApplicationStatus
                ApplicationStatus requiredStatus = getStatusFromThreshold(badge.getThreshold());
                if (requiredStatus != null) {
                    return applicationRepository.existsByStudentIdAndStatus(studentId, requiredStatus);
                }
                return false;
                
            case SUBMISSIONS_COMPLETED:
                // Anzahl abgegebene Aufgaben
                long submissionCount = submissionRepository.countByStudentEmail(studentEmail);
                return submissionCount >= badge.getThreshold();
                
            case FEEDBACK_RECEIVED:
                // Anzahl Submissions mit Feedback
                long feedbackCount = submissionRepository.countByStudentEmailAndTeacherFeedbackIsNotNull(studentEmail);
                return feedbackCount >= badge.getThreshold();
                
            case GRADES_RECEIVED:
                // Anzahl Submissions mit Note
                long gradeCount = submissionRepository.countByStudentEmailAndGradeIsNotNull(studentEmail);
                return gradeCount >= badge.getThreshold();
                
            default:
                log.warn("Unbekannter Badge-RuleType: {}", badge.getRuleType());
                return false;
        }
    }

    /**
     * Mappt Threshold auf ApplicationStatus
     * 0 = APPLIED, 1 = INVITED, 2 = INTERVIEW_DONE, 3 = ACCEPTED
     */
    private ApplicationStatus getStatusFromThreshold(int threshold) {
        switch (threshold) {
            case 0:
                return ApplicationStatus.APPLIED;
            case 1:
                return ApplicationStatus.INVITED;
            case 2:
                return ApplicationStatus.INTERVIEW_DONE;
            case 3:
                return ApplicationStatus.ACCEPTED;
            default:
                return null;
        }
    }

    /**
     * Holt Badge-Details zu einer Liste von UserBadges
     */
    public List<BadgeWithEarnedInfo> getBadgesWithEarnedInfo(String studentId) {
        List<Badge> allBadges = getAllBadges();
        List<UserBadge> earnedBadges = getEarnedBadges(studentId);
        
        // Map für schnellen Lookup
        var earnedMap = earnedBadges.stream()
                .collect(Collectors.toMap(UserBadge::getBadgeId, ub -> ub));
        
        return allBadges.stream()
                .map(badge -> {
                    UserBadge earned = earnedMap.get(badge.getId());
                    return BadgeWithEarnedInfo.builder()
                            .badge(badge)
                            .earned(earned != null)
                            .earnedAt(earned != null ? earned.getEarnedAt() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Holt die Icons aller verdienten Badges (für Dashboard)
     */
    public List<String> getEarnedBadgeIcons(String studentId) {
        return getBadgesWithEarnedInfo(studentId).stream()
                .filter(BadgeWithEarnedInfo::isEarned)
                .map(b -> b.getBadge().getIcon())
                .collect(Collectors.toList());
    }

    /**
     * DTO für Badge mit Earned-Info
     */
    @lombok.Builder
    @lombok.Getter
    public static class BadgeWithEarnedInfo {
        private Badge badge;
        private boolean earned;
        private Instant earnedAt;
    }
}
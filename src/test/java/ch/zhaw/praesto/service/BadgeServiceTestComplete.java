package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTestComplete {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BadgeService badgeService;

    private Badge sessionBadge;
    private Badge noteBadge;
    private Badge appBadge;
    private Badge statusBadge;
    private Badge submissionBadge;
    private Badge feedbackBadge;
    private Badge gradeBadge;

    @BeforeEach
    void setUp() {
        sessionBadge = Badge.builder()
                .id("badge-sessions")
                .title("Session Master")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(5)
                .icon("🎯")
                .build();

        noteBadge = Badge.builder()
                .id("badge-notes")
                .title("Notizen Profi")
                .ruleType(BadgeRuleType.NOTES_CREATED)
                .threshold(10)
                .icon("📝")
                .build();

        appBadge = Badge.builder()
                .id("badge-apps")
                .title("Bewerber")
                .ruleType(BadgeRuleType.APPLICATIONS_CREATED)
                .threshold(3)
                .icon("📋")
                .build();

        statusBadge = Badge.builder()
                .id("badge-status")
                .title("Eingeladen")
                .ruleType(BadgeRuleType.APPLICATION_STATUS)
                .threshold(1) // INVITED
                .icon("✉️")
                .build();

        submissionBadge = Badge.builder()
                .id("badge-submissions")
                .title("Abgabe König")
                .ruleType(BadgeRuleType.SUBMISSIONS_COMPLETED)
                .threshold(5)
                .icon("📤")
                .build();

        feedbackBadge = Badge.builder()
                .id("badge-feedback")
                .title("Feedback Sammler")
                .ruleType(BadgeRuleType.FEEDBACK_RECEIVED)
                .threshold(3)
                .icon("💬")
                .build();

        gradeBadge = Badge.builder()
                .id("badge-grades")
                .title("Noten Sammler")
                .ruleType(BadgeRuleType.GRADES_RECEIVED)
                .threshold(2)
                .icon("⭐")
                .build();
    }

    // ========================================
    // getAllBadges
    // ========================================
    @Nested
    @DisplayName("getAllBadges")
    class GetAllBadges {

        @Test
        @DisplayName("Alle Badges abrufen")
        void getAllBadges_returnsBadges() {
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(sessionBadge, noteBadge));

            List<Badge> result = badgeService.getAllBadges();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Keine Badges")
        void getAllBadges_empty() {
            when(badgeRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of());

            List<Badge> result = badgeService.getAllBadges();

            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // getEarnedBadges
    // ========================================
    @Nested
    @DisplayName("getEarnedBadges")
    class GetEarnedBadges {

        @Test
        @DisplayName("Verdiente Badges abrufen")
        void getEarnedBadges_returnsList() {
            UserBadge userBadge = UserBadge.builder()
                    .studentId("student-123")
                    .badgeId("badge-sessions")
                    .earnedAt(Instant.now())
                    .build();

            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(List.of(userBadge));

            List<UserBadge> result = badgeService.getEarnedBadges("student-123");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Keine verdienten Badges")
        void getEarnedBadges_empty() {
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(List.of());

            List<UserBadge> result = badgeService.getEarnedBadges("student-123");

            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // getEarnedBadgeCount
    // ========================================
    @Nested
    @DisplayName("getEarnedBadgeCount")
    class GetEarnedBadgeCount {

        @Test
        @DisplayName("Anzahl verdienter Badges")
        void getEarnedBadgeCount_returnsCount() {
            when(userBadgeRepository.countByStudentId("student-123")).thenReturn(5L);

            long result = badgeService.getEarnedBadgeCount("student-123");

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Keine Badges - 0")
        void getEarnedBadgeCount_zero() {
            when(userBadgeRepository.countByStudentId("student-123")).thenReturn(0L);

            long result = badgeService.getEarnedBadgeCount("student-123");

            assertThat(result).isEqualTo(0);
        }
    }

    // ========================================
    // checkAndAwardBadges - SESSIONS_COMPLETED
    // ========================================
    @Nested
    @DisplayName("checkAndAwardBadges - Sessions")
    class CheckAndAwardBadgesSessions {

        @Test
        @DisplayName("Badge verdient - genug Sessions")
        void checkAndAward_enoughSessions_awardsBadge() {
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(sessionBadge));
            when(sessionRepository.countByStudentIdAndStatus("student-123", SessionStatus.CLOSED))
                    .thenReturn(5L);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Session Master");
            verify(userBadgeRepository).save(any(UserBadge.class));
        }

        @Test
        @DisplayName("Badge nicht verdient - nicht genug Sessions")
        void checkAndAward_notEnoughSessions_noBadge() {
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(sessionBadge));
            when(sessionRepository.countByStudentIdAndStatus("student-123", SessionStatus.CLOSED))
                    .thenReturn(3L); // threshold ist 5

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).isEmpty();
            verify(userBadgeRepository, never()).save(any(UserBadge.class));
        }

        @Test
        @DisplayName("Badge bereits verdient - kein erneutes Vergeben")
        void checkAndAward_alreadyEarned_noDuplicate() {
            UserBadge existingBadge = UserBadge.builder()
                    .badgeId("badge-sessions")
                    .studentId("student-123")
                    .build();

            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(List.of(existingBadge));
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(sessionBadge));

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).isEmpty();
            verify(userBadgeRepository, never()).save(any(UserBadge.class));
        }
    }

    // ========================================
    // checkAndAwardBadges - NOTES_CREATED
    // ========================================
    @Nested
    @DisplayName("checkAndAwardBadges - Notes")
    class CheckAndAwardBadgesNotes {

        @Test
        @DisplayName("Badge verdient - genug Notizen")
        void checkAndAward_enoughNotes_awardsBadge() {
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(noteBadge));
            when(noteRepository.countByStudentId("student-123")).thenReturn(10L);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Notizen Profi");
        }

        @Test
        @DisplayName("Badge nicht verdient - nicht genug Notizen")
        void checkAndAward_notEnoughNotes_noBadge() {
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(noteBadge));
            when(noteRepository.countByStudentId("student-123")).thenReturn(5L);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // checkAndAwardBadges - APPLICATIONS_CREATED
    // ========================================
    @Nested
    @DisplayName("checkAndAwardBadges - Applications")
    class CheckAndAwardBadgesApplications {

        @Test
        @DisplayName("Badge verdient - genug Bewerbungen")
        void checkAndAward_enoughApps_awardsBadge() {
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(appBadge));
            when(applicationRepository.countByStudentId("student-123")).thenReturn(3L);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Badge nicht verdient - nicht genug Bewerbungen")
        void checkAndAward_notEnoughApps_noBadge() {
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(appBadge));
            when(applicationRepository.countByStudentId("student-123")).thenReturn(2L);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // checkAndAwardBadges - APPLICATION_STATUS
    // ========================================
    @Nested
    @DisplayName("checkAndAwardBadges - ApplicationStatus")
    class CheckAndAwardBadgesStatus {

        @ParameterizedTest
        @CsvSource({
            "0, APPLIED",
            "1, INVITED",
            "2, INTERVIEW_DONE",
            "3, ACCEPTED"
        })
        @DisplayName("Badge verdient - passender Status")
        void checkAndAward_correctStatus_awardsBadge(int threshold, String statusName) {
            ApplicationStatus status = ApplicationStatus.valueOf(statusName);
            Badge badge = Badge.builder()
                    .id("badge-status-" + threshold)
                    .title("Status Badge")
                    .ruleType(BadgeRuleType.APPLICATION_STATUS)
                    .threshold(threshold)
                    .icon("📋")
                    .build();

            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(badge));
            when(applicationRepository.existsByStudentIdAndStatus("student-123", status))
                    .thenReturn(true);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Badge nicht verdient - Status nicht erreicht")
        void checkAndAward_statusNotReached_noBadge() {
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(statusBadge));
            when(applicationRepository.existsByStudentIdAndStatus("student-123", ApplicationStatus.INVITED))
                    .thenReturn(false);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Ungültiger Threshold - kein Badge")
        void checkAndAward_invalidThreshold_noBadge() {
            Badge invalidBadge = Badge.builder()
                    .id("badge-invalid")
                    .title("Invalid")
                    .ruleType(BadgeRuleType.APPLICATION_STATUS)
                    .threshold(99) // ungültig
                    .icon("❓")
                    .build();

            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(invalidBadge));

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // checkAndAwardBadges - SUBMISSIONS_COMPLETED
    // ========================================
    @Nested
    @DisplayName("checkAndAwardBadges - Submissions")
    class CheckAndAwardBadgesSubmissions {

        @Test
        @DisplayName("Badge verdient - genug Abgaben")
        void checkAndAward_enoughSubmissions_awardsBadge() {
            when(userService.getEmail()).thenReturn("Student@Test.CH");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(submissionBadge));
            when(submissionRepository.countByStudentEmail("student@test.ch")).thenReturn(5L);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Badge nicht verdient - nicht genug Abgaben")
        void checkAndAward_notEnoughSubmissions_noBadge() {
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(submissionBadge));
            when(submissionRepository.countByStudentEmail("student@test.ch")).thenReturn(2L);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // checkAndAwardBadges - FEEDBACK_RECEIVED
    // ========================================
    @Nested
    @DisplayName("checkAndAwardBadges - Feedback")
    class CheckAndAwardBadgesFeedback {

        @Test
        @DisplayName("Badge verdient - genug Feedback")
        void checkAndAward_enoughFeedback_awardsBadge() {
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(feedbackBadge));
            when(submissionRepository.countByStudentEmailAndTeacherFeedbackIsNotNull("student@test.ch"))
                    .thenReturn(3L);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Badge nicht verdient - nicht genug Feedback")
        void checkAndAward_notEnoughFeedback_noBadge() {
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(feedbackBadge));
            when(submissionRepository.countByStudentEmailAndTeacherFeedbackIsNotNull("student@test.ch"))
                    .thenReturn(1L);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // checkAndAwardBadges - GRADES_RECEIVED
    // ========================================
    @Nested
    @DisplayName("checkAndAwardBadges - Grades")
    class CheckAndAwardBadgesGrades {

        @Test
        @DisplayName("Badge verdient - genug Noten")
        void checkAndAward_enoughGrades_awardsBadge() {
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(gradeBadge));
            when(submissionRepository.countByStudentEmailAndGradeIsNotNull("student@test.ch"))
                    .thenReturn(2L);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Badge nicht verdient - nicht genug Noten")
        void checkAndAward_notEnoughGrades_noBadge() {
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(new ArrayList<>());
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(gradeBadge));
            when(submissionRepository.countByStudentEmailAndGradeIsNotNull("student@test.ch"))
                    .thenReturn(0L);

            List<Badge> result = badgeService.checkAndAwardBadges("student-123");

            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // getBadgesWithEarnedInfo
    // ========================================
    @Nested
    @DisplayName("getBadgesWithEarnedInfo")
    class GetBadgesWithEarnedInfo {

        @Test
        @DisplayName("Badges mit Earned-Info - alle verdient")
        void getBadgesWithEarnedInfo_allEarned() {
            UserBadge earned1 = UserBadge.builder()
                    .badgeId("badge-sessions")
                    .studentId("student-123")
                    .earnedAt(Instant.now())
                    .build();
            UserBadge earned2 = UserBadge.builder()
                    .badgeId("badge-notes")
                    .studentId("student-123")
                    .earnedAt(Instant.now())
                    .build();

            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(sessionBadge, noteBadge));
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(List.of(earned1, earned2));

            List<BadgeService.BadgeWithEarnedInfo> result = 
                    badgeService.getBadgesWithEarnedInfo("student-123");

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(BadgeService.BadgeWithEarnedInfo::isEarned);
        }

        @Test
        @DisplayName("Badges mit Earned-Info - keine verdient")
        void getBadgesWithEarnedInfo_noneEarned() {
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(sessionBadge, noteBadge));
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(List.of());

            List<BadgeService.BadgeWithEarnedInfo> result = 
                    badgeService.getBadgesWithEarnedInfo("student-123");

            assertThat(result).hasSize(2);
            assertThat(result).noneMatch(BadgeService.BadgeWithEarnedInfo::isEarned);
        }

        @Test
        @DisplayName("Badges mit Earned-Info - teils verdient")
        void getBadgesWithEarnedInfo_partiallyEarned() {
            UserBadge earned = UserBadge.builder()
                    .badgeId("badge-sessions")
                    .studentId("student-123")
                    .earnedAt(Instant.now())
                    .build();

            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(sessionBadge, noteBadge));
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(List.of(earned));

            List<BadgeService.BadgeWithEarnedInfo> result = 
                    badgeService.getBadgesWithEarnedInfo("student-123");

            assertThat(result).hasSize(2);
            long earnedCount = result.stream().filter(BadgeService.BadgeWithEarnedInfo::isEarned).count();
            assertThat(earnedCount).isEqualTo(1);
        }
    }

    // ========================================
    // getEarnedBadgeIcons
    // ========================================
    @Nested
    @DisplayName("getEarnedBadgeIcons")
    class GetEarnedBadgeIcons {

        @Test
        @DisplayName("Verdiente Badge-Icons")
        void getEarnedBadgeIcons_returnsIcons() {
            UserBadge earned = UserBadge.builder()
                    .badgeId("badge-sessions")
                    .studentId("student-123")
                    .earnedAt(Instant.now())
                    .build();

            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(sessionBadge, noteBadge));
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(List.of(earned));

            List<String> result = badgeService.getEarnedBadgeIcons("student-123");

            assertThat(result).containsExactly("🎯");
        }

        @Test
        @DisplayName("Keine verdienten Badges - leere Liste")
        void getEarnedBadgeIcons_noneEarned_emptyList() {
            when(badgeRepository.findAllByOrderBySortOrderAsc())
                    .thenReturn(List.of(sessionBadge));
            when(userBadgeRepository.findByStudentIdOrderByEarnedAtDesc("student-123"))
                    .thenReturn(List.of());

            List<String> result = badgeService.getEarnedBadgeIcons("student-123");

            assertThat(result).isEmpty();
        }
    }
}

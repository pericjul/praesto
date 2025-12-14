package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentDashboardServiceExtendedTest {

    @Mock
    private UserService userService;

    @Mock
    private SchoolClassService schoolClassService;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private BadgeService badgeService;

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private StudentDashboardService studentDashboardService;

    @BeforeEach
    void setUpBaseMocks() {
        // Basis-Mocks die in jedem Test benötigt werden
        when(userService.getUserId()).thenReturn("student-123");
        when(userService.getUserName()).thenReturn("Max Mustermann");
        when(userService.getEmail()).thenReturn("student@test.ch");
        when(schoolClassService.getMyClassId()).thenReturn("class-123");
        when(badgeService.getEarnedBadgeCount("student-123")).thenReturn(0L);
        when(badgeService.getBadgesWithEarnedInfo("student-123")).thenReturn(new ArrayList<>());
    }

    // ========================================
    // Notifications Tests
    // ========================================
    @Nested
    @DisplayName("Notifications")
    class NotificationsTests {

        @Test
        @DisplayName("Feedback-Notification anzeigen")
        void getDashboard_withRecentFeedback_showsNotification() {
            Assignment assignment = Assignment.builder()
                    .id("assign-123")
                    .title("Bewerbungstraining")
                    .status(AssignmentStatus.ASSIGNED)
                    .classId("class-123")
                    .build();

            Submission submissionWithFeedback = Submission.builder()
                    .assignmentId("assign-123")
                    .studentEmail("student@test.ch")
                    .teacherFeedback("Gut gemacht!")
                    .submittedAt(Instant.now().minusSeconds(3600)) // vor 1 Stunde
                    .build();

            when(assignmentRepository.findByClassId("class-123"))
                    .thenReturn(new ArrayList<>(List.of(assignment)));
            when(submissionRepository.findByStudentEmail("student@test.ch"))
                    .thenReturn(new ArrayList<>(List.of(submissionWithFeedback)));
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getNotifications()).hasSize(1);
            assertThat(result.getNotifications().get(0).getType()).isEqualTo("FEEDBACK");
            assertThat(result.getNotifications().get(0).getIcon()).isEqualTo("💬");
        }

        @Test
        @DisplayName("Keine Notification für altes Feedback (> 7 Tage)")
        void getDashboard_withOldFeedback_noNotification() {
            Submission oldSubmission = Submission.builder()
                    .assignmentId("assign-123")
                    .studentEmail("student@test.ch")
                    .teacherFeedback("Gut!")
                    .submittedAt(Instant.now().minusSeconds(10 * 24 * 60 * 60)) // vor 10 Tagen
                    .build();

            when(assignmentRepository.findByClassId("class-123")).thenReturn(new ArrayList<>());
            when(submissionRepository.findByStudentEmail("student@test.ch"))
                    .thenReturn(new ArrayList<>(List.of(oldSubmission)));
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getNotifications()).isEmpty();
        }

        @Test
        @DisplayName("Keine Notification ohne Feedback")
        void getDashboard_withoutFeedback_noNotification() {
            Submission submissionWithoutFeedback = Submission.builder()
                    .assignmentId("assign-123")
                    .studentEmail("student@test.ch")
                    .teacherFeedback(null) // kein Feedback
                    .submittedAt(Instant.now().minusSeconds(3600))
                    .build();

            when(assignmentRepository.findByClassId("class-123")).thenReturn(new ArrayList<>());
            when(submissionRepository.findByStudentEmail("student@test.ch"))
                    .thenReturn(new ArrayList<>(List.of(submissionWithoutFeedback)));
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getNotifications()).isEmpty();
        }

        @Test
        @DisplayName("Notification mit Note")
        void getDashboard_feedbackWithGrade_showsGrade() {
            Assignment assignment = Assignment.builder()
                    .id("assign-123")
                    .title("Test Assignment")
                    .status(AssignmentStatus.REVIEWED)
                    .classId("class-123")
                    .build();

            Submission submissionWithGrade = Submission.builder()
                    .assignmentId("assign-123")
                    .studentEmail("student@test.ch")
                    .teacherFeedback("Sehr gut!")
                    .grade(5)
                    .submittedAt(Instant.now().minusSeconds(3600))
                    .build();

            when(assignmentRepository.findByClassId("class-123"))
                    .thenReturn(new ArrayList<>(List.of(assignment)));
            when(submissionRepository.findByStudentEmail("student@test.ch"))
                    .thenReturn(new ArrayList<>(List.of(submissionWithGrade)));
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getNotifications()).hasSize(1);
            assertThat(result.getNotifications().get(0).getGrade()).isEqualTo(5);
        }

        @Test
        @DisplayName("Mehrere Notifications sortiert nach Datum")
        void getDashboard_multipleNotifications_sortedByDate() {
            Assignment assignment = Assignment.builder()
                    .id("assign-123")
                    .title("Assignment")
                    .status(AssignmentStatus.REVIEWED)
                    .classId("class-123")
                    .build();

            Submission older = Submission.builder()
                    .assignmentId("assign-123")
                    .studentEmail("student@test.ch")
                    .teacherFeedback("Älteres Feedback")
                    .submittedAt(Instant.now().minusSeconds(86400)) // vor 1 Tag
                    .build();

            Submission newer = Submission.builder()
                    .assignmentId("assign-123")
                    .studentEmail("student@test.ch")
                    .teacherFeedback("Neueres Feedback")
                    .submittedAt(Instant.now().minusSeconds(3600)) // vor 1 Stunde
                    .build();

            when(assignmentRepository.findByClassId("class-123"))
                    .thenReturn(new ArrayList<>(List.of(assignment)));
            when(submissionRepository.findByStudentEmail("student@test.ch"))
                    .thenReturn(new ArrayList<>(List.of(older, newer)));
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getNotifications()).hasSize(2);
            // Neueste zuerst
            assertThat(result.getNotifications().get(0).getCreatedAt())
                    .isAfter(result.getNotifications().get(1).getCreatedAt());
        }
    }

    // ========================================
    // Earned Badge Icons Tests
    // ========================================
    @Nested
    @DisplayName("Earned Badge Icons")
    class EarnedBadgeIconsTests {

        @Test
        @DisplayName("Verdiente Badge-Icons anzeigen")
        void getDashboard_withEarnedBadges_showsIcons() {
            Badge badge = Badge.builder()
                    .id("badge-1")
                    .title("Test Badge")
                    .icon("🏆")
                    .build();

            BadgeService.BadgeWithEarnedInfo earnedInfo = BadgeService.BadgeWithEarnedInfo.builder()
                    .badge(badge)
                    .earned(true)
                    .earnedAt(Instant.now())
                    .build();

            when(assignmentRepository.findByClassId("class-123")).thenReturn(new ArrayList<>());
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(badgeService.getBadgesWithEarnedInfo("student-123"))
                    .thenReturn(List.of(earnedInfo));

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getEarnedBadgeIcons()).containsExactly("🏆");
        }

        @Test
        @DisplayName("Nur verdiente Badges in Icons")
        void getDashboard_mixedBadges_onlyEarnedIcons() {
            Badge earnedBadge = Badge.builder().id("b1").icon("🏆").build();
            Badge notEarnedBadge = Badge.builder().id("b2").icon("🎯").build();

            BadgeService.BadgeWithEarnedInfo earned = BadgeService.BadgeWithEarnedInfo.builder()
                    .badge(earnedBadge)
                    .earned(true)
                    .build();
            BadgeService.BadgeWithEarnedInfo notEarned = BadgeService.BadgeWithEarnedInfo.builder()
                    .badge(notEarnedBadge)
                    .earned(false)
                    .build();

            when(assignmentRepository.findByClassId("class-123")).thenReturn(new ArrayList<>());
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(badgeService.getBadgesWithEarnedInfo("student-123"))
                    .thenReturn(List.of(earned, notEarned));

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getEarnedBadgeIcons()).containsExactly("🏆");
            assertThat(result.getEarnedBadgeIcons()).doesNotContain("🎯");
        }
    }

    // ========================================
    // Class ID Edge Cases
    // ========================================
    @Nested
    @DisplayName("Class ID Edge Cases")
    class ClassIdEdgeCases {

        @Test
        @DisplayName("Leere ClassId - lädt alle Assignments")
        void getDashboard_emptyClassId_loadsAllAssignments() {
            when(schoolClassService.getMyClassId()).thenReturn("");
            when(assignmentRepository.findAll()).thenReturn(new ArrayList<>());
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            studentDashboardService.getDashboardForCurrentStudent();

            verify(assignmentRepository).findAll();
            verify(assignmentRepository, never()).findByClassId(any());
        }

        @Test
        @DisplayName("Whitespace ClassId - lädt alle Assignments")
        void getDashboard_blankClassId_loadsAllAssignments() {
            when(schoolClassService.getMyClassId()).thenReturn("   ");
            when(assignmentRepository.findAll()).thenReturn(new ArrayList<>());
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            studentDashboardService.getDashboardForCurrentStudent();

            verify(assignmentRepository).findAll();
        }
    }

    // ========================================
    // Assignment Info Mapping
    // ========================================
    @Nested
    @DisplayName("Assignment Info Mapping")
    class AssignmentInfoMapping {

        @Test
        @DisplayName("Assignment mit null Type")
        void getDashboard_assignmentNullType_handlesGracefully() {
            Assignment assignment = Assignment.builder()
                    .id("assign-123")
                    .title("Test")
                    .type(null) // null type
                    .status(AssignmentStatus.ASSIGNED)
                    .classId("class-123")
                    .build();

            when(assignmentRepository.findByClassId("class-123"))
                    .thenReturn(new ArrayList<>(List.of(assignment)));
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getOpenAssignments()).hasSize(1);
            assertThat(result.getOpenAssignments().get(0).getType()).isNull();
        }

        @Test
        @DisplayName("Assignment mit null DueDate - sortiert korrekt")
        void getDashboard_assignmentNullDueDate_sortsCorrectly() {
            Assignment withDueDate = Assignment.builder()
                    .id("a1")
                    .title("Mit Datum")
                    .status(AssignmentStatus.ASSIGNED)
                    .dueDate(Instant.now().plusSeconds(86400))
                    .classId("class-123")
                    .build();

            Assignment withoutDueDate = Assignment.builder()
                    .id("a2")
                    .title("Ohne Datum")
                    .status(AssignmentStatus.ASSIGNED)
                    .dueDate(null)
                    .classId("class-123")
                    .build();

            when(assignmentRepository.findByClassId("class-123"))
                    .thenReturn(new ArrayList<>(List.of(withoutDueDate, withDueDate)));
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getOpenAssignments()).hasSize(2);
            // Mit DueDate sollte vor ohne DueDate kommen
            assertThat(result.getOpenAssignments().get(0).getTitle()).isEqualTo("Mit Datum");
        }
    }

    // ========================================
    // Session Edge Cases
    // ========================================
    @Nested
    @DisplayName("Session Edge Cases")
    class SessionEdgeCases {

        @Test
        @DisplayName("Keine Sessions - null Werte")
        void getDashboard_noSessions_nullValues() {
            when(assignmentRepository.findByClassId("class-123")).thenReturn(new ArrayList<>());
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getLastSessionId()).isNull();
            assertThat(result.getLastSessionStartedAt()).isNull();
            assertThat(result.getOpenSessionId()).isNull();
            assertThat(result.getTotalSessionsCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Mehrere offene Sessions - neueste wird angezeigt")
        void getDashboard_multipleOpenSessions_showsNewest() {
            Session older = Session.builder()
                    .id("old-session")
                    .studentId("student-123")
                    .status(SessionStatus.OPEN)
                    .startedAt(Instant.now().minusSeconds(3600))
                    .build();

            Session newer = Session.builder()
                    .id("new-session")
                    .studentId("student-123")
                    .status(SessionStatus.OPEN)
                    .startedAt(Instant.now())
                    .build();

            when(assignmentRepository.findByClassId("class-123")).thenReturn(new ArrayList<>());
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123"))
                    .thenReturn(new ArrayList<>(List.of(older, newer)));
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getOpenSessionId()).isEqualTo("new-session");
        }

        @Test
        @DisplayName("Nur geschlossene Sessions - kein offener Session")
        void getDashboard_onlyClosedSessions_noOpenSession() {
            Session closed = Session.builder()
                    .id("closed-session")
                    .studentId("student-123")
                    .status(SessionStatus.CLOSED)
                    .startedAt(Instant.now().minusSeconds(3600))
                    .build();

            when(assignmentRepository.findByClassId("class-123")).thenReturn(new ArrayList<>());
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123"))
                    .thenReturn(new ArrayList<>(List.of(closed)));
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getOpenSessionId()).isNull();
            assertThat(result.getTotalSessionsCount()).isEqualTo(1);
        }
    }
}

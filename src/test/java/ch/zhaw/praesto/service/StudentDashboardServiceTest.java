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
class StudentDashboardServiceTest {

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

    private Assignment testAssignment;
    private Session testSession;

    @BeforeEach
    void setUp() {
        testAssignment = Assignment.builder()
                .id("assign-123")
                .title("Bewerbungstraining")
                .type(AssignmentType.AI_INTERVIEW)
                .status(AssignmentStatus.ASSIGNED)
                .classId("class-123")
                .dueDate(Instant.now().plusSeconds(86400))
                .build();

        testSession = Session.builder()
                .id("session-123")
                .studentId("student-123")
                .status(SessionStatus.CLOSED)
                .startedAt(Instant.now().minusSeconds(3600))
                .build();
    }

    @Nested
    @DisplayName("getDashboardForCurrentStudent")
    class GetDashboard {

        @Test
        @DisplayName("Dashboard mit allen Daten abrufen")
        void getDashboard_withAllData_returnsCompleteResponse() {
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getUserName()).thenReturn("Max Mustermann");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(schoolClassService.getMyClassId()).thenReturn("class-123");
            when(assignmentRepository.findByClassId("class-123")).thenReturn(new ArrayList<>(List.of(testAssignment)));
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>(List.of(testSession)));
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(badgeService.getEarnedBadgeCount("student-123")).thenReturn(3L);
            when(badgeService.getBadgesWithEarnedInfo("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result).isNotNull();
            assertThat(result.getStudentName()).isEqualTo("Max Mustermann");
            assertThat(result.getOpenAssignmentsCount()).isEqualTo(1);
            assertThat(result.getTotalSessionsCount()).isEqualTo(1);
            assertThat(result.getBadgesCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Dashboard ohne Klasse - lädt alle Assignments")
        void getDashboard_noClass_loadsAllAssignments() {
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getUserName()).thenReturn("Max");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(schoolClassService.getMyClassId()).thenReturn(null);
            when(assignmentRepository.findAll()).thenReturn(new ArrayList<>(List.of(testAssignment)));
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(badgeService.getEarnedBadgeCount("student-123")).thenReturn(0L);
            when(badgeService.getBadgesWithEarnedInfo("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getOpenAssignmentsCount()).isEqualTo(1);
            verify(assignmentRepository).findAll();
            verify(assignmentRepository, never()).findByClassId(any());
        }

        @Test
        @DisplayName("Dashboard filtert abgegebene Assignments")
        void getDashboard_filtersSubmittedAssignments() {
            Submission submission = Submission.builder()
                    .assignmentId("assign-123")
                    .studentEmail("student@test.ch")
                    .build();

            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getUserName()).thenReturn("Max");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(schoolClassService.getMyClassId()).thenReturn("class-123");
            when(assignmentRepository.findByClassId("class-123")).thenReturn(new ArrayList<>(List.of(testAssignment)));
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>(List.of(submission)));
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(badgeService.getEarnedBadgeCount("student-123")).thenReturn(0L);
            when(badgeService.getBadgesWithEarnedInfo("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            // Assignment wurde abgegeben, also keine offenen Assignments
            assertThat(result.getOpenAssignmentsCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Dashboard filtert geschlossene Assignments")
        void getDashboard_filtersClosedAssignments() {
            Assignment closedAssignment = Assignment.builder()
                    .id("assign-closed")
                    .title("Geschlossen")
                    .status(AssignmentStatus.CLOSED)
                    .classId("class-123")
                    .build();

            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getUserName()).thenReturn("Max");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(schoolClassService.getMyClassId()).thenReturn("class-123");
            when(assignmentRepository.findByClassId("class-123")).thenReturn(new ArrayList<>(List.of(closedAssignment)));
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(badgeService.getEarnedBadgeCount("student-123")).thenReturn(0L);
            when(badgeService.getBadgesWithEarnedInfo("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getOpenAssignmentsCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Dashboard zeigt offene Session")
        void getDashboard_showsOpenSession() {
            Session openSession = Session.builder()
                    .id("open-session")
                    .studentId("student-123")
                    .status(SessionStatus.OPEN)
                    .startedAt(Instant.now())
                    .build();

            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getUserName()).thenReturn("Max");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(schoolClassService.getMyClassId()).thenReturn("class-123");
            when(assignmentRepository.findByClassId("class-123")).thenReturn(new ArrayList<>());
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>(List.of(openSession)));
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(badgeService.getEarnedBadgeCount("student-123")).thenReturn(0L);
            when(badgeService.getBadgesWithEarnedInfo("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getOpenSessionId()).isEqualTo("open-session");
        }

        @Test
        @DisplayName("Dashboard zeigt Bewerbungsanzahl")
        void getDashboard_showsApplicationCount() {
            Application app = Application.builder()
                    .id("app-123")
                    .studentId("student-123")
                    .companyName("Google")
                    .build();

            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getUserName()).thenReturn("Max");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(schoolClassService.getMyClassId()).thenReturn("class-123");
            when(assignmentRepository.findByClassId("class-123")).thenReturn(new ArrayList<>());
            when(submissionRepository.findByStudentEmail("student@test.ch")).thenReturn(new ArrayList<>());
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());
            when(applicationRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>(List.of(app)));
            when(badgeService.getEarnedBadgeCount("student-123")).thenReturn(0L);
            when(badgeService.getBadgesWithEarnedInfo("student-123")).thenReturn(new ArrayList<>());

            StudentDashboardResponse result = studentDashboardService.getDashboardForCurrentStudent();

            assertThat(result.getApplicationsCount()).isEqualTo(1);
        }
    }
}

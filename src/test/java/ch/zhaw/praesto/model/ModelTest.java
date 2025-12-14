package ch.zhaw.praesto.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Model Tests")
class ModelTest {

    // ========================================
    // Session Tests
    // ========================================
    @Nested
    @DisplayName("Session")
    class SessionTests {

        @Test
        @DisplayName("Session Builder")
        void session_builder_setsAllFields() {
            Instant now = Instant.now();
            List<SessionMessage> messages = new ArrayList<>();
            
            Session session = Session.builder()
                    .id("session-123")
                    .studentId("student-123")
                    .studentEmail("student@test.ch")
                    .assignmentId("assign-123")
                    .assignmentTitle("Test Assignment")
                    .targetDurationMin(30)
                    .status(SessionStatus.OPEN)
                    .messages(messages)
                    .startedAt(now)
                    .closedAt(null)
                    .score(85)
                    .submittedAsAssignment(false)
                    .build();

            assertThat(session.getId()).isEqualTo("session-123");
            assertThat(session.getStudentId()).isEqualTo("student-123");
            assertThat(session.getStudentEmail()).isEqualTo("student@test.ch");
            assertThat(session.getAssignmentId()).isEqualTo("assign-123");
            assertThat(session.getAssignmentTitle()).isEqualTo("Test Assignment");
            assertThat(session.getTargetDurationMin()).isEqualTo(30);
            assertThat(session.getStatus()).isEqualTo(SessionStatus.OPEN);
            assertThat(session.getMessages()).isEmpty();
            assertThat(session.getStartedAt()).isEqualTo(now);
            assertThat(session.getClosedAt()).isNull();
            assertThat(session.getScore()).isEqualTo(85);
            assertThat(session.isSubmittedAsAssignment()).isFalse();
        }

        @Test
        @DisplayName("Session Setter")
        void session_setters_work() {
            Session session = new Session();
            Instant now = Instant.now();
            
            session.setId("id");
            session.setStudentId("student");
            session.setStudentEmail("student@test.ch");
            session.setStatus(SessionStatus.CLOSED);
            session.setClosedAt(now);
            session.setScore(100);
            session.setSubmittedAsAssignment(true);

            assertThat(session.getId()).isEqualTo("id");
            assertThat(session.getStudentEmail()).isEqualTo("student@test.ch");
            assertThat(session.getStatus()).isEqualTo(SessionStatus.CLOSED);
            assertThat(session.getScore()).isEqualTo(100);
            assertThat(session.isSubmittedAsAssignment()).isTrue();
        }
    }

    // ========================================
    // Application Tests
    // ========================================
    @Nested
    @DisplayName("Application")
    class ApplicationTests {

        @Test
        @DisplayName("Application Builder")
        void application_builder_setsAllFields() {
            Instant now = Instant.now();
            LocalDate today = LocalDate.now();
            
            Application app = Application.builder()
                    .id("app-123")
                    .studentId("student-123")
                    .companyName("Google")
                    .position("Software Engineer")
                    .status(ApplicationStatus.APPLIED)
                    .appliedAt(today)
                    .interviewDate(today.plusDays(7))
                    .notes("Interessante Stelle")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            assertThat(app.getId()).isEqualTo("app-123");
            assertThat(app.getCompanyName()).isEqualTo("Google");
            assertThat(app.getPosition()).isEqualTo("Software Engineer");
            assertThat(app.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
            assertThat(app.getAppliedAt()).isEqualTo(today);
            assertThat(app.getInterviewDate()).isEqualTo(today.plusDays(7));
        }

        @ParameterizedTest
        @EnumSource(ApplicationStatus.class)
        @DisplayName("Application mit allen Status-Werten")
        void application_allStatusValues(ApplicationStatus status) {
            Application app = Application.builder()
                    .status(status)
                    .build();

            assertThat(app.getStatus()).isEqualTo(status);
        }
    }

    // ========================================
    // Assignment Tests
    // ========================================
    @Nested
    @DisplayName("Assignment")
    class AssignmentTests {

        @Test
        @DisplayName("Assignment Builder")
        void assignment_builder_setsAllFields() {
            Instant now = Instant.now();
            
            Assignment assignment = Assignment.builder()
                    .id("assign-123")
                    .title("Bewerbungstraining")
                    .description("Übe dein Vorstellungsgespräch")
                    .type(AssignmentType.AI_INTERVIEW)
                    .status(AssignmentStatus.ASSIGNED)
                    .classId("class-123")
                    .createdByTeacherId("teacher-123")
                    .dueDate(now)
                    .durationMin(30)
                    .createdAt(now)
                    .build();

            assertThat(assignment.getTitle()).isEqualTo("Bewerbungstraining");
            assertThat(assignment.getType()).isEqualTo(AssignmentType.AI_INTERVIEW);
            assertThat(assignment.getDurationMin()).isEqualTo(30);
        }

        @ParameterizedTest
        @EnumSource(AssignmentType.class)
        @DisplayName("Assignment mit allen Type-Werten")
        void assignment_allTypeValues(AssignmentType type) {
            Assignment assignment = Assignment.builder()
                    .type(type)
                    .build();

            assertThat(assignment.getType()).isEqualTo(type);
        }

        @ParameterizedTest
        @EnumSource(AssignmentStatus.class)
        @DisplayName("Assignment mit allen Status-Werten")
        void assignment_allStatusValues(AssignmentStatus status) {
            Assignment assignment = Assignment.builder()
                    .status(status)
                    .build();

            assertThat(assignment.getStatus()).isEqualTo(status);
        }
    }

    // ========================================
    // Note Tests
    // ========================================
    @Nested
    @DisplayName("Note")
    class NoteTests {

        @Test
        @DisplayName("Note Builder")
        void note_builder_setsAllFields() {
            Instant now = Instant.now();
            
            Note note = Note.builder()
                    .id("note-123")
                    .studentId("student-123")
                    .companyName("Google")
                    .position("Software Engineer")
                    .text("Wichtige Notiz")
                    .createdAt(now)
                    .lastUpdated(now)
                    .build();

            assertThat(note.getId()).isEqualTo("note-123");
            assertThat(note.getText()).isEqualTo("Wichtige Notiz");
            assertThat(note.getCompanyName()).isEqualTo("Google");
            assertThat(note.getPosition()).isEqualTo("Software Engineer");
        }
    }

    // ========================================
    // Badge Tests
    // ========================================
    @Nested
    @DisplayName("Badge")
    class BadgeTests {

        @Test
        @DisplayName("Badge Builder")
        void badge_builder_setsAllFields() {
            Badge badge = Badge.builder()
                    .id("badge-123")
                    .title("Erste Schritte")
                    .description("Erste Session abgeschlossen")
                    .icon("🎯")
                    .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                    .threshold(1)
                    .sortOrder(1)
                    .build();

            assertThat(badge.getTitle()).isEqualTo("Erste Schritte");
            assertThat(badge.getIcon()).isEqualTo("🎯");
            assertThat(badge.getThreshold()).isEqualTo(1);
        }

        @ParameterizedTest
        @EnumSource(BadgeRuleType.class)
        @DisplayName("Badge mit allen RuleType-Werten")
        void badge_allRuleTypes(BadgeRuleType ruleType) {
            Badge badge = Badge.builder()
                    .ruleType(ruleType)
                    .build();

            assertThat(badge.getRuleType()).isEqualTo(ruleType);
        }
    }

    // ========================================
    // UserBadge Tests
    // ========================================
    @Nested
    @DisplayName("UserBadge")
    class UserBadgeTests {

        @Test
        @DisplayName("UserBadge Builder")
        void userBadge_builder_setsAllFields() {
            Instant now = Instant.now();
            
            UserBadge userBadge = UserBadge.builder()
                    .id("ub-123")
                    .studentId("student-123")
                    .badgeId("badge-123")
                    .earnedAt(now)
                    .build();

            assertThat(userBadge.getStudentId()).isEqualTo("student-123");
            assertThat(userBadge.getBadgeId()).isEqualTo("badge-123");
            assertThat(userBadge.getEarnedAt()).isEqualTo(now);
        }
    }

    // ========================================
    // Submission Tests
    // ========================================
    @Nested
    @DisplayName("Submission")
    class SubmissionTests {

        @Test
        @DisplayName("Submission Builder")
        void submission_builder_setsAllFields() {
            Instant now = Instant.now();
            List<String> links = List.of("https://example.com");
            
            Submission submission = Submission.builder()
                    .id("sub-123")
                    .assignmentId("assign-123")
                    .studentEmail("student@test.ch")
                    .type(AssignmentType.AI_INTERVIEW)
                    .textContent("Mein Text")
                    .links(links)
                    .fileUrl("https://file.url")
                    .fileName("document.pdf")
                    .comment("Kommentar")
                    .chatSessionId("session-123")
                    .status(SubmissionStatus.SUBMITTED)
                    .submittedAt(now)
                    .teacherFeedback("Gut gemacht!")
                    .grade(5)
                    .reviewedAt(now)
                    .reviewedByTeacherId("teacher-123")
                    .build();

            assertThat(submission.getStudentEmail()).isEqualTo("student@test.ch");
            assertThat(submission.getGrade()).isEqualTo(5);
            assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
            assertThat(submission.getChatSessionId()).isEqualTo("session-123");
            assertThat(submission.getTextContent()).isEqualTo("Mein Text");
            assertThat(submission.getLinks()).hasSize(1);
        }

        @ParameterizedTest
        @EnumSource(SubmissionStatus.class)
        @DisplayName("Submission mit allen Status-Werten")
        void submission_allStatusValues(SubmissionStatus status) {
            Submission submission = Submission.builder()
                    .status(status)
                    .build();

            assertThat(submission.getStatus()).isEqualTo(status);
        }
    }

    // ========================================
    // SchoolClass Tests
    // ========================================
    @Nested
    @DisplayName("SchoolClass")
    class SchoolClassTests {

        @Test
        @DisplayName("SchoolClass Builder")
        void schoolClass_builder_setsAllFields() {
            List<String> students = List.of("student1@test.ch", "student2@test.ch");
            Instant now = Instant.now();
            
            SchoolClass schoolClass = SchoolClass.builder()
                    .id("class-123")
                    .name("3SE2")
                    .teacherId("teacher-123")
                    .studentEmails(students)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            assertThat(schoolClass.getName()).isEqualTo("3SE2");
            assertThat(schoolClass.getStudentEmails()).hasSize(2);
            assertThat(schoolClass.getTeacherId()).isEqualTo("teacher-123");
        }

        @Test
        @DisplayName("SchoolClass addStudent")
        void schoolClass_addStudent_addsNormalized() {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setStudentEmails(new ArrayList<>());
            
            schoolClass.addStudent("Test@Example.COM");
            
            assertThat(schoolClass.getStudentEmails()).containsExactly("test@example.com");
        }

        @Test
        @DisplayName("SchoolClass addStudent - kein Duplikat")
        void schoolClass_addStudent_noDuplicate() {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setStudentEmails(new ArrayList<>());
            
            schoolClass.addStudent("test@example.com");
            schoolClass.addStudent("TEST@EXAMPLE.COM");
            
            assertThat(schoolClass.getStudentEmails()).hasSize(1);
        }

        @Test
        @DisplayName("SchoolClass removeStudent")
        void schoolClass_removeStudent_removes() {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setStudentEmails(new ArrayList<>(List.of("test@example.com")));
            
            schoolClass.removeStudent("Test@Example.COM");
            
            assertThat(schoolClass.getStudentEmails()).isEmpty();
        }

        @Test
        @DisplayName("SchoolClass hasStudent")
        void schoolClass_hasStudent_returnsTrue() {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setStudentEmails(new ArrayList<>(List.of("test@example.com")));
            
            assertThat(schoolClass.hasStudent("TEST@EXAMPLE.COM")).isTrue();
            assertThat(schoolClass.hasStudent("other@example.com")).isFalse();
        }

        @Test
        @DisplayName("SchoolClass getStudentCount")
        void schoolClass_getStudentCount_returnsCount() {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setStudentEmails(new ArrayList<>(List.of("a@b.com", "c@d.com")));
            
            assertThat(schoolClass.getStudentCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("SchoolClass getStudentCount - null Liste")
        void schoolClass_getStudentCount_nullList_returnsZero() {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setStudentEmails(null);
            
            assertThat(schoolClass.getStudentCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("SchoolClass hasStudent - null Liste")
        void schoolClass_hasStudent_nullList_returnsFalse() {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setStudentEmails(null);
            
            assertThat(schoolClass.hasStudent("test@test.com")).isFalse();
        }

        @Test
        @DisplayName("SchoolClass removeStudent - null Liste")
        void schoolClass_removeStudent_nullList_noException() {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setStudentEmails(null);
            
            // Sollte keine Exception werfen
            schoolClass.removeStudent("test@test.com");
        }

        @Test
        @DisplayName("SchoolClass addStudent - null Liste wird initialisiert")
        void schoolClass_addStudent_nullList_initializes() {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setStudentEmails(null);
            
            schoolClass.addStudent("test@test.com");
            
            assertThat(schoolClass.getStudentEmails()).containsExactly("test@test.com");
        }
    }

    // ========================================
    // DTO Tests
    // ========================================
    @Nested
    @DisplayName("DTOs")
    class DTOTests {

        @Test
        @DisplayName("ApplicationDTO")
        void applicationDTO_gettersSetters() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setPosition("Dev");
            dto.setStatus("APPLIED");
            dto.setAppliedAt("2025-01-15");
            dto.setInterviewDate("2025-01-22");
            dto.setNotes("Test");

            assertThat(dto.getCompanyName()).isEqualTo("Google");
            assertThat(dto.getPosition()).isEqualTo("Dev");
            assertThat(dto.getStatus()).isEqualTo("APPLIED");
            assertThat(dto.getAppliedAt()).isEqualTo("2025-01-15");
            assertThat(dto.getInterviewDate()).isEqualTo("2025-01-22");
        }

        @Test
        @DisplayName("NoteDTO")
        void noteDTO_gettersSetters() {
            NoteDTO dto = new NoteDTO();
            dto.setCompanyName("Google");
            dto.setPosition("Software Engineer");
            dto.setText("Test Notiz");

            assertThat(dto.getCompanyName()).isEqualTo("Google");
            assertThat(dto.getPosition()).isEqualTo("Software Engineer");
            assertThat(dto.getText()).isEqualTo("Test Notiz");
        }

        @Test
        @DisplayName("SchoolClassDTO")
        void schoolClassDTO_gettersSetters() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("3SE2");
            dto.setStudentIds(List.of("student1", "student2"));

            assertThat(dto.getName()).isEqualTo("3SE2");
            assertThat(dto.getStudentIds()).hasSize(2);
        }

        @Test
        @DisplayName("ChatMessageRequest")
        void chatMessageRequest_gettersSetters() {
            ChatMessageRequest request = new ChatMessageRequest();
            request.setMessage("Hallo");

            assertThat(request.getMessage()).isEqualTo("Hallo");
        }

        @Test
        @DisplayName("StartSessionRequest")
        void startSessionRequest_gettersSetters() {
            StartSessionRequest request = new StartSessionRequest();
            request.setAssignmentId("assign-123");

            assertThat(request.getAssignmentId()).isEqualTo("assign-123");
        }

        @Test
        @DisplayName("AssignmentInfo Builder")
        void assignmentInfo_builder() {
            Instant now = Instant.now();
            
            AssignmentInfo info = AssignmentInfo.builder()
                    .id("assign-123")
                    .title("Test")
                    .type("AI_INTERVIEW")
                    .dueDate(now)
                    .durationMin(30)
                    .build();

            assertThat(info.getId()).isEqualTo("assign-123");
            assertThat(info.getTitle()).isEqualTo("Test");
            assertThat(info.getDurationMin()).isEqualTo(30);
        }

        @Test
        @DisplayName("NotificationInfo Builder")
        void notificationInfo_builder() {
            Instant now = Instant.now();
            
            NotificationInfo info = NotificationInfo.builder()
                    .type("FEEDBACK")
                    .icon("💬")
                    .title("Neues Feedback")
                    .message("Feedback erhalten")
                    .assignmentId("assign-123")
                    .grade(5)
                    .createdAt(now)
                    .build();

            assertThat(info.getType()).isEqualTo("FEEDBACK");
            assertThat(info.getIcon()).isEqualTo("💬");
            assertThat(info.getGrade()).isEqualTo(5);
        }

        @Test
        @DisplayName("ApplicationStats Builder")
        void applicationStats_builder() {
            ApplicationStats stats = ApplicationStats.builder()
                    .total(10L)
                    .planned(2L)
                    .applied(3L)
                    .invited(2L)
                    .interviewDone(1L)
                    .accepted(1L)
                    .rejected(1L)
                    .withdrawn(0L)
                    .active(5L)
                    .build();

            assertThat(stats.getTotal()).isEqualTo(10L);
            assertThat(stats.getAccepted()).isEqualTo(1L);
            assertThat(stats.getActive()).isEqualTo(5L);
        }

        @Test
        @DisplayName("StudentDashboardResponse Builder")
        void studentDashboardResponse_builder() {
            List<AssignmentInfo> assignments = new ArrayList<>();
            List<String> badgeIcons = List.of("🎯", "🔥");
            List<NotificationInfo> notifications = new ArrayList<>();
            Instant now = Instant.now();
            
            StudentDashboardResponse response = StudentDashboardResponse.builder()
                    .studentName("Max")
                    .openAssignmentsCount(5L)
                    .openAssignments(assignments)
                    .lastSessionId("session-123")
                    .lastSessionStartedAt(now)
                    .totalSessionsCount(10)
                    .openSessionId("open-session")
                    .applicationsCount(3)
                    .badgesCount(2L)
                    .earnedBadgeIcons(badgeIcons)
                    .notifications(notifications)
                    .build();

            assertThat(response.getStudentName()).isEqualTo("Max");
            assertThat(response.getOpenAssignmentsCount()).isEqualTo(5L);
            assertThat(response.getLastSessionId()).isEqualTo("session-123");
            assertThat(response.getTotalSessionsCount()).isEqualTo(10);
            assertThat(response.getEarnedBadgeIcons()).hasSize(2);
        }
    }

    // ========================================
    // SessionMessage Tests
    // ========================================
    @Nested
    @DisplayName("SessionMessage")
    class SessionMessageTests {

        @Test
        @DisplayName("SessionMessage Builder")
        void sessionMessage_builder() {
            Instant now = Instant.now();
            
            SessionMessage message = SessionMessage.builder()
                    .role("user")
                    .content("Hallo!")
                    .createdAt(now)
                    .build();

            assertThat(message.getRole()).isEqualTo("user");
            assertThat(message.getContent()).isEqualTo("Hallo!");
            assertThat(message.getCreatedAt()).isEqualTo(now);
        }
    }
}
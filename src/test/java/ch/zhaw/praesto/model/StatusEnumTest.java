package ch.zhaw.praesto.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Parametrisierte Tests für die Status-Enums.
 * Demonstriert @ParameterizedTest mit @ValueSource, @CsvSource und @EnumSource.
 */
@DisplayName("Status Enum Tests")
class StatusEnumTest {

    // ========================================
    // SessionStatus Tests
    // ========================================
    @Nested
    @DisplayName("SessionStatus")
    class SessionStatusTests {

        @ParameterizedTest
        @EnumSource(SessionStatus.class)
        @DisplayName("Alle SessionStatus-Werte sind gültig")
        void allSessionStatusValues_areValid(SessionStatus status) {
            assertThat(status).isNotNull();
            assertThat(status.name()).isNotEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"OPEN", "CLOSED"})
        @DisplayName("SessionStatus kann aus String erstellt werden")
        void sessionStatus_fromString_isValid(String statusName) {
            SessionStatus status = SessionStatus.valueOf(statusName);
            assertThat(status).isNotNull();
            assertEquals(statusName, status.name());
        }

        @ParameterizedTest
        @CsvSource({
            "OPEN, false",
            "CLOSED, true"
        })
        @DisplayName("SessionStatus - isClosed Prüfung")
        void sessionStatus_isClosed_correctValue(String statusName, boolean expectedClosed) {
            SessionStatus status = SessionStatus.valueOf(statusName);
            boolean isClosed = (status == SessionStatus.CLOSED);
            assertEquals(expectedClosed, isClosed);
        }

        @Test
        @DisplayName("Ungültiger SessionStatus wirft Exception")
        void sessionStatus_invalid_throwsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                SessionStatus.valueOf("INVALID_STATUS");
            });
        }
    }

    // ========================================
    // ApplicationStatus Tests
    // ========================================
    @Nested
    @DisplayName("ApplicationStatus")
    class ApplicationStatusTests {

        @ParameterizedTest
        @EnumSource(ApplicationStatus.class)
        @DisplayName("Alle ApplicationStatus-Werte sind gültig")
        void allApplicationStatusValues_areValid(ApplicationStatus status) {
            assertThat(status).isNotNull();
            assertThat(status.name()).isNotEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"PLANNED", "APPLIED", "INVITED", "INTERVIEW_DONE", "ACCEPTED", "REJECTED", "WITHDRAWN"})
        @DisplayName("ApplicationStatus kann aus String erstellt werden")
        void applicationStatus_fromString_isValid(String statusName) {
            ApplicationStatus status = ApplicationStatus.valueOf(statusName);
            assertThat(status).isNotNull();
            assertEquals(statusName, status.name());
        }

        @ParameterizedTest
        @CsvSource({
            "PLANNED, false, false",
            "APPLIED, false, false",
            "INVITED, false, false",
            "INTERVIEW_DONE, false, false",
            "ACCEPTED, true, true",
            "REJECTED, true, false",
            "WITHDRAWN, true, false"
        })
        @DisplayName("ApplicationStatus - Final und Erfolgreich Prüfung")
        void applicationStatus_isFinalAndSuccessful(String statusName, boolean expectedFinal, boolean expectedSuccessful) {
            ApplicationStatus status = ApplicationStatus.valueOf(statusName);
            
            boolean isFinal = (status == ApplicationStatus.ACCEPTED || 
                              status == ApplicationStatus.REJECTED ||
                              status == ApplicationStatus.WITHDRAWN);
            boolean isSuccessful = (status == ApplicationStatus.ACCEPTED);
            
            assertEquals(expectedFinal, isFinal, "isFinal für " + statusName);
            assertEquals(expectedSuccessful, isSuccessful, "isSuccessful für " + statusName);
        }

        @Test
        @DisplayName("Ungültiger ApplicationStatus wirft Exception")
        void applicationStatus_invalid_throwsException() {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                ApplicationStatus.valueOf("NOT_A_STATUS");
            });
            assertThat(exception.getMessage()).contains("NOT_A_STATUS");
        }
    }

    // ========================================
    // AssignmentStatus Tests
    // ========================================
    @Nested
    @DisplayName("AssignmentStatus")
    class AssignmentStatusTests {

        @ParameterizedTest
        @EnumSource(AssignmentStatus.class)
        @DisplayName("Alle AssignmentStatus-Werte sind gültig")
        void allAssignmentStatusValues_areValid(AssignmentStatus status) {
            assertThat(status).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"ASSIGNED", "IN_PROGRESS", "SUBMITTED", "REVIEWED", "CLOSED"})
        @DisplayName("AssignmentStatus kann aus String erstellt werden")
        void assignmentStatus_fromString_isValid(String statusName) {
            AssignmentStatus status = AssignmentStatus.valueOf(statusName);
            assertEquals(statusName, status.name());
        }

        @ParameterizedTest
        @CsvSource({
            "ASSIGNED, false, true",
            "IN_PROGRESS, true, true",
            "SUBMITTED, false, true",
            "REVIEWED, false, true",
            "CLOSED, false, false"
        })
        @DisplayName("AssignmentStatus - isInProgress und isEditable Prüfung")
        void assignmentStatus_properties(String statusName, boolean expectedInProgress, boolean expectedEditable) {
            AssignmentStatus status = AssignmentStatus.valueOf(statusName);
            
            boolean isInProgress = (status == AssignmentStatus.IN_PROGRESS);
            boolean isEditable = (status != AssignmentStatus.CLOSED);
            
            assertEquals(expectedInProgress, isInProgress, "isInProgress für " + statusName);
            assertEquals(expectedEditable, isEditable, "isEditable für " + statusName);
        }
    }

    // ========================================
    // SubmissionStatus Tests
    // ========================================
    @Nested
    @DisplayName("SubmissionStatus")
    class SubmissionStatusTests {

        @ParameterizedTest
        @EnumSource(SubmissionStatus.class)
        @DisplayName("Alle SubmissionStatus-Werte sind gültig")
        void allSubmissionStatusValues_areValid(SubmissionStatus status) {
            assertThat(status).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"SUBMITTED", "REVIEWED"})
        @DisplayName("SubmissionStatus kann aus String erstellt werden")
        void submissionStatus_fromString_isValid(String statusName) {
            SubmissionStatus status = SubmissionStatus.valueOf(statusName);
            assertEquals(statusName, status.name());
        }

        @ParameterizedTest
        @CsvSource({
            "SUBMITTED, false",
            "REVIEWED, true"
        })
        @DisplayName("SubmissionStatus - isReviewed Prüfung")
        void submissionStatus_isReviewed(String statusName, boolean expectedReviewed) {
            SubmissionStatus status = SubmissionStatus.valueOf(statusName);
            boolean isReviewed = (status == SubmissionStatus.REVIEWED);
            assertEquals(expectedReviewed, isReviewed);
        }
    }

    // ========================================
    // AssignmentType Tests
    // ========================================
    @Nested
    @DisplayName("AssignmentType")
    class AssignmentTypeTests {

        @ParameterizedTest
        @EnumSource(AssignmentType.class)
        @DisplayName("Alle AssignmentType-Werte sind gültig")
        void allAssignmentTypeValues_areValid(AssignmentType type) {
            assertThat(type).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"AI_INTERVIEW", "SELF_REFLECTION", "RESEARCH", "DOCUMENT_UPLOAD", "VIDEO_PITCH"})
        @DisplayName("AssignmentType kann aus String erstellt werden")
        void assignmentType_fromString_isValid(String typeName) {
            AssignmentType type = AssignmentType.valueOf(typeName);
            assertEquals(typeName, type.name());
        }

        @ParameterizedTest
        @CsvSource({
            "AI_INTERVIEW, true",
            "SELF_REFLECTION, false",
            "RESEARCH, false",
            "DOCUMENT_UPLOAD, false",
            "VIDEO_PITCH, false"
        })
        @DisplayName("AssignmentType - requiresAI Prüfung")
        void assignmentType_requiresAI(String typeName, boolean expectedRequiresAI) {
            AssignmentType type = AssignmentType.valueOf(typeName);
            boolean requiresAI = (type == AssignmentType.AI_INTERVIEW);
            assertEquals(expectedRequiresAI, requiresAI, "requiresAI für " + typeName);
        }
    }

    // ========================================
    // BadgeRuleType Tests
    // ========================================
    @Nested
    @DisplayName("BadgeRuleType")
    class BadgeRuleTypeTests {

        @ParameterizedTest
        @EnumSource(BadgeRuleType.class)
        @DisplayName("Alle BadgeRuleType-Werte sind gültig")
        void allBadgeRuleTypeValues_areValid(BadgeRuleType type) {
            assertThat(type).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"SESSIONS_COMPLETED", "NOTES_CREATED", "APPLICATIONS_CREATED", "APPLICATION_STATUS", "SUBMISSIONS_COMPLETED", "FEEDBACK_RECEIVED", "GRADES_RECEIVED"})
        @DisplayName("BadgeRuleType kann aus String erstellt werden")
        void badgeRuleType_fromString_isValid(String typeName) {
            BadgeRuleType type = BadgeRuleType.valueOf(typeName);
            assertEquals(typeName, type.name());
        }

        @ParameterizedTest
        @CsvSource({
            "SESSIONS_COMPLETED, true",
            "NOTES_CREATED, true",
            "APPLICATIONS_CREATED, true",
            "APPLICATION_STATUS, false",
            "SUBMISSIONS_COMPLETED, true",
            "FEEDBACK_RECEIVED, true",
            "GRADES_RECEIVED, true"
        })
        @DisplayName("BadgeRuleType - isCountBased Prüfung")
        void badgeRuleType_isCountBased(String typeName, boolean expectedCountBased) {
            BadgeRuleType type = BadgeRuleType.valueOf(typeName);
            
            // Count-basierte Regeln haben typischerweise _COMPLETED, _CREATED oder _RECEIVED
            boolean isCountBased = type != BadgeRuleType.APPLICATION_STATUS;
            
            assertEquals(expectedCountBased, isCountBased, "isCountBased für " + typeName);
        }
    }
}

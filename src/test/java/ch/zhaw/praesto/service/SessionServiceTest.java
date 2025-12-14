package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.repository.SessionRepository;
import ch.zhaw.praesto.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserService userService;

    @Mock
    private ChatClient chatClient;

    @Mock
    private BadgeService badgeService;

    @InjectMocks
    private SessionService sessionService;

    private Session testSession;

    @BeforeEach
    void setUp() {
        testSession = Session.builder()
                .id("session-123")
                .studentId("student-123")
                .status(SessionStatus.OPEN)
                .messages(new ArrayList<>())
                .startedAt(Instant.now())
                .build();
    }

    // ========================================
    // getSessionById
    // ========================================
    @Nested
    @DisplayName("getSessionById")
    class GetSessionById {

        @Test
        @DisplayName("Student sieht eigene Session")
        void getSessionById_ownSession_returnsSession() {
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));

            Session result = sessionService.getSessionById("session-123");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("session-123");
        }

        @Test
        @DisplayName("Teacher kann alle Sessions sehen")
        void getSessionById_asTeacher_returnsSession() {
            when(userService.getUserId()).thenReturn("teacher-123");
            when(userService.userHasRole("STUDENT")).thenReturn(false);
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));

            Session result = sessionService.getSessionById("session-123");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Session nicht gefunden")
        void getSessionById_notFound_throwsNotFound() {
            when(userService.getUserId()).thenReturn("student-123");
            when(sessionRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sessionService.getSessionById("unknown"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Fremde Session - kein Zugriff")
        void getSessionById_otherStudent_throwsForbidden() {
            when(userService.getUserId()).thenReturn("other-student");
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));

            assertThatThrownBy(() -> sessionService.getSessionById("session-123"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // getSessionsForStudent
    // ========================================
    @Nested
    @DisplayName("getSessionsForStudent")
    class GetSessionsForStudent {

        @Test
        @DisplayName("Sessions für Student abrufen")
        void getSessionsForStudent_returnsSortedList() {
            Session session1 = Session.builder().id("s1").studentId("student-123").startedAt(Instant.now().minusSeconds(100)).build();
            Session session2 = Session.builder().id("s2").studentId("student-123").startedAt(Instant.now()).build();
            // ArrayList statt List.of() damit sortiert werden kann
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>(List.of(session1, session2)));

            List<Session> result = sessionService.getSessionsForStudent("student-123");

            assertThat(result).hasSize(2);
            // Neueste zuerst
            assertThat(result.get(0).getId()).isEqualTo("s2");
        }

        @Test
        @DisplayName("Keine Sessions vorhanden")
        void getSessionsForStudent_noSessions_returnsEmptyList() {
            when(sessionRepository.findByStudentId("student-123")).thenReturn(new ArrayList<>());

            List<Session> result = sessionService.getSessionsForStudent("student-123");

            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // closeSession
    // ========================================
    @Nested
    @DisplayName("closeSession")
    class CloseSession {

        @Test
        @DisplayName("Session erfolgreich schliessen")
        void closeSession_ownSession_closesSession() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));
            when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

            Session result = sessionService.closeSession("session-123");

            assertThat(result).isNotNull();
            verify(sessionRepository).save(any(Session.class));
            verify(badgeService).checkAndAwardBadges("student-123");
        }

        @Test
        @DisplayName("Nicht-Student darf nicht schliessen")
        void closeSession_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> sessionService.closeSession("session-123"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Session nicht gefunden")
        void closeSession_notFound_throwsNotFound() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(sessionRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sessionService.closeSession("unknown"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Fremde Session schliessen - nicht erlaubt")
        void closeSession_otherStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("other-student");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));

            assertThatThrownBy(() -> sessionService.closeSession("session-123"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Bereits geschlossene Session")
        void closeSession_alreadyClosed_throwsBadRequest() {
            testSession.setStatus(SessionStatus.CLOSED);
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));

            assertThatThrownBy(() -> sessionService.closeSession("session-123"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("bereits geschlossen");
        }
    }

    // ========================================
    // closeAndSubmitAsAssignment
    // ========================================
    @Nested
    @DisplayName("closeAndSubmitAsAssignment")
    class CloseAndSubmit {

        @Test
        @DisplayName("Session erfolgreich abgeben")
        void closeAndSubmit_valid_submitsSession() {
            testSession.setAssignmentId("assign-123");
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));
            when(submissionRepository.existsByAssignmentIdAndStudentEmail("assign-123", "student@test.ch")).thenReturn(false);
            when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

            Session result = sessionService.closeAndSubmitAsAssignment("session-123");

            assertThat(result).isNotNull();
            verify(submissionRepository).save(any(Submission.class));
            verify(badgeService).checkAndAwardBadges("student-123");
        }

        @Test
        @DisplayName("Nicht-Student darf nicht abgeben")
        void closeAndSubmit_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> sessionService.closeAndSubmitAsAssignment("session-123"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Session ohne Assignment")
        void closeAndSubmit_noAssignment_throwsBadRequest() {
            // testSession hat kein assignmentId
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));

            assertThatThrownBy(() -> sessionService.closeAndSubmitAsAssignment("session-123"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("keiner Aufgabe");
        }

        @Test
        @DisplayName("Bereits abgegeben")
        void closeAndSubmit_alreadySubmitted_throwsBadRequest() {
            testSession.setAssignmentId("assign-123");
            testSession.setSubmittedAsAssignment(true);
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));

            assertThatThrownBy(() -> sessionService.closeAndSubmitAsAssignment("session-123"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("bereits abgegeben");
        }

        @Test
        @DisplayName("Doppelte Abgabe für Assignment")
        void closeAndSubmit_duplicateSubmission_throwsBadRequest() {
            testSession.setAssignmentId("assign-123");
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));
            when(submissionRepository.existsByAssignmentIdAndStudentEmail("assign-123", "student@test.ch")).thenReturn(true);

            assertThatThrownBy(() -> sessionService.closeAndSubmitAsAssignment("session-123"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("bereits abgegeben");
        }
    }

    // ========================================
    // startSession
    // ========================================
    @Nested
    @DisplayName("startSession")
    class StartSession {

        @Mock
        private ChatClientRequestSpec chatClientRequestSpec;
        
        @Mock
        private CallResponseSpec callResponseSpec;

        @Test
        @DisplayName("Nicht-Student darf keine Session starten")
        void startSession_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> sessionService.startSession(null))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("Nur Schueler");
        }

        @Test
        @DisplayName("Session mit ungültiger Assignment-ID")
        void startSession_invalidAssignment_throwsNotFound() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(assignmentRepository.findById("invalid-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sessionService.startSession("invalid-id"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Session für bereits abgegebene Aufgabe")
        void startSession_alreadySubmitted_throwsBadRequest() {
            Assignment assignment = Assignment.builder()
                    .id("assign-123")
                    .title("Test")
                    .build();
            
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(assignmentRepository.findById("assign-123")).thenReturn(Optional.of(assignment));
            when(submissionRepository.existsByAssignmentIdAndStudentEmail("assign-123", "student@test.ch")).thenReturn(true);

            assertThatThrownBy(() -> sessionService.startSession("assign-123"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("bereits abgegeben");
        }
    }

    // ========================================
    // addMessageAndGetAIResponse
    // ========================================
    @Nested
    @DisplayName("addMessageAndGetAIResponse")
    class AddMessage {

        @Test
        @DisplayName("Session nicht gefunden")
        void addMessage_sessionNotFound_throwsNotFound() {
            when(userService.getUserId()).thenReturn("student-123");
            when(sessionRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sessionService.addMessageAndGetAIResponse("unknown", "Hallo"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Fremde Session - kein Zugriff")
        void addMessage_otherStudent_throwsForbidden() {
            when(userService.getUserId()).thenReturn("other-student");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));

            assertThatThrownBy(() -> sessionService.addMessageAndGetAIResponse("session-123", "Hallo"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Geschlossene Session")
        void addMessage_closedSession_throwsBadRequest() {
            testSession.setStatus(SessionStatus.CLOSED);
            when(userService.getUserId()).thenReturn("student-123");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));

            assertThatThrownBy(() -> sessionService.addMessageAndGetAIResponse("session-123", "Hallo"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("bereits geschlossen");
        }
    }

    // ========================================
    // getSessionById - weitere Edge Cases
    // ========================================
    @Nested
    @DisplayName("getSessionById - Edge Cases")
    class GetSessionByIdEdgeCases {

        @Test
        @DisplayName("Weder Student noch Teacher - kein Zugriff")
        void getSessionById_noRole_throwsForbidden() {
            when(userService.getUserId()).thenReturn("unknown-user");
            when(userService.userHasRole("STUDENT")).thenReturn(false);
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));

            assertThatThrownBy(() -> sessionService.getSessionById("session-123"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }
}

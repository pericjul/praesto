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

    @Mock
    private AiQuotaService aiQuotaService;

    @InjectMocks
    private SessionService sessionService;

    private Session testSession;

    @BeforeEach
    void setUp() {
        // @Value-Felder werden in Unit-Tests nicht injiziert -> hier setzen.
        org.springframework.test.util.ReflectionTestUtils.setField(sessionService, "maxUserMessages", 30);
        org.springframework.test.util.ReflectionTestUtils.setField(sessionService, "assignmentInterviewsPerWeek", 2);

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
            when(userService.getCurrentSchoolId()).thenReturn("school-1");
            when(sessionRepository.findByIdAndSchoolId("session-123", "school-1")).thenReturn(Optional.of(testSession));

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
            when(userService.getCurrentSchoolId()).thenReturn("school-1");
            when(sessionRepository.findByIdAndSchoolId("session-123", "school-1")).thenReturn(Optional.of(testSession));

            Session result = sessionService.getSessionById("session-123");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Session nicht gefunden")
        void getSessionById_notFound_throwsNotFound() {
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getCurrentSchoolId()).thenReturn("school-1");
            when(sessionRepository.findByIdAndSchoolId("unknown", "school-1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sessionService.getSessionById("unknown"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Fremde Session - kein Zugriff")
        void getSessionById_otherStudent_throwsForbidden() {
            when(userService.getUserId()).thenReturn("other-student");
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn("school-1");
            when(sessionRepository.findByIdAndSchoolId("session-123", "school-1")).thenReturn(Optional.of(testSession));

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
            when(userService.getCurrentSchoolId()).thenReturn("school-1");
            when(sessionRepository.findByIdAndSchoolId("session-123", "school-1")).thenReturn(Optional.of(testSession));

            assertThatThrownBy(() -> sessionService.getSessionById("session-123"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // startSession - Erfolgreiche Fälle
    // ========================================
    @Nested
    @DisplayName("startSession - Success")
    class StartSessionSuccess {

        @Mock
        private ChatClientRequestSpec chatClientRequestSpec;

        @Mock
        private CallResponseSpec callResponseSpec;

        @BeforeEach
        void quotaStubs() {
            // Freies Üben: Tages-Quota erlaubt (1 pro Tag, heute noch 0 genutzt).
            lenient().when(aiQuotaService.practicePerDay()).thenReturn(1);
            lenient().when(aiQuotaService.practiceUsedToday(anyString())).thenReturn(0);
        }

        @Test
        @DisplayName("Session ohne Assignment erfolgreich starten")
        void startSession_noAssignment_createsSession() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(chatClient.prompt(anyString())).thenReturn(chatClientRequestSpec);
            when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.content()).thenReturn("Hallo! Ich bin dein Coach.");
            when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

            Session result = sessionService.startSession(null);

            assertThat(result).isNotNull();
            assertThat(result.getStudentId()).isEqualTo("student-123");
            assertThat(result.getStatus()).isEqualTo(SessionStatus.OPEN);
            assertThat(result.getMessages()).hasSize(1);
            verify(sessionRepository).save(any(Session.class));
        }

        @Test
        @DisplayName("Session ohne Assignment - leerer String wird ignoriert")
        void startSession_emptyAssignmentId_createsSession() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(chatClient.prompt(anyString())).thenReturn(chatClientRequestSpec);
            when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.content()).thenReturn("Hallo!");
            when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

            Session result = sessionService.startSession("");

            assertThat(result).isNotNull();
            assertThat(result.getAssignmentId()).isNull();
        }

        @Test
        @DisplayName("Session mit Assignment erfolgreich starten")
        void startSession_withAssignment_createsSessionWithAssignmentInfo() {
            Assignment assignment = Assignment.builder()
                    .id("assign-123")
                    .title("Interview Training")
                    .description("Übe dein Vorstellungsgespräch")
                    .durationMin(20)
                    .build();

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(assignmentRepository.findById("assign-123")).thenReturn(Optional.of(assignment));
            when(submissionRepository.existsByAssignmentIdAndStudentEmail("assign-123", "student@test.ch")).thenReturn(false);
            when(chatClient.prompt(anyString())).thenReturn(chatClientRequestSpec);
            when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.content()).thenReturn("Willkommen zum Interview!");
            when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

            Session result = sessionService.startSession("assign-123");

            assertThat(result).isNotNull();
            assertThat(result.getAssignmentId()).isEqualTo("assign-123");
            assertThat(result.getAssignmentTitle()).isEqualTo("Interview Training");
            assertThat(result.getTargetDurationMin()).isEqualTo(20);
        }

        @Test
        @DisplayName("Session mit Assignment ohne Duration - Default 15 Min")
        void startSession_assignmentNoDuration_usesDefault() {
            Assignment assignment = Assignment.builder()
                    .id("assign-123")
                    .title("Test")
                    .durationMin(null)
                    .build();

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(assignmentRepository.findById("assign-123")).thenReturn(Optional.of(assignment));
            when(submissionRepository.existsByAssignmentIdAndStudentEmail(anyString(), anyString())).thenReturn(false);
            when(chatClient.prompt(anyString())).thenReturn(chatClientRequestSpec);
            when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.content()).thenReturn("Hallo!");
            when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

            Session result = sessionService.startSession("assign-123");

            assertThat(result.getTargetDurationMin()).isEqualTo(15);
        }

        @Test
        @DisplayName("Session - KI-Fehler gibt Fallback-Nachricht")
        void startSession_aiError_usesFallbackMessage() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(chatClient.prompt(anyString())).thenThrow(new RuntimeException("API Error"));
            when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

            Session result = sessionService.startSession(null);

            assertThat(result).isNotNull();
            assertThat(result.getMessages()).hasSize(1);
            assertThat(result.getMessages().get(0).getContent()).contains("Bewerbungscoach");
        }

        @Test
        @DisplayName("Session mit Assignment - KI-Fehler gibt Assignment-Fallback")
        void startSession_withAssignment_aiError_usesFallbackMessage() {
            Assignment assignment = Assignment.builder()
                    .id("assign-123")
                    .title("Test")
                    .build();

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(assignmentRepository.findById("assign-123")).thenReturn(Optional.of(assignment));
            when(submissionRepository.existsByAssignmentIdAndStudentEmail(anyString(), anyString())).thenReturn(false);
            when(chatClient.prompt(anyString())).thenThrow(new RuntimeException("API Error"));
            when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

            Session result = sessionService.startSession("assign-123");

            assertThat(result.getMessages().get(0).getContent()).contains("üben");
        }
    }

    // ========================================
    // addMessageAndGetAIResponse - Erfolgreiche Fälle
    // ========================================
    @Nested
    @DisplayName("addMessageAndGetAIResponse - Success")
    class AddMessageSuccess {

        @Mock
        private ChatClientRequestSpec chatClientRequestSpec;

        @Mock
        private CallResponseSpec callResponseSpec;

        @Test
        @DisplayName("Nachricht erfolgreich hinzufügen und KI-Antwort erhalten")
        void addMessage_valid_addsMessageAndResponse() {
            testSession.setMessages(new ArrayList<>());
            
            when(userService.getUserId()).thenReturn("student-123");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));
            when(chatClient.prompt(any(Prompt.class))).thenReturn(chatClientRequestSpec);
            when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.content()).thenReturn("Das ist eine gute Frage!");
            when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

            Session result = sessionService.addMessageAndGetAIResponse("session-123", "Wie bereite ich mich vor?");

            assertThat(result.getMessages()).hasSize(2);
            assertThat(result.getMessages().get(0).getRole()).isEqualTo("USER");
            assertThat(result.getMessages().get(0).getContent()).isEqualTo("Wie bereite ich mich vor?");
            assertThat(result.getMessages().get(1).getRole()).isEqualTo("ASSISTANT");
        }

        @Test
        @DisplayName("Nachricht mit Assignment-Session")
        void addMessage_withAssignment_usesAssignmentPrompt() {
            testSession.setMessages(new ArrayList<>());
            testSession.setAssignmentId("assign-123");
            testSession.setAssignmentTitle("Interview Übung");
            testSession.setTargetDurationMin(15);

            when(userService.getUserId()).thenReturn("student-123");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));
            when(chatClient.prompt(any(Prompt.class))).thenReturn(chatClientRequestSpec);
            when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.content()).thenReturn("Gute Antwort!");
            when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

            Session result = sessionService.addMessageAndGetAIResponse("session-123", "Ich möchte Informatiker werden");

            assertThat(result.getMessages()).hasSize(2);
            verify(sessionRepository).save(any(Session.class));
        }

        @Test
        @DisplayName("KI-Fehler gibt Fallback-Nachricht")
        void addMessage_aiError_returnsFallback() {
            testSession.setMessages(new ArrayList<>());

            when(userService.getUserId()).thenReturn("student-123");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));
            when(chatClient.prompt(any(Prompt.class))).thenThrow(new RuntimeException("API Error"));
            when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

            Session result = sessionService.addMessageAndGetAIResponse("session-123", "Hallo");

            assertThat(result.getMessages()).hasSize(2);
            assertThat(result.getMessages().get(1).getContent()).contains("technischen Fehler");
        }

        @Test
        @DisplayName("Konversation mit mehreren Nachrichten")
        void addMessage_existingMessages_appendsCorrectly() {
            List<SessionMessage> existingMessages = new ArrayList<>();
            existingMessages.add(SessionMessage.builder().role("ASSISTANT").content("Hallo!").createdAt(Instant.now()).build());
            existingMessages.add(SessionMessage.builder().role("USER").content("Hi").createdAt(Instant.now()).build());
            testSession.setMessages(existingMessages);

            when(userService.getUserId()).thenReturn("student-123");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));
            when(chatClient.prompt(any(Prompt.class))).thenReturn(chatClientRequestSpec);
            when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.content()).thenReturn("Antwort");
            when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

            Session result = sessionService.addMessageAndGetAIResponse("session-123", "Neue Frage");

            assertThat(result.getMessages()).hasSize(4);
        }
    }

    // ========================================
    // closeAndSubmitAsAssignment - Weitere Fälle
    // ========================================
    @Nested
    @DisplayName("closeAndSubmitAsAssignment - Edge Cases")
    class CloseAndSubmitEdgeCases {

        @Test
        @DisplayName("Session nicht gefunden")
        void closeAndSubmit_sessionNotFound_throwsNotFound() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student@test.ch");
            when(sessionRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sessionService.closeAndSubmitAsAssignment("unknown"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Fremde Session abgeben - nicht erlaubt")
        void closeAndSubmit_otherStudent_throwsForbidden() {
            testSession.setAssignmentId("assign-123");
            
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("other-student");
            when(userService.getEmail()).thenReturn("other@test.ch");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));

            assertThatThrownBy(() -> sessionService.closeAndSubmitAsAssignment("session-123"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Email wird zu Lowercase normalisiert")
        void closeAndSubmit_uppercaseEmail_normalized() {
            testSession.setAssignmentId("assign-123");
            
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("STUDENT@TEST.CH");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));
            when(submissionRepository.existsByAssignmentIdAndStudentEmail("assign-123", "student@test.ch")).thenReturn(false);
            when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

            sessionService.closeAndSubmitAsAssignment("session-123");

            verify(submissionRepository).existsByAssignmentIdAndStudentEmail("assign-123", "student@test.ch");
        }
    }

    // ========================================
    // closeSession - Weitere Fälle
    // ========================================
    @Nested
    @DisplayName("closeSession - Edge Cases")
    class CloseSessionEdgeCases {

        @Test
        @DisplayName("Session wird korrekt mit Timestamp geschlossen")
        void closeSession_setsClosedAtTimestamp() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(sessionRepository.findById("session-123")).thenReturn(Optional.of(testSession));
            when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

            Session result = sessionService.closeSession("session-123");

            assertThat(result.getStatus()).isEqualTo(SessionStatus.CLOSED);
            assertThat(result.getClosedAt()).isNotNull();
        }
    }

    // ========================================
    // getSessionsForStudent - Weitere Fälle
    // ========================================
    @Nested
    @DisplayName("getSessionsForStudent - Edge Cases")
    class GetSessionsEdgeCases {

        @Test
        @DisplayName("Mehrere Sessions werden korrekt sortiert")
        void getSessionsForStudent_multipleSessions_sortedByDateDesc() {
            Instant now = Instant.now();
            Session s1 = Session.builder().id("s1").studentId("student-123").startedAt(now.minusSeconds(300)).build();
            Session s2 = Session.builder().id("s2").studentId("student-123").startedAt(now.minusSeconds(100)).build();
            Session s3 = Session.builder().id("s3").studentId("student-123").startedAt(now).build();

            when(sessionRepository.findByStudentId("student-123"))
                    .thenReturn(new ArrayList<>(List.of(s1, s3, s2)));

            List<Session> result = sessionService.getSessionsForStudent("student-123");

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getId()).isEqualTo("s3");
            assertThat(result.get(1).getId()).isEqualTo("s2");
            assertThat(result.get(2).getId()).isEqualTo("s1");
        }
    }
}

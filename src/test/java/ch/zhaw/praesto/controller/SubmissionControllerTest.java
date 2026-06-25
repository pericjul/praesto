package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.SubmissionRepository;
import ch.zhaw.praesto.security.TestSecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.mockito.Answers;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SubmissionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SchoolClassRepository schoolClassRepository;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private ChatModel chatModel;

    private static ObjectMapper objectMapper = new ObjectMapper();
    private String classId = "";
    private String assignmentId = "";
    private String submissionId = "";

    @BeforeAll
    public void setUp() {
        // Test-Klasse mit Test-Student (per User-Id) in der Schule des Test-Lehrers
        SchoolClass c = SchoolClass.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .name("SubmissionTestClass")
                .teacherId(TestSecurityConfig.TEACHER_ID)
                .studentIds(new ArrayList<>())
                .build();
        c.addStudent(TestSecurityConfig.STUDENT_ID);
        SchoolClass testClass = schoolClassRepository.save(c);
        classId = testClass.getId();

        // Test-Assignment erstellen
        Assignment assignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Submission Test Assignment")
                .description("Test")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        assignment = assignmentRepository.save(assignment);
        assignmentId = assignment.getId();
    }

    @AfterAll
    public void tearDown() {
        // Aufräumen
        if (submissionId != null && !submissionId.isEmpty()) {
            submissionRepository.deleteById(submissionId);
        }
        if (assignmentId != null && !assignmentId.isEmpty()) {
            assignmentRepository.deleteById(assignmentId);
        }
    }

    @Test
    @Order(10)
    public void testCreateSubmission() throws Exception {
        String jsonBody = """
                {
                    "assignmentId": "%s",
                    "textContent": "Dies ist meine Selbstreflexion zum Thema Bewerbung. Ich habe viel gelernt über meine Stärken und Schwächen."
                }
                """
                .formatted(assignmentId);

        var result = mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentId").value(assignmentId))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        submissionId = jsonNode.get("id").asText();
    }

    @Test
    @Order(15)
    public void testCreateSubmission_AlreadySubmitted() throws Exception {
        String jsonBody = """
                {
                    "assignmentId": "%s",
                    "textContent": "Nochmal abgeben sollte nicht gehen."
                }
                """.formatted(assignmentId);

        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(20)
    public void testGetMySubmissions() throws Exception {
        mvc.perform(get("/api/submissions/my")
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(25)
    public void testGetSubmission() throws Exception {
        mvc.perform(get("/api/submissions/" + submissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(submissionId));
    }

    @Test
    @Order(26)
    public void testCheckSubmission_HasSubmitted() throws Exception {
        mvc.perform(get("/api/submissions/check/" + assignmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasSubmitted").value(true));
    }

    @Test
    @Order(30)
    public void testGetSubmissionsForAssignment_AsTeacher() throws Exception {
        mvc.perform(get("/api/submissions/assignment/" + assignmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(35)
    public void testGetSubmission_AsTeacher() throws Exception {
        mvc.perform(get("/api/submissions/" + submissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(40)
    public void testGiveFeedback() throws Exception {
        String jsonBody = """
                {
                    "feedback": "Gute Reflexion!",
                    "grade": 5
                }
                """;

        mvc.perform(put("/api/submissions/" + submissionId + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherFeedback").value("Gute Reflexion!"))
                .andExpect(jsonPath("$.grade").value(5))
                .andExpect(jsonPath("$.status").value("REVIEWED"));
    }

    @Test
    public void testCreateSubmission_AsTeacher_Forbidden() throws Exception {
        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetMySubmissions_AsTeacher_Forbidden() throws Exception {
        mvc.perform(get("/api/submissions/my")
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetSubmissionsForAssignment_AsStudent_Forbidden() throws Exception {
        mvc.perform(get("/api/submissions/assignment/" + assignmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGiveFeedback_AsStudent_Forbidden() throws Exception {
        mvc.perform(put("/api/submissions/" + submissionId + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"feedback\": \"test\"}")
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCheckSubmission_AsTeacher_Forbidden() throws Exception {
        mvc.perform(get("/api/submissions/check/" + assignmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateSubmission_NoAuth() throws Exception {
        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateSubmission_TextTooShort() throws Exception {
        // Erstelle neues Assignment für diesen Test
        Assignment shortTextAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Short Text Test")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        shortTextAssignment = assignmentRepository.save(shortTextAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s",
                    "textContent": "Zu kurz"
                }
                """.formatted(shortTextAssignment.getId());

        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Aufräumen
        assignmentRepository.deleteById(shortTextAssignment.getId());
    }

    @Test
    public void testCreateSubmission_DocumentUpload_MissingUrl() throws Exception {
        // Erstelle Assignment für Dokument-Upload
        Assignment docAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Doc Upload Test")
                .type(AssignmentType.DOCUMENT_UPLOAD)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        docAssignment = assignmentRepository.save(docAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s"
                }
                """.formatted(docAssignment.getId());

        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Aufräumen
        assignmentRepository.deleteById(docAssignment.getId());
    }

    @Test
    public void testCreateSubmission_AIInterview_MissingSessionId() throws Exception {
        // Erstelle Assignment für AI Interview
        Assignment aiAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("AI Interview Test")
                .type(AssignmentType.AI_INTERVIEW)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        aiAssignment = assignmentRepository.save(aiAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s"
                }
                """.formatted(aiAssignment.getId());

        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Aufräumen
        assignmentRepository.deleteById(aiAssignment.getId());
    }

    @Test
    public void testCreateSubmission_AssignmentNotFound() throws Exception {
        String jsonBody = """
                {
                    "assignmentId": "nonexistent-id",
                    "textContent": "Dies ist ein langer Text mit mehr als 50 Zeichen für die Validierung."
                }
                """;

        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetSubmission_NotFound() throws Exception {
        mvc.perform(get("/api/submissions/nonexistent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGiveFeedback_SubmissionNotFound() throws Exception {
        mvc.perform(put("/api/submissions/nonexistent-id/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"feedback\": \"test\"}")
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateSubmission_Research_TextTooShort() throws Exception {
        Assignment researchAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Research Test")
                .type(AssignmentType.RESEARCH)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        researchAssignment = assignmentRepository.save(researchAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s",
                    "textContent": "Zu kurz"
                }
                """.formatted(researchAssignment.getId());

        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        assignmentRepository.deleteById(researchAssignment.getId());
    }

    @Test
    public void testCreateSubmission_VideoPitch_MissingUrl() throws Exception {
        Assignment videoAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Video Pitch Test")
                .type(AssignmentType.VIDEO_PITCH)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        videoAssignment = assignmentRepository.save(videoAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s"
                }
                """.formatted(videoAssignment.getId());

        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        assignmentRepository.deleteById(videoAssignment.getId());
    }

    @Test
    public void testCreateSubmission_SelfReflection_NullText() throws Exception {
        Assignment reflectionAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Reflection Null Test")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        reflectionAssignment = assignmentRepository.save(reflectionAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s"
                }
                """.formatted(reflectionAssignment.getId());

        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        assignmentRepository.deleteById(reflectionAssignment.getId());
    }

    @Test
    public void testCreateSubmission_DocumentUpload_BlankUrl() throws Exception {
        Assignment docAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Doc Blank URL Test")
                .type(AssignmentType.DOCUMENT_UPLOAD)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        docAssignment = assignmentRepository.save(docAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s",
                    "fileUrl": "   "
                }
                """.formatted(docAssignment.getId());

        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        assignmentRepository.deleteById(docAssignment.getId());
    }

    @Test
    public void testCreateSubmission_AIInterview_BlankSessionId() throws Exception {
        Assignment aiAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("AI Blank Session Test")
                .type(AssignmentType.AI_INTERVIEW)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        aiAssignment = assignmentRepository.save(aiAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s",
                    "chatSessionId": "   "
                }
                """.formatted(aiAssignment.getId());

        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        assignmentRepository.deleteById(aiAssignment.getId());
    }

    // ========================================
    // Erfolgreiche Abgaben für verschiedene Typen
    // ========================================

    @Test
    public void testCreateSubmission_DocumentUpload_Success() throws Exception {
        Assignment docAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Doc Upload Success Test")
                .type(AssignmentType.DOCUMENT_UPLOAD)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        docAssignment = assignmentRepository.save(docAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s",
                    "fileUrl": "https://example.com/document.pdf",
                    "fileName": "mein-dokument.pdf"
                }
                """.formatted(docAssignment.getId());

        var result = mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileUrl").value("https://example.com/document.pdf"))
                .andExpect(jsonPath("$.type").value("DOCUMENT_UPLOAD"))
                .andReturn();

        // Aufräumen
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        submissionRepository.deleteById(jsonNode.get("id").asText());
        assignmentRepository.deleteById(docAssignment.getId());
    }

    @Test
    public void testCreateSubmission_VideoPitch_Success() throws Exception {
        Assignment videoAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Video Pitch Success Test")
                .type(AssignmentType.VIDEO_PITCH)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        videoAssignment = assignmentRepository.save(videoAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s",
                    "fileUrl": "https://youtube.com/watch?v=123",
                    "comment": "Mein Video Pitch"
                }
                """.formatted(videoAssignment.getId());

        var result = mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("VIDEO_PITCH"))
                .andReturn();

        // Aufräumen
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        submissionRepository.deleteById(jsonNode.get("id").asText());
        assignmentRepository.deleteById(videoAssignment.getId());
    }

    @Test
    public void testCreateSubmission_AIInterview_Success() throws Exception {
        Assignment aiAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("AI Interview Success Test")
                .type(AssignmentType.AI_INTERVIEW)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        aiAssignment = assignmentRepository.save(aiAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s",
                    "chatSessionId": "session-123-abc"
                }
                """.formatted(aiAssignment.getId());

        var result = mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chatSessionId").value("session-123-abc"))
                .andExpect(jsonPath("$.type").value("AI_INTERVIEW"))
                .andReturn();

        // Aufräumen
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        submissionRepository.deleteById(jsonNode.get("id").asText());
        assignmentRepository.deleteById(aiAssignment.getId());
    }

    @Test
    public void testCreateSubmission_Research_Success() throws Exception {
        Assignment researchAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Research Success Test")
                .type(AssignmentType.RESEARCH)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        researchAssignment = assignmentRepository.save(researchAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s",
                    "textContent": "Dies ist meine Recherche zum Thema XY. Ich habe verschiedene Quellen analysiert und bin zu folgenden Erkenntnissen gekommen.",
                    "links": ["https://example.com/source1", "https://example.com/source2"]
                }
                """
                .formatted(researchAssignment.getId());

        var result = mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("RESEARCH"))
                .andReturn();

        // Aufräumen
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        submissionRepository.deleteById(jsonNode.get("id").asText());
        assignmentRepository.deleteById(researchAssignment.getId());
    }

    // ========================================
    // Berechtigungsprüfungen
    // ========================================

    @Test
    public void testCreateSubmission_StudentNotInClass() throws Exception {
        // Erstelle Klasse ohne den Test-Student
        SchoolClass otherClass = SchoolClass.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .name("OtherClassForSubmissionTest")
                .teacherId(TestSecurityConfig.TEACHER_ID)
                .studentIds(new ArrayList<>())
                .build();
        otherClass.addStudent("some-other-student-id");
        otherClass = schoolClassRepository.save(otherClass);

        Assignment assignmentOtherClass = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Assignment Other Class")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(otherClass.getId())
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        assignmentOtherClass = assignmentRepository.save(assignmentOtherClass);

        String jsonBody = """
                {
                    "assignmentId": "%s",
                    "textContent": "Dies ist ein langer Text mit mehr als 50 Zeichen für die Validierung der Abgabe."
                }
                """.formatted(assignmentOtherClass.getId());

        mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Aufräumen
        assignmentRepository.deleteById(assignmentOtherClass.getId());
        schoolClassRepository.deleteById(otherClass.getId());
    }

    @Test
    public void testGetSubmissionsForAssignment_OtherTeacher_Forbidden() throws Exception {
        // Assignment gehört test-user-id, aber wir versuchen mit anderem Teacher
        // Da TestSecurityConfig.TEACHER auch test-user-id ist, müssen wir ein
        // Assignment
        // mit anderem Teacher erstellen
        Assignment otherTeacherAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Other Teacher Assignment")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId("other-teacher-id")
                .status(AssignmentStatus.ASSIGNED)
                .build();
        otherTeacherAssignment = assignmentRepository.save(otherTeacherAssignment);

        mvc.perform(get("/api/submissions/assignment/" + otherTeacherAssignment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Aufräumen
        assignmentRepository.deleteById(otherTeacherAssignment.getId());
    }

    @Test
    public void testGetSubmissionsForAssignment_NotFound() throws Exception {
        mvc.perform(get("/api/submissions/assignment/nonexistent-assignment-id")
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGiveFeedback_OtherTeacher_Forbidden() throws Exception {
        // Erstelle Submission für Assignment eines anderen Teachers
        Assignment otherTeacherAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Feedback Other Teacher Test")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId("other-teacher-id")
                .status(AssignmentStatus.ASSIGNED)
                .build();
        otherTeacherAssignment = assignmentRepository.save(otherTeacherAssignment);

        Submission otherSubmission = Submission.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .assignmentId(otherTeacherAssignment.getId())
                .studentEmail("test@test.ch")
                .type(AssignmentType.SELF_REFLECTION)
                .textContent("Test content")
                .status(SubmissionStatus.SUBMITTED)
                .build();
        otherSubmission = submissionRepository.save(otherSubmission);

        mvc.perform(put("/api/submissions/" + otherSubmission.getId() + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"feedback\": \"test\"}")
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Aufräumen
        submissionRepository.deleteById(otherSubmission.getId());
        assignmentRepository.deleteById(otherTeacherAssignment.getId());
    }

    // ========================================
    // checkSubmission Tests
    // ========================================

    @Test
    public void testCheckSubmission_NotSubmitted() throws Exception {
        // Erstelle neues Assignment ohne Abgabe
        Assignment newAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Check Not Submitted Test")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        newAssignment = assignmentRepository.save(newAssignment);

        mvc.perform(get("/api/submissions/check/" + newAssignment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasSubmitted").value(false));

        // Aufräumen
        assignmentRepository.deleteById(newAssignment.getId());
    }

    // ========================================
    // giveFeedback Varianten
    // ========================================

    @Test
    @Order(41)
    public void testGiveFeedback_OnlyFeedback() throws Exception {
        // Erstelle neue Submission für diesen Test
        Assignment feedbackAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Only Feedback Test")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        feedbackAssignment = assignmentRepository.save(feedbackAssignment);

        Submission feedbackSubmission = Submission.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .assignmentId(feedbackAssignment.getId())
                .studentEmail("test@test.ch")
                .type(AssignmentType.SELF_REFLECTION)
                .textContent("Dies ist ein langer Text mit mehr als 50 Zeichen für die Validierung.")
                .status(SubmissionStatus.SUBMITTED)
                .build();
        feedbackSubmission = submissionRepository.save(feedbackSubmission);

        String jsonBody = """
                {
                    "feedback": "Nur Feedback ohne Note"
                }
                """;

        mvc.perform(put("/api/submissions/" + feedbackSubmission.getId() + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherFeedback").value("Nur Feedback ohne Note"))
                .andExpect(jsonPath("$.grade").isEmpty());

        // Aufräumen
        submissionRepository.deleteById(feedbackSubmission.getId());
        assignmentRepository.deleteById(feedbackAssignment.getId());
    }

    @Test
    public void testGiveFeedback_OnlyGrade() throws Exception {
        Assignment gradeAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Only Grade Test")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        gradeAssignment = assignmentRepository.save(gradeAssignment);

        Submission gradeSubmission = Submission.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .assignmentId(gradeAssignment.getId())
                .studentEmail("test@test.ch")
                .type(AssignmentType.SELF_REFLECTION)
                .textContent("Test content for grade only test with enough characters.")
                .status(SubmissionStatus.SUBMITTED)
                .build();
        gradeSubmission = submissionRepository.save(gradeSubmission);

        String jsonBody = """
                {
                    "grade": 6
                }
                """;

        mvc.perform(put("/api/submissions/" + gradeSubmission.getId() + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value(6))
                .andExpect(jsonPath("$.status").value("REVIEWED"));

        // Aufräumen
        submissionRepository.deleteById(gradeSubmission.getId());
        assignmentRepository.deleteById(gradeAssignment.getId());
    }

    // ========================================
    // getSubmission Berechtigungen
    // ========================================

    @Test
    public void testGetSubmission_OtherStudent_Forbidden() throws Exception {
        // Erstelle Submission eines anderen Students
        Submission otherStudentSubmission = Submission.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .assignmentId(assignmentId)
                .studentEmail("other@student.ch")
                .type(AssignmentType.SELF_REFLECTION)
                .textContent("Other student submission")
                .status(SubmissionStatus.SUBMITTED)
                .build();
        otherStudentSubmission = submissionRepository.save(otherStudentSubmission);

        mvc.perform(get("/api/submissions/" + otherStudentSubmission.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Aufräumen
        submissionRepository.deleteById(otherStudentSubmission.getId());
    }

    @Test
    public void testGetSubmission_TeacherOtherAssignment_Forbidden() throws Exception {
        // Erstelle Assignment eines anderen Teachers
        Assignment otherAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Other Teacher Get Test")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId("other-teacher-id")
                .status(AssignmentStatus.ASSIGNED)
                .build();
        otherAssignment = assignmentRepository.save(otherAssignment);

        Submission otherSubmission = Submission.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .assignmentId(otherAssignment.getId())
                .studentEmail("test@test.ch")
                .type(AssignmentType.SELF_REFLECTION)
                .textContent("Test")
                .status(SubmissionStatus.SUBMITTED)
                .build();
        otherSubmission = submissionRepository.save(otherSubmission);

        mvc.perform(get("/api/submissions/" + otherSubmission.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Aufräumen
        submissionRepository.deleteById(otherSubmission.getId());
        assignmentRepository.deleteById(otherAssignment.getId());
    }

    // ========================================
    // Ohne Authentifizierung
    // ========================================

    @Test
    public void testGetMySubmissions_NoAuth() throws Exception {
        mvc.perform(get("/api/submissions/my")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetSubmission_NoAuth() throws Exception {
        mvc.perform(get("/api/submissions/" + submissionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCheckSubmission_NoAuth() throws Exception {
        mvc.perform(get("/api/submissions/check/" + assignmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetSubmissionsForAssignment_NoAuth() throws Exception {
        mvc.perform(get("/api/submissions/assignment/" + assignmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGiveFeedback_NoAuth() throws Exception {
        mvc.perform(put("/api/submissions/" + submissionId + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"feedback\": \"test\"}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


    // ========================================
    // Submission mit optionalen Feldern
    // ========================================

    @Test
    public void testCreateSubmission_WithComment() throws Exception {
        Assignment commentAssignment = Assignment.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .title("Comment Test")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId(TestSecurityConfig.TEACHER_ID)
                .status(AssignmentStatus.ASSIGNED)
                .build();
        commentAssignment = assignmentRepository.save(commentAssignment);

        String jsonBody = """
                {
                    "assignmentId": "%s",
                    "textContent": "Dies ist meine Reflexion mit einem Kommentar. Der Text muss mindestens 50 Zeichen haben.",
                    "comment": "Bitte beachten Sie meine Anmerkungen"
                }
                """
                .formatted(commentAssignment.getId());

        var result = mvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment").value("Bitte beachten Sie meine Anmerkungen"))
                .andReturn();

        // Aufräumen
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        submissionRepository.deleteById(jsonNode.get("id").asText());
        assignmentRepository.deleteById(commentAssignment.getId());
    }
}

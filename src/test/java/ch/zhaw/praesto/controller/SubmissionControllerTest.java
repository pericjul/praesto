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
import org.springframework.ai.openai.OpenAiChatModel;
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
@Import(TestSecurityConfig.class)
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
    private OpenAiChatModel chatModel;

    private static ObjectMapper objectMapper = new ObjectMapper();
    private String classId = "";
    private String assignmentId = "";
    private String submissionId = "";

    @BeforeAll
    public void setUp() {
        // Test-Klasse mit Student erstellen
        SchoolClass testClass = schoolClassRepository.findByName("SubmissionTestClass")
                .orElseGet(() -> {
                    SchoolClass c = SchoolClass.builder()
                            .name("SubmissionTestClass")
                            .teacherId("test-user-id")
                            .studentEmails(new ArrayList<>())
                            .build();
                    c.getStudentEmails().add("test@test.ch"); // Student hinzufügen
                    return schoolClassRepository.save(c);
                });
        classId = testClass.getId();

        // Sicherstellen dass Student in Klasse ist
        if (!testClass.getStudentEmails().contains("test@test.ch")) {
            testClass.getStudentEmails().add("test@test.ch");
            schoolClassRepository.save(testClass);
        }

        // Test-Assignment erstellen
        Assignment assignment = Assignment.builder()
                .title("Submission Test Assignment")
                .description("Test")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId("test-user-id")
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
            """.formatted(assignmentId);

        var result = mvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(20)
    public void testGetMySubmissions() throws Exception {
        mvc.perform(get("/api/submissions/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(25)
    public void testGetSubmission() throws Exception {
        mvc.perform(get("/api/submissions/" + submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(submissionId));
    }

    @Test
    @Order(26)
    public void testCheckSubmission_HasSubmitted() throws Exception {
        mvc.perform(get("/api/submissions/check/" + assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasSubmitted").value(true));
    }

    @Test
    @Order(30)
    public void testGetSubmissionsForAssignment_AsTeacher() throws Exception {
        mvc.perform(get("/api/submissions/assignment/" + assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(35)
    public void testGetSubmission_AsTeacher() throws Exception {
        mvc.perform(get("/api/submissions/" + submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetMySubmissions_AsTeacher_Forbidden() throws Exception {
        mvc.perform(get("/api/submissions/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetSubmissionsForAssignment_AsStudent_Forbidden() throws Exception {
        mvc.perform(get("/api/submissions/assignment/" + assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGiveFeedback_AsStudent_Forbidden() throws Exception {
        mvc.perform(put("/api/submissions/" + submissionId + "/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"feedback\": \"test\"}")
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCheckSubmission_AsTeacher_Forbidden() throws Exception {
        mvc.perform(get("/api/submissions/check/" + assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
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
                .title("Short Text Test")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId("test-user-id")
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isBadRequest());
        
        // Aufräumen
        assignmentRepository.deleteById(shortTextAssignment.getId());
    }

    @Test
    public void testCreateSubmission_DocumentUpload_MissingUrl() throws Exception {
        // Erstelle Assignment für Dokument-Upload
        Assignment docAssignment = Assignment.builder()
                .title("Doc Upload Test")
                .type(AssignmentType.DOCUMENT_UPLOAD)
                .classId(classId)
                .createdByTeacherId("test-user-id")
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isBadRequest());
        
        // Aufräumen
        assignmentRepository.deleteById(docAssignment.getId());
    }

    @Test
    public void testCreateSubmission_AIInterview_MissingSessionId() throws Exception {
        // Erstelle Assignment für AI Interview
        Assignment aiAssignment = Assignment.builder()
                .title("AI Interview Test")
                .type(AssignmentType.AI_INTERVIEW)
                .classId(classId)
                .createdByTeacherId("test-user-id")
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetSubmission_NotFound() throws Exception {
        mvc.perform(get("/api/submissions/nonexistent-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGiveFeedback_SubmissionNotFound() throws Exception {
        mvc.perform(put("/api/submissions/nonexistent-id/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"feedback\": \"test\"}")
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateSubmission_Research_TextTooShort() throws Exception {
        Assignment researchAssignment = Assignment.builder()
                .title("Research Test")
                .type(AssignmentType.RESEARCH)
                .classId(classId)
                .createdByTeacherId("test-user-id")
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isBadRequest());
        
        assignmentRepository.deleteById(researchAssignment.getId());
    }

    @Test
    public void testCreateSubmission_VideoPitch_MissingUrl() throws Exception {
        Assignment videoAssignment = Assignment.builder()
                .title("Video Pitch Test")
                .type(AssignmentType.VIDEO_PITCH)
                .classId(classId)
                .createdByTeacherId("test-user-id")
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isBadRequest());
        
        assignmentRepository.deleteById(videoAssignment.getId());
    }

    @Test
    public void testCreateSubmission_SelfReflection_NullText() throws Exception {
        Assignment reflectionAssignment = Assignment.builder()
                .title("Reflection Null Test")
                .type(AssignmentType.SELF_REFLECTION)
                .classId(classId)
                .createdByTeacherId("test-user-id")
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isBadRequest());
        
        assignmentRepository.deleteById(reflectionAssignment.getId());
    }

    @Test
    public void testCreateSubmission_DocumentUpload_BlankUrl() throws Exception {
        Assignment docAssignment = Assignment.builder()
                .title("Doc Blank URL Test")
                .type(AssignmentType.DOCUMENT_UPLOAD)
                .classId(classId)
                .createdByTeacherId("test-user-id")
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isBadRequest());
        
        assignmentRepository.deleteById(docAssignment.getId());
    }

    @Test
    public void testCreateSubmission_AIInterview_BlankSessionId() throws Exception {
        Assignment aiAssignment = Assignment.builder()
                .title("AI Blank Session Test")
                .type(AssignmentType.AI_INTERVIEW)
                .classId(classId)
                .createdByTeacherId("test-user-id")
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isBadRequest());
        
        assignmentRepository.deleteById(aiAssignment.getId());
    }
}

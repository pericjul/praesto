package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.SchoolClass;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.security.TestSecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
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
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AssignmentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SchoolClassRepository schoolClassRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    // Mock OpenAiChatModel wie in Übung 11 beschrieben
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private OpenAiChatModel chatModel;

    private static ObjectMapper objectMapper = new ObjectMapper();
    private String class_id = "";
    private String assignment_id = "";

    @BeforeAll
    public void setUp() {
        // Test-Klasse in der Schule des Test-Lehrers erstellen
        SchoolClass c = SchoolClass.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .name("AssignmentTestClass")
                .teacherId(TestSecurityConfig.TEACHER_ID)
                .studentIds(new ArrayList<>())
                .build();
        SchoolClass testClass = schoolClassRepository.save(c);
        class_id = testClass.getId();
    }

    @Test
    @Order(10)
    public void testCreateAssignment() throws Exception {
        String jsonBody = """
            {
                "title": "Bewerbungstraining",
                "description": "Übe ein Vorstellungsgespräch",
                "durationMin": 15,
                "classId": "%s",
                "type": "AI_INTERVIEW"
            }
            """.formatted(class_id);

        var result = mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].title").value("Bewerbungstraining"))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        assignment_id = jsonNode.get(0).get("id").asText();
    }

    @Test
    @Order(20)
    public void testGetAssignment() throws Exception {
        mvc.perform(get("/api/assignments/" + assignment_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Bewerbungstraining"));
    }

    @Test
    @Order(25)
    public void testGetTeacherAssignments() throws Exception {
        mvc.perform(get("/api/assignments/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(26)
    public void testGetAssignmentsForClass() throws Exception {
        mvc.perform(get("/api/assignments/class/" + class_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(30)
    public void testUpdateAssignment() throws Exception {
        String jsonBody = """
            {
                "title": "Aktualisiert",
                "description": "Neue Beschreibung",
                "durationMin": 20,
                "classId": "%s",
                "type": "AI_INTERVIEW"
            }
            """.formatted(class_id);

        mvc.perform(put("/api/assignments/" + assignment_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Aktualisiert"));
    }

    @Test
    @Order(40)
    public void testDeleteAssignment() throws Exception {
        mvc.perform(delete("/api/assignments/" + assignment_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void testCreateAssignment_AsStudent_Forbidden() throws Exception {
        String jsonBody = """
            {
                "title": "Test",
                "classId": "%s",
                "type": "AI_INTERVIEW"
            }
            """.formatted(class_id);

        mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateAssignment_NoAuth() throws Exception {
        mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAssignment_NotFound() throws Exception {
        mvc.perform(get("/api/assignments/nonexistent-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAssignment_AsStudent_Forbidden() throws Exception {
        // Erst ein Assignment erstellen
        String jsonBody = """
            {
                "title": "Student View Test",
                "classId": "%s",
                "type": "SELF_REFLECTION"
            }
            """.formatted(class_id);

        var result = mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.teacher()))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        String newAssignmentId = jsonNode.get(0).get("id").asText();

        // Als Student abrufen - sollte Forbidden sein
        mvc.perform(get("/api/assignments/" + newAssignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Aufräumen
        assignmentRepository.deleteById(newAssignmentId);
    }

    @Test
    public void testUpdateAssignment_NotFound() throws Exception {
        String jsonBody = """
            {
                "title": "Test",
                "classId": "%s",
                "type": "AI_INTERVIEW"
            }
            """.formatted(class_id);

        mvc.perform(put("/api/assignments/nonexistent-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteAssignment_NotFound() throws Exception {
        mvc.perform(delete("/api/assignments/nonexistent-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateAssignment_AsStudent_Forbidden() throws Exception {
        // Erst ein Assignment erstellen
        String jsonBody = """
            {
                "title": "Forbidden Update Test",
                "classId": "%s",
                "type": "SELF_REFLECTION"
            }
            """.formatted(class_id);

        var result = mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.teacher()))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        String newAssignmentId = jsonNode.get(0).get("id").asText();

        // Als Student updaten versuchen
        mvc.perform(put("/api/assignments/" + newAssignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Aufräumen
        assignmentRepository.deleteById(newAssignmentId);
    }

    @Test
    public void testDeleteAssignment_AsStudent_Forbidden() throws Exception {
        // Erst ein Assignment erstellen
        String jsonBody = """
            {
                "title": "Forbidden Delete Test",
                "classId": "%s",
                "type": "SELF_REFLECTION"
            }
            """.formatted(class_id);

        var result = mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.teacher()))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        String newAssignmentId = jsonNode.get(0).get("id").asText();

        // Als Student löschen versuchen
        mvc.perform(delete("/api/assignments/" + newAssignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Aufräumen
        assignmentRepository.deleteById(newAssignmentId);
    }

    @Test
    public void testGetTeacherAssignments_AsStudent_Forbidden() throws Exception {
        mvc.perform(get("/api/assignments/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // ========================================
    // createAssignment Validierung
    // ========================================

    @Test
    public void testCreateAssignment_EmptyTitle() throws Exception {
        String jsonBody = """
            {
                "title": "",
                "classId": "%s",
                "type": "AI_INTERVIEW"
            }
            """.formatted(class_id);

        mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateAssignment_NullTitle() throws Exception {
        String jsonBody = """
            {
                "classId": "%s",
                "type": "AI_INTERVIEW"
            }
            """.formatted(class_id);

        mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateAssignment_MissingClassId() throws Exception {
        String jsonBody = """
            {
                "title": "Test",
                "type": "AI_INTERVIEW"
            }
            """;

        mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateAssignment_MissingType() throws Exception {
        String jsonBody = """
            {
                "title": "Test",
                "classId": "%s"
            }
            """.formatted(class_id);

        mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateAssignment_WithDueDate() throws Exception {
        String jsonBody = """
            {
                "title": "Mit Deadline",
                "classId": "%s",
                "type": "SELF_REFLECTION",
                "dueDate": "2025-12-31T23:59:59Z"
            }
            """.formatted(class_id);

        var result = mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].dueDate").exists())
                .andReturn();

        // Aufräumen
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        assignmentRepository.deleteById(jsonNode.get(0).get("id").asText());
    }

    // ========================================
    // updateAssignment Validierung
    // ========================================

    @Test
    public void testUpdateAssignment_EmptyTitle() throws Exception {
        // Erst Assignment erstellen
        String createBody = """
            {
                "title": "Zum Updaten",
                "classId": "%s",
                "type": "SELF_REFLECTION"
            }
            """.formatted(class_id);

        var result = mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .with(TestSecurityConfig.teacher()))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        String newAssignmentId = jsonNode.get(0).get("id").asText();

        // Update mit leerem Titel
        String updateBody = """
            {
                "title": "",
                "classId": "%s",
                "type": "SELF_REFLECTION"
            }
            """.formatted(class_id);

        mvc.perform(put("/api/assignments/" + newAssignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Aufräumen
        assignmentRepository.deleteById(newAssignmentId);
    }

    // ========================================
    // updateStatus Tests
    // ========================================

    @Test
    public void testUpdateStatus_Success() throws Exception {
        // Erst Assignment erstellen
        String createBody = """
            {
                "title": "Status Test",
                "classId": "%s",
                "type": "AI_INTERVIEW"
            }
            """.formatted(class_id);

        var result = mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .with(TestSecurityConfig.teacher()))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        String newAssignmentId = jsonNode.get(0).get("id").asText();

        // Status ändern
        String statusBody = """
            {
                "status": "CLOSED"
            }
            """;

        mvc.perform(put("/api/" + newAssignmentId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        // Aufräumen
        assignmentRepository.deleteById(newAssignmentId);
    }

    @Test
    public void testUpdateStatus_AsStudent_Forbidden() throws Exception {
        mvc.perform(put("/api/some-id/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CLOSED\"}")
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateStatus_MissingStatus() throws Exception {
        mvc.perform(put("/api/some-id/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateStatus_NotFound() throws Exception {
        String statusBody = """
            {
                "status": "CLOSED"
            }
            """;

        mvc.perform(put("/api/nonexistent-id/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateStatus_NoAuth() throws Exception {
        mvc.perform(put("/api/some-id/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CLOSED\"}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // ========================================
    // Anderer Teacher - kein Zugriff
    // ========================================

    @Test
    public void testUpdateAssignment_OtherTeacher_Forbidden() throws Exception {
        // Assignment von anderem Teacher erstellen (direkt in DB)
        var otherTeacherAssignment = assignmentRepository.save(
                ch.zhaw.praesto.model.Assignment.builder()
                        .schoolId(TestSecurityConfig.SCHOOL_ID)
                        .title("Other Teacher Assignment")
                        .classId(class_id)
                        .type(ch.zhaw.praesto.model.AssignmentType.SELF_REFLECTION)
                        .status(ch.zhaw.praesto.model.AssignmentStatus.ASSIGNED)
                        .createdByTeacherId("other-teacher-id")
                        .build()
        );

        String updateBody = """
            {
                "title": "Versuch zu ändern",
                "classId": "%s",
                "type": "SELF_REFLECTION"
            }
            """.formatted(class_id);

        mvc.perform(put("/api/assignments/" + otherTeacherAssignment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Aufräumen
        assignmentRepository.deleteById(otherTeacherAssignment.getId());
    }

    @Test
    public void testDeleteAssignment_OtherTeacher_Forbidden() throws Exception {
        // Assignment von anderem Teacher erstellen
        var otherTeacherAssignment = assignmentRepository.save(
                ch.zhaw.praesto.model.Assignment.builder()
                        .schoolId(TestSecurityConfig.SCHOOL_ID)
                        .title("Other Teacher Delete Test")
                        .classId(class_id)
                        .type(ch.zhaw.praesto.model.AssignmentType.SELF_REFLECTION)
                        .status(ch.zhaw.praesto.model.AssignmentStatus.ASSIGNED)
                        .createdByTeacherId("other-teacher-id")
                        .build()
        );

        mvc.perform(delete("/api/assignments/" + otherTeacherAssignment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Aufräumen
        assignmentRepository.deleteById(otherTeacherAssignment.getId());
    }

    @Test
    public void testGetAssignment_OtherTeacher_Forbidden() throws Exception {
        // Assignment von anderem Teacher erstellen
        var otherTeacherAssignment = assignmentRepository.save(
                ch.zhaw.praesto.model.Assignment.builder()
                        .schoolId(TestSecurityConfig.SCHOOL_ID)
                        .title("Other Teacher View Test")
                        .classId(class_id)
                        .type(ch.zhaw.praesto.model.AssignmentType.SELF_REFLECTION)
                        .status(ch.zhaw.praesto.model.AssignmentStatus.ASSIGNED)
                        .createdByTeacherId("other-teacher-id")
                        .build()
        );

        mvc.perform(get("/api/assignments/" + otherTeacherAssignment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Aufräumen
        assignmentRepository.deleteById(otherTeacherAssignment.getId());
    }

    // ========================================
    // Student kann Assignment seiner Klasse sehen
    // ========================================

    @Test
    public void testGetAssignment_AsStudentInClass_Success() throws Exception {
        // Klasse mit Test-Student (per User-Id) erstellen
        SchoolClass c = SchoolClass.builder()
                .schoolId(TestSecurityConfig.SCHOOL_ID)
                .name("StudentAssignmentTestClass")
                .teacherId(TestSecurityConfig.TEACHER_ID)
                .studentIds(new ArrayList<>())
                .build();
        c.addStudent(TestSecurityConfig.STUDENT_ID);
        SchoolClass classWithStudent = schoolClassRepository.save(c);

        // Assignment für diese Klasse erstellen
        String createBody = """
            {
                "title": "Student View Success",
                "classId": "%s",
                "type": "SELF_REFLECTION"
            }
            """.formatted(classWithStudent.getId());

        var result = mvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .with(TestSecurityConfig.teacher()))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        String studentAssignmentId = jsonNode.get(0).get("id").asText();

        // Als Student abrufen - sollte OK sein
        mvc.perform(get("/api/assignments/" + studentAssignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Student View Success"));

        // Aufräumen
        assignmentRepository.deleteById(studentAssignmentId);
    }

    @Test
    public void testGetAssignmentsForClass_AsStudent() throws Exception {
        mvc.perform(get("/api/assignments/class/" + class_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // ========================================
    // NoAuth Tests
    // ========================================

    @Test
    public void testGetAssignment_NoAuth() throws Exception {
        mvc.perform(get("/api/assignments/some-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetTeacherAssignments_NoAuth() throws Exception {
        mvc.perform(get("/api/assignments/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAssignmentsForClass_NoAuth() throws Exception {
        mvc.perform(get("/api/assignments/class/" + class_id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateAssignment_NoAuth() throws Exception {
        mvc.perform(put("/api/assignments/some-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Test\"}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteAssignment_NoAuth() throws Exception {
        mvc.perform(delete("/api/assignments/some-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

}

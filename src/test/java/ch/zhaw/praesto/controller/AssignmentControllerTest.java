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
@Import(TestSecurityConfig.class)
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
        // Test-Klasse erstellen falls nicht vorhanden
        SchoolClass testClass = schoolClassRepository.findByName("AssignmentTestClass")
                .orElseGet(() -> {
                    SchoolClass c = SchoolClass.builder()
                            .name("AssignmentTestClass")
                            .teacherId("test-user-id")
                            .studentEmails(new ArrayList<>())
                            .build();
                    return schoolClassRepository.save(c);
                });
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Bewerbungstraining"))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        assignment_id = jsonNode.get("id").asText();
    }

    @Test
    @Order(20)
    public void testGetAssignment() throws Exception {
        mvc.perform(get("/api/assignments/" + assignment_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Bewerbungstraining"));
    }

    @Test
    @Order(25)
    public void testGetTeacherAssignments() throws Exception {
        mvc.perform(get("/api/assignments/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(26)
    public void testGetAssignmentsForClass() throws Exception {
        mvc.perform(get("/api/assignments/class/" + class_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Aktualisiert"));
    }

    @Test
    @Order(40)
    public void testDeleteAssignment() throws Exception {
        mvc.perform(delete("/api/assignments/" + assignment_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        String newAssignmentId = jsonNode.get("id").asText();

        // Als Student abrufen - sollte Forbidden sein
        mvc.perform(get("/api/assignments/" + newAssignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteAssignment_NotFound() throws Exception {
        mvc.perform(delete("/api/assignments/nonexistent-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        String newAssignmentId = jsonNode.get("id").asText();

        // Als Student updaten versuchen
        mvc.perform(put("/api/assignments/" + newAssignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
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
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        String newAssignmentId = jsonNode.get("id").asText();

        // Als Student löschen versuchen
        mvc.perform(delete("/api/assignments/" + newAssignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Aufräumen
        assignmentRepository.deleteById(newAssignmentId);
    }

    @Test
    public void testGetTeacherAssignments_AsStudent_Forbidden() throws Exception {
        mvc.perform(get("/api/assignments/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}

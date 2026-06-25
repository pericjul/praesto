package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.SchoolClassDTO;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.UserRepository;
import ch.zhaw.praesto.security.TestSecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class SchoolClassControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SchoolClassRepository schoolClassRepository;

    @Autowired
    private UserRepository userRepository;

    // Mock ChatModel wie in Übung 11 beschrieben
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private ChatModel chatModel;

    /**
     * Stellt sicher, dass der Test-Schüler (Principal von TestSecurityConfig.student())
     * als echter User in der Test-Schule existiert, damit er einer Klasse zugeordnet
     * werden kann (addStudentToClass validiert den User in der DB).
     */
    @BeforeEach
    void ensureTestStudentExists() {
        if (!userRepository.existsByEmail(TestSecurityConfig.STUDENT_EMAIL)) {
            userRepository.save(User.builder()
                    .id(TestSecurityConfig.STUDENT_ID)
                    .email(TestSecurityConfig.STUDENT_EMAIL)
                    .firstName("Test")
                    .lastName("Student")
                    .role(UserRole.STUDENT)
                    .schoolId(TestSecurityConfig.SCHOOL_ID)
                    .isActive(true)
                    .build());
        }
    }

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static String class_id = "";

    private static final String TEST_CLASS_NAME = "TestKlasse3SE2";

    @Test
    @Order(10)
    public void testCreateClass() throws Exception {
        SchoolClassDTO dto = new SchoolClassDTO();
        dto.setName(TEST_CLASS_NAME);
        String jsonBody = objectMapper.writeValueAsString(dto);

        var result = mvc.perform(post("/api/classes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(TEST_CLASS_NAME))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        class_id = jsonNode.get("id").asText();
    }

    @Test
    @Order(20)
    public void testGetClass() throws Exception {
        mvc.perform(get("/api/classes/" + class_id)
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(TEST_CLASS_NAME));
    }

    @Test
    @Order(25)
    public void testGetMyClasses() throws Exception {
        mvc.perform(get("/api/classes")
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(30)
    public void testAddStudent() throws Exception {
        mvc.perform(post("/api/classes/" + class_id + "/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + TestSecurityConfig.STUDENT_ID + "\"}")
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(32)
    public void testGetMyClass_AsStudent() throws Exception {
        // Student ist in irgendeiner Klasse (könnte SubmissionTestClass sein)
        mvc.perform(get("/api/classes/my")
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void testGetMyClass_AsTeacher_Forbidden() throws Exception {
        mvc.perform(get("/api/classes/my")
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetMyClass_NoAuth_Unauthorized() throws Exception {
        mvc.perform(get("/api/classes/my")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden()); // 401, nicht 403
    }

    @Test
    @Order(35)
    public void testRemoveStudent() throws Exception {
        mvc.perform(delete("/api/classes/" + class_id + "/students/" + TestSecurityConfig.STUDENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(40)
    public void testDeleteClass() throws Exception {
        mvc.perform(delete("/api/classes/" + class_id)
                .contentType(MediaType.APPLICATION_JSON)
                .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void testCreateClass_AsStudent_Forbidden() throws Exception {
        SchoolClassDTO dto = new SchoolClassDTO();
        dto.setName("ForbiddenClass");
        String jsonBody = objectMapper.writeValueAsString(dto);

        mvc.perform(post("/api/classes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
                .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateClass_NoAuth() throws Exception {
        mvc.perform(post("/api/classes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}

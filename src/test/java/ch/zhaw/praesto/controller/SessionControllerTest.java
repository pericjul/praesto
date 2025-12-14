package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.repository.SessionRepository;
import ch.zhaw.praesto.security.TestSecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class SessionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SessionRepository sessionRepository;

    // Mock OpenAiChatModel wie in Übung 11 beschrieben
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private OpenAiChatModel chatModel;

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static String session_id = "";

    @Test
    @Order(10)
    public void testStartSession() throws Exception {
        var result = mvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        session_id = jsonNode.get("id").asText();
    }

    @Test
    @Order(20)
    public void testGetSession() throws Exception {
        mvc.perform(get("/api/sessions/" + session_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(session_id));
    }

    @Test
    @Order(25)
    public void testGetMySessions() throws Exception {
        mvc.perform(get("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(30)
    public void testSendMessage() throws Exception {
        String jsonBody = """
            {
                "message": "Hallo, ich möchte mich auf eine Stelle bewerben."
            }
            """;

        mvc.perform(post("/api/sessions/" + session_id + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(session_id));
    }

    @Test
    public void testSendMessage_AsTeacher_Forbidden() throws Exception {
        mvc.perform(post("/api/sessions/some-id/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"Test\"}")
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSendMessage_NoAuth_Forbidden() throws Exception {
        mvc.perform(post("/api/sessions/some-id/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"Test\"}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSendMessage_SessionNotFound() throws Exception {
        mvc.perform(post("/api/sessions/nonexistent-id/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"Test\"}")
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(40)
    public void testCloseSession() throws Exception {
        mvc.perform(put("/api/sessions/" + session_id + "/close")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    public void testStartSession_AsTeacher_Forbidden() throws Exception {
        mvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testStartSession_NoAuth() throws Exception {
        mvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


    @Test
    public void testCloseAndSubmitSession_AsTeacher_Forbidden() throws Exception {
        mvc.perform(put("/api/sessions/some-id/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCloseAndSubmitSession_NoAuth_Forbidden() throws Exception {
        mvc.perform(put("/api/sessions/some-id/submit")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCloseAndSubmitSession_NotFound() throws Exception {
        mvc.perform(put("/api/sessions/nonexistent-session-id/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}

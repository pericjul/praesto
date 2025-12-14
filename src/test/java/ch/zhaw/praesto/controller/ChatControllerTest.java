package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.security.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class ChatControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private OpenAiChatModel chatModel;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @BeforeEach
    void setupMockAiResponse() {
        // Mock ChatClient für /api/chat Endpoint
        when(chatClient.prompt(any(String.class))
                .user(any(String.class))
                .call()
                .content()).thenReturn("Das ist eine Test-Antwort vom KI-Coach.");
    }

    @Test
    public void testChat_AsStudent() throws Exception {
        mvc.perform(get("/api/chat")
                        .param("message", "Wie bereite ich mich auf ein Vorstellungsgespräch vor?")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testChat_AsTeacher() throws Exception {
        mvc.perform(get("/api/chat")
                        .param("message", "Was sind gute Interview-Fragen?")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.TEACHER))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testChat_NoAuth_Forbidden() throws Exception {
        mvc.perform(get("/api/chat")
                        .param("message", "Hallo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testChat_WithoutMessage_BadRequest() throws Exception {
        mvc.perform(get("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

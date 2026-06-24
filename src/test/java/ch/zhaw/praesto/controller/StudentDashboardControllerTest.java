package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.security.TestSecurityConfig;
import org.junit.jupiter.api.Test;
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
public class StudentDashboardControllerTest {

    @Autowired
    private MockMvc mvc;

    // Mock OpenAiChatModel wie in Übung 11 beschrieben
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private OpenAiChatModel chatModel;

    @Test
    public void testGetDashboard_AsStudent() throws Exception {
        mvc.perform(get("/api/student/dashboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testGetDashboard_AsTeacher_Forbidden() throws Exception {
        mvc.perform(get("/api/student/dashboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.teacher()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetDashboard_NoAuth() throws Exception {
        mvc.perform(get("/api/student/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}

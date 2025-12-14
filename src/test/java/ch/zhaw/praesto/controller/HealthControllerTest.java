package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.security.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class HealthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private OpenAiChatModel chatModel;

    @Test
    public void testHealth() throws Exception {
        mvc.perform(get("/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    public void testHealth_WithAuth() throws Exception {
        // Health sollte auch mit Auth funktionieren
        mvc.perform(get("/health")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", TestSecurityConfig.STUDENT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }
}

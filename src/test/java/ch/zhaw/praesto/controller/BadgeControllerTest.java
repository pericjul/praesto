package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.Badge;
import ch.zhaw.praesto.repository.BadgeRepository;
import ch.zhaw.praesto.security.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
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
public class BadgeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BadgeRepository badgeRepository;

    // Mock OpenAiChatModel wie in Übung 11 beschrieben
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private OpenAiChatModel chatModel;

    @BeforeEach
    void setUp() {
        // Badge für Tests sicherstellen
        if (badgeRepository.count() == 0) {
            Badge badge = Badge.builder()
                    .title("Erste Schritte")
                    .description("Erste Session abgeschlossen")
                    .icon("🎯")
                    .sortOrder(1)
                    .build();
            badgeRepository.save(badge);
        }
    }

    @Test
    public void testGetAllBadges() throws Exception {
        mvc.perform(get("/api/badges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testGetMyBadges() throws Exception {
        mvc.perform(get("/api/badges/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testGetMyBadgeCount() throws Exception {
        mvc.perform(get("/api/badges/my/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testCheckBadges() throws Exception {
        mvc.perform(post("/api/badges/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAllBadges_NoAuth() throws Exception {
        mvc.perform(get("/api/badges")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}

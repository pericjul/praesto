package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.NoteDTO;
import ch.zhaw.praesto.repository.NoteRepository;
import ch.zhaw.praesto.security.TestSecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class NoteControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private NoteRepository noteRepository;

    // Mock ChatModel wie in Übung 11 beschrieben
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private ChatModel chatModel;

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static String note_id = "";

    private static final String TEST_COMPANY = "Google";
    private static final String TEST_POSITION = "Software Engineer";
    private static final String TEST_TEXT = "Wichtige Notiz";

    @Test
    @Order(10)
    public void testCreateNote() throws Exception {
        NoteDTO dto = new NoteDTO();
        dto.setCompanyName(TEST_COMPANY);
        dto.setPosition(TEST_POSITION);
        dto.setText(TEST_TEXT);
        String jsonBody = objectMapper.writeValueAsString(dto);

        var result = mvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyName").value(TEST_COMPANY))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        note_id = jsonNode.get("id").asText();
    }

    @Test
    @Order(20)
    public void testGetNote() throws Exception {
        mvc.perform(get("/api/notes/" + note_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value(TEST_COMPANY));
    }

    @Test
    @Order(25)
    public void testGetAllNotes() throws Exception {
        mvc.perform(get("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(30)
    public void testUpdateNote() throws Exception {
        NoteDTO dto = new NoteDTO();
        dto.setCompanyName("Microsoft");
        dto.setPosition("PM");
        dto.setText("Aktualisiert");
        String jsonBody = objectMapper.writeValueAsString(dto);

        mvc.perform(put("/api/notes/" + note_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Microsoft"));
    }

    @Test
    @Order(40)
    public void testDeleteNote() throws Exception {
        mvc.perform(delete("/api/notes/" + note_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void testCreateNote_NoAuth() throws Exception {
        mvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}

package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.ApplicationDTO;
import ch.zhaw.praesto.repository.ApplicationRepository;
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
@TestMethodOrder(OrderAnnotation.class)
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApplicationRepository applicationRepository;

    // Mock OpenAiChatModel wie in Übung 11 beschrieben
    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private OpenAiChatModel chatModel;

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static String application_id = "";

    private static final String TEST_COMPANY = "Google";
    private static final String TEST_POSITION = "Software Engineer";

    @Test
    @Order(10)
    public void testCreateApplication() throws Exception {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setCompanyName(TEST_COMPANY);
        dto.setPosition(TEST_POSITION);
        dto.setStatus("APPLIED");
        String jsonBody = objectMapper.writeValueAsString(dto);

        var result = mvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyName").value(TEST_COMPANY))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        application_id = jsonNode.get("id").asText();
    }

    @Test
    @Order(20)
    public void testGetApplication() throws Exception {
        mvc.perform(get("/api/applications/" + application_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value(TEST_COMPANY));
    }

    @Test
    @Order(25)
    public void testGetAllApplications() throws Exception {
        mvc.perform(get("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(26)
    public void testGetStats() throws Exception {
        mvc.perform(get("/api/applications/stats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(30)
    public void testUpdateStatus() throws Exception {
        mvc.perform(put("/api/applications/" + application_id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVITED\"}")
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVITED"));
    }

    @Test
    @Order(40)
    public void testDeleteApplication() throws Exception {
        mvc.perform(delete("/api/applications/" + application_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(TestSecurityConfig.student()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void testCreateApplication_NoAuth() throws Exception {
        mvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}

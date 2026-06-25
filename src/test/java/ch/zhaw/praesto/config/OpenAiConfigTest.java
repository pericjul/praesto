package ch.zhaw.praesto.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAiConfig Tests")
class OpenAiConfigTest {

    @Mock
    private ChatModel chatModel;

    @InjectMocks
    private OpenAiConfig openAiConfig;

    @Test
    @DisplayName("ChatClient Bean wird erstellt")
    void chatClient_createsBean() {
        // Test dass die Methode nicht null zurückgibt
        ChatClient result = openAiConfig.chatClient();

        assertThat(result).isNotNull();
    }
}

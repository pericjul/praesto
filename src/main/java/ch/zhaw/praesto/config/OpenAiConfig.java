package ch.zhaw.praesto.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    private final OpenAiChatModel chatModel;

    public OpenAiConfig(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Bean
    public ChatClient chatClient() {
        MessageChatMemoryAdvisor chatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(MessageWindowChatMemory.builder().build())
                .build();
        return ChatClient.builder(chatModel)
                .defaultAdvisors(chatMemoryAdvisor, new SimpleLoggerAdvisor())
                .build();
    }
}

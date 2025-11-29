package ch.zhaw.praesto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.chat.client.ChatClient;

@Configuration
public class OpenAiConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        // OpenAiChatModel kommt vom Spring AI Starter
        return ChatClient.builder(chatModel).build();
    }
}
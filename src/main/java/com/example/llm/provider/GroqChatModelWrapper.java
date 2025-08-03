package com.example.llm.provider;

import com.example.llm.Config;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.time.Duration;

public class GroqChatModelWrapper extends BaseChatModelWrapper {

    public GroqChatModelWrapper(Config config, String modelName) {
        super(
                OpenAiChatModel.builder()
                        .apiKey(config.getOrThrow("groq.api.key", "Missing GROQ_API_KEY environment variable"))
                        .modelName(modelName)
                        .baseUrl("https://api.groq.com/openai/v1")
                        .timeout(Duration.ofSeconds(60))
                        .build(),
                OpenAiStreamingChatModel.builder()
                        .apiKey(config.getOrThrow("groq.api.key", "Missing GROQ_API_KEY environment variable"))
                        .modelName(modelName)
                        .baseUrl("https://api.groq.com/openai/v1")
                        .timeout(Duration.ofSeconds(60))
                        .build()
        );
    }
}

package com.example.llm.provider;

import com.example.llm.ModelConfig;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.time.Duration;

public class OpenrouterChatModelWrapper extends BaseChatModelWrapper {
    public OpenrouterChatModelWrapper(ModelConfig modelConfig, String modelName) {
        super(
                OpenAiChatModel.builder()
                        .apiKey(modelConfig.getOrThrow("openrouter.api.key", "Missing OPENROUTER_API_KEY environment variable"))
                        .modelName(modelName)
                        .baseUrl("https://openrouter.ai/api/v1")
                        .timeout(Duration.ofSeconds(60))
                        .build(),
                OpenAiStreamingChatModel.builder()
                        .apiKey(modelConfig.getOrThrow("openrouter.api.key", "Missing OPENROUTER_API_KEY environment variable"))
                        .modelName(modelName)
                        .baseUrl("https://openrouter.ai/api/v1")
                        .timeout(Duration.ofSeconds(60))
                        .build()
        );
    }
}

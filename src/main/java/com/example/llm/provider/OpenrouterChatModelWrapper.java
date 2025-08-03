package com.example.llm.provider;

import com.example.llm.Config;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.time.Duration;

public class OpenrouterChatModelWrapper extends BaseChatModelWrapper {

    public OpenrouterChatModelWrapper(Config config, String modelName) {
        super(
                OpenAiChatModel.builder()
                        .apiKey(config.getOrThrow("openrouter.api.key", "Missing OPENROUTER_API_KEY environment variable"))
                        .modelName(modelName)
                        .baseUrl("https://openrouter.ai/api/v1")
                        .timeout(Duration.ofSeconds(60))
                        .build(),
                OpenAiStreamingChatModel.builder()
                        .apiKey(config.getOrThrow("openrouter.api.key", "Missing OPENROUTER_API_KEY environment variable"))
                        .modelName(modelName)
                        .baseUrl("https://openrouter.ai/api/v1")
                        .timeout(Duration.ofSeconds(60))
                        .build()
        );
    }
}

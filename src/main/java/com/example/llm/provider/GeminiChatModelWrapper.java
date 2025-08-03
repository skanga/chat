package com.example.llm.provider;

import com.example.llm.Config;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;

import java.time.Duration;

public class GeminiChatModelWrapper extends BaseChatModelWrapper {

    public GeminiChatModelWrapper(Config config, String modelName) {
        super(
                GoogleAiGeminiChatModel.builder()
                        .apiKey(config.getOrThrow("gemini.api.key", "Missing GEMINI_API_KEY environment variable"))
                        .modelName(modelName)
                        .timeout(Duration.ofSeconds(60))
                        .build(),
                GoogleAiGeminiStreamingChatModel.builder()
                        .apiKey(config.getOrThrow("gemini.api.key", "Missing GEMINI_API_KEY environment variable"))
                        .modelName(modelName)
                        .timeout(Duration.ofSeconds(60))
                        .build()
        );
    }
}

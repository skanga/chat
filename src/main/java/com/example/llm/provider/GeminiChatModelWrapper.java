package com.example.llm.provider;

import com.example.llm.ModelConfig;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;

import java.time.Duration;

public class GeminiChatModelWrapper extends BaseChatModelWrapper {
    public GeminiChatModelWrapper(ModelConfig modelConfig, String modelName) {
        super(
                GoogleAiGeminiChatModel.builder()
                        .apiKey(modelConfig.getOrThrow("gemini.api.key", "Missing GEMINI_API_KEY environment variable"))
                        .modelName(modelName)
                        .timeout(Duration.ofSeconds(60))
                        .build(),
                GoogleAiGeminiStreamingChatModel.builder()
                        .apiKey(modelConfig.getOrThrow("gemini.api.key", "Missing GEMINI_API_KEY environment variable"))
                        .modelName(modelName)
                        .timeout(Duration.ofSeconds(60))
                        .build()
        );
    }
}

package com.example.llm.provider;

import com.example.llm.ModelConfig;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;

import java.time.Duration;

public class AnthropicChatModelWrapper extends BaseChatModelWrapper {
    public AnthropicChatModelWrapper(ModelConfig modelConfig, String modelName) {
        super(
                AnthropicChatModel.builder()
                        .apiKey(modelConfig.getOrThrow("anthropic.api.key", "Missing ANTHROPIC_API_KEY environment variable"))
                        .modelName(modelName)
                        .timeout(Duration.ofSeconds(60))
                        .build(),
                AnthropicStreamingChatModel.builder()
                        .apiKey(modelConfig.getOrThrow("anthropic.api.key", "Missing ANTHROPIC_API_KEY environment variable"))
                        .modelName(modelName)
                        .timeout(Duration.ofSeconds(60))
                        .build()
        );
    }
}

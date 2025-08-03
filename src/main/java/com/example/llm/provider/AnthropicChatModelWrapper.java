package com.example.llm.provider;

import com.example.llm.Config;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;

import java.time.Duration;

public class AnthropicChatModelWrapper extends BaseChatModelWrapper {

    public AnthropicChatModelWrapper(Config config, String modelName) {
        super(
                AnthropicChatModel.builder()
                        .apiKey(config.getOrThrow("anthropic.api.key", "Missing ANTHROPIC_API_KEY environment variable"))
                        .modelName(modelName)
                        .timeout(Duration.ofSeconds(60))
                        .build(),
                AnthropicStreamingChatModel.builder()
                        .apiKey(config.getOrThrow("anthropic.api.key", "Missing ANTHROPIC_API_KEY environment variable"))
                        .modelName(modelName)
                        .timeout(Duration.ofSeconds(60))
                        .build()
        );
    }
}

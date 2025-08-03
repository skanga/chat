package com.example.llm.provider;

import com.example.llm.Config;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.time.Duration;

public class OpenAiChatModelWrapper extends BaseChatModelWrapper {

    public OpenAiChatModelWrapper(Config config, String modelName) {
        super(
                OpenAiChatModel.builder()
                        .apiKey(config.getOrThrow("openai.api.key", "Missing OPENAI_API_KEY environment variable"))
                        .modelName(modelName)
                        .timeout(Duration.ofSeconds(60))
                        .build(),
                OpenAiStreamingChatModel.builder()
                        .apiKey(config.getOrThrow("openai.api.key", "Missing OPENAI_API_KEY environment variable"))
                        .modelName(modelName)
                        .timeout(Duration.ofSeconds(60))
                        .build()
        );
    }
}

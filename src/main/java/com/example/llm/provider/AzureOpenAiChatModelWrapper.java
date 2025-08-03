package com.example.llm.provider;

import com.example.llm.Config;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;

import java.time.Duration;

public class AzureOpenAiChatModelWrapper extends BaseChatModelWrapper {

    public AzureOpenAiChatModelWrapper(Config config, String endpoint) {
        super(
                AzureOpenAiChatModel.builder()
                        .apiKey(config.getOrThrow("azure.openai.api.key", "Missing AZURE_OPENAI_API_KEY environment variable"))
                        .endpoint(endpoint)
                        .timeout(Duration.ofSeconds(60))
                        .build(),
                AzureOpenAiStreamingChatModel.builder()
                        .apiKey(config.getOrThrow("azure.openai.api.key", "Missing AZURE_OPENAI_API_KEY environment variable"))
                        .endpoint(endpoint)
                        .timeout(Duration.ofSeconds(60))
                        .build()
        );
    }
}

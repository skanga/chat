package com.example.llm.provider;

import com.example.llm.ModelConfig;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;

import java.time.Duration;

public class AzureOpenAiChatModelWrapper extends BaseChatModelWrapper {

    public AzureOpenAiChatModelWrapper(ModelConfig modelConfig, String endPoint) {
        super(
                AzureOpenAiChatModel.builder()
                        .apiKey(modelConfig.getOrThrow("azure.openai.api.key", "Missing AZURE_OPENAI_API_KEY environment variable"))
                        .endpoint(endPoint)
                        .timeout(Duration.ofSeconds(60))
                        .build(),
                AzureOpenAiStreamingChatModel.builder()
                        .apiKey(modelConfig.getOrThrow("azure.openai.api.key", "Missing AZURE_OPENAI_API_KEY environment variable"))
                        .endpoint(endPoint)
                        .timeout(Duration.ofSeconds(60))
                        .build()
        );
    }
}

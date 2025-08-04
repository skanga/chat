package com.example.llm.provider;


import com.example.llm.ModelConfig;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;

import java.time.Duration;

public class OllamaChatModelWrapper extends BaseChatModelWrapper {
    public OllamaChatModelWrapper(ModelConfig modelConfig, String modelName) {
        super(
                OllamaChatModel.builder()
                        .baseUrl("http://localhost:11434")
                        .modelName(modelName)
                        .timeout(Duration.ofSeconds(60))
                        .build(),
                OllamaStreamingChatModel.builder()
                        .baseUrl("http://localhost:11434")
                        .modelName(modelName)
                        .timeout(Duration.ofSeconds(60))
                        .build()
        );
    }
}


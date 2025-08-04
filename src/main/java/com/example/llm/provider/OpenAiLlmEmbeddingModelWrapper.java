package com.example.llm.provider;

import com.example.llm.ModelConfig;
import com.example.llm.LlmEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

import java.util.List;

public class OpenAiLlmEmbeddingModelWrapper implements LlmEmbeddingModel {
    private final OpenAiEmbeddingModel embeddingModel;

    public OpenAiLlmEmbeddingModelWrapper(ModelConfig modelConfig, String modelName) {
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(modelConfig.getOrThrow("openai.api.key", "Missing OPENAI_API_KEY environment variable"))
                .modelName(modelName)
                .build();
    }

    @Override
    public List<Float> embed(String embedText) {
        return embeddingModel.embed(embedText).content().vectorAsList();
    }
}


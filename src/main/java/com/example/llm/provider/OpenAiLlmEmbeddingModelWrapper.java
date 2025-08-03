package com.example.llm.provider;

import com.example.llm.Config;
import com.example.llm.LlmEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

import java.util.List;

public class OpenAiLlmEmbeddingModelWrapper implements LlmEmbeddingModel {
    private final OpenAiEmbeddingModel model;

    public OpenAiLlmEmbeddingModelWrapper(Config config, String modelName) {
        this.model = OpenAiEmbeddingModel.builder()
                .apiKey(config.getOrThrow("openai.api.key", "Missing OPENAI_API_KEY environment variable"))
                .modelName(modelName)
                .build();
    }

    @Override
    public List<Float> embed(String text) {
        return model.embed(text).content().vectorAsList();
    }
}


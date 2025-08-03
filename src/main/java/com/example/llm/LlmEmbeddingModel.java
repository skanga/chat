package com.example.llm;

import java.util.List;

public interface LlmEmbeddingModel {
    List<Float> embed(String text);
}

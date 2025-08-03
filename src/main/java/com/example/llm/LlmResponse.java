package com.example.llm;

public record LlmResponse(String text, Integer promptTokens, Integer responseTokens, Integer totalTokens) {
    public LlmResponse(String text) {
        this(text, null, null, null);
    }
}

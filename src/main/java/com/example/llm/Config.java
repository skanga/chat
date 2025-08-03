package com.example.llm;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Config {

    private final Map<String, String> config = new HashMap<>();

    public Config() {
        // Load from environment variables
        config.put("anthropic.api.key", System.getenv("ANTHROPIC_API_KEY"));
        config.put("azure.openai.api.key", System.getenv("AZURE_OPENAI_API_KEY"));
        config.put("azure.openai.endpoint", System.getenv("AZURE_OPENAI_ENDPOINT"));
        config.put("gemini.api.key", System.getenv("GEMINI_API_KEY"));
        config.put("groq.api.key", System.getenv("GROQ_API_KEY"));
        config.put("huggingface.api.key", System.getenv("HF_API_KEY"));
        config.put("openai.api.key", System.getenv("OPENAI_API_KEY"));
        config.put("openrouter.api.key", System.getenv("OPENROUTER_API_KEY"));
    }

    /**
     * Constructor for testing purposes.
     */
    public Config(Map<String, String> initialConfig) {
        this.config.putAll(initialConfig);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(config.get(key));
    }

    public String getOrThrow(String key, String message) {
        return Optional.ofNullable(config.get(key)).orElseThrow(() -> new IllegalArgumentException(message));
    }
}

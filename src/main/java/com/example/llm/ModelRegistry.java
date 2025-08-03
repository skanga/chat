package com.example.llm;

import com.example.llm.provider.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class ModelRegistry {
    private final Map<String, BiFunction<Config, String, LlmChatModel>> chatModelFactories = new HashMap<>();
    private final Map<String, BiFunction<Config, String, LlmEmbeddingModel>> embeddingModelFactories = new HashMap<>();
    private final Config config;

    public ModelRegistry(Config config) {
        this.config = config;
        // Register chat model factories
        chatModelFactories.put("anthropic", AnthropicChatModelWrapper::new);
        chatModelFactories.put("azure", AzureOpenAiChatModelWrapper::new);
        chatModelFactories.put("gemini", GeminiChatModelWrapper::new);
        chatModelFactories.put("groq", GroqChatModelWrapper::new);
        chatModelFactories.put("ollama", OllamaChatModelWrapper::new);
        chatModelFactories.put("openai", OpenAiChatModelWrapper::new);
        chatModelFactories.put("openrouter", OpenrouterChatModelWrapper::new);

        // Register embedding model factories
        embeddingModelFactories.put("openai", OpenAiLlmEmbeddingModelWrapper::new);
    }

    public LlmChatModel getModel(String provider, String modelName) {
        if (!chatModelFactories.containsKey(provider)) {
            throw new IllegalArgumentException("Provider not found: " + provider);
        }
        return chatModelFactories.get(provider).apply(config, modelName);
    }

    public LlmChatModel getModel(String provider) {
        return switch (provider) {
            case "openai" -> getModel("openai", "gpt-3.5-turbo");
            case "gemini" -> getModel("gemini", "gemini-pro");
            case "groq" -> getModel("groq", "mixtral-8x7b-32768");
            case "openrouter" -> getModel("openrouter", "openai/gpt-3.5-turbo");
            default -> throw new IllegalArgumentException("Provider not found: " + provider);
        };
    }

    public LlmEmbeddingModel getEmbeddingModel(String provider, String modelName) {
        if (!embeddingModelFactories.containsKey(provider)) {
            throw new IllegalArgumentException("Embedding provider not found: " + provider);
        }
        return embeddingModelFactories.get(provider).apply(config, modelName);
    }

    public LlmEmbeddingModel getEmbeddingModel(String provider) {
        return switch (provider) {
            case "openai" -> getEmbeddingModel("openai", "text-embedding-ada-002");
            default -> throw new IllegalArgumentException("Embedding provider not found: " + provider);
        };
    }

    public void registerChatModelFactory(String provider, BiFunction<Config, String, LlmChatModel> factory) {
        chatModelFactories.put(provider, factory);
    }

    public void registerEmbeddingModelFactory(String provider, BiFunction<Config, String, LlmEmbeddingModel> factory) {
        embeddingModelFactories.put(provider, factory);
    }

    public String[] getAvailableChatProviders() {
        return chatModelFactories.keySet().toArray(new String[0]);
    }

    public Config getConfig() {
        return config;
    }
}

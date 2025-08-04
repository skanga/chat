package com.example.llm;

import com.example.llm.provider.OllamaChatModelWrapper;
import com.example.llm.provider.OpenAiChatModelWrapper;
import com.example.llm.provider.OpenAiLlmEmbeddingModelWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelRegistryTest {

    private ModelRegistry modelRegistry;
    private ModelConfig modelConfig;

    @BeforeEach
    void setUp() {
        // Provide dummy keys for testing to avoid exceptions
        modelConfig = new ModelConfig(Map.of(
                "openai.api.key", "dummy-key",
                "anthropic.api.key", "dummy-key",
                "azure.openai.api.key", "dummy-key",
                "azure.openai.endpoint", "dummy-endpoint",
                "gemini.api.key", "dummy-key",
                "groq.api.key", "dummy-key",
                "huggingface.api.key", "dummy-key",
                "openrouter.api.key", "dummy-key"
        ));
        modelRegistry = new ModelRegistry(modelConfig);
    }

    @Test
    void shouldGetChatModelWithProviderAndModelName() {
        LlmChatModel model = modelRegistry.getModel("openai", "gpt-4o");
        assertThat(model).isInstanceOf(OpenAiChatModelWrapper.class);
    }

    @Test
    void shouldGetDefaultChatModelWithProvider() {
        LlmChatModel model = modelRegistry.getModel("openai");
        assertThat(model).isInstanceOf(OpenAiChatModelWrapper.class);
    }

    @Test
    void shouldGetOllamaModelWithoutApiKey() {
        // Ollama does not require an API key
        ModelConfig emptyModelConfig = new ModelConfig(Map.of());
        ModelRegistry registry = new ModelRegistry(emptyModelConfig);
        LlmChatModel model = registry.getModel("ollama", "llama2");
        assertThat(model).isInstanceOf(OllamaChatModelWrapper.class);
    }

    @Test
    void shouldThrowForUnknownChatProvider() {
        assertThatThrownBy(() -> modelRegistry.getModel("unknown", "model"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Provider not found: unknown");
    }

    @Test
    void shouldGetEmbeddingModelWithProviderAndModelName() {
        LlmEmbeddingModel model = modelRegistry.getEmbeddingModel("openai", "text-embedding-ada-002");
        assertThat(model).isInstanceOf(OpenAiLlmEmbeddingModelWrapper.class);
    }

    @Test
    void shouldGetDefaultEmbeddingModelWithProvider() {
        LlmEmbeddingModel model = modelRegistry.getEmbeddingModel("openai");
        assertThat(model).isInstanceOf(OpenAiLlmEmbeddingModelWrapper.class);
    }

    @Test
    void shouldThrowForUnknownEmbeddingProvider() {
        assertThatThrownBy(() -> modelRegistry.getEmbeddingModel("unknown", "model"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Embedding provider not found: unknown");
    }
}

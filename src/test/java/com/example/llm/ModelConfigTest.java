package com.example.llm;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelConfigTest {

    @Test
    void shouldReturnEmptyOptionalForMissingKey() {
        ModelConfig modelConfig = new ModelConfig(Map.of());
        assertThat(modelConfig.get("nonexistent.key")).isEmpty();
    }

    @Test
    void shouldThrowForMissingKeyWhenUsingGetOrThrow() {
        ModelConfig modelConfig = new ModelConfig(Map.of());
        assertThatThrownBy(() -> modelConfig.getOrThrow("nonexistent.key", "Test message"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test message");
    }

    @Test
    void shouldReturnValueWhenPresent() {
        ModelConfig modelConfig = new ModelConfig(Map.of("test.key", "test.value"));
        assertThat(modelConfig.get("test.key")).hasValue("test.value");
    }

    @Test
    void shouldReturnValueWhenPresentUsingGetOrThrow() {
        ModelConfig modelConfig = new ModelConfig(Map.of("test.key", "test.value"));
        assertThat(modelConfig.getOrThrow("test.key", "message")).isEqualTo("test.value");
    }
}

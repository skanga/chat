package com.example.llm;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigTest {

    @Test
    void shouldReturnEmptyOptionalForMissingKey() {
        Config config = new Config(Map.of());
        assertThat(config.get("nonexistent.key")).isEmpty();
    }

    @Test
    void shouldThrowForMissingKeyWhenUsingGetOrThrow() {
        Config config = new Config(Map.of());
        assertThatThrownBy(() -> config.getOrThrow("nonexistent.key", "Test message"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test message");
    }

    @Test
    void shouldReturnValueWhenPresent() {
        Config config = new Config(Map.of("test.key", "test.value"));
        assertThat(config.get("test.key")).hasValue("test.value");
    }

    @Test
    void shouldReturnValueWhenPresentUsingGetOrThrow() {
        Config config = new Config(Map.of("test.key", "test.value"));
        assertThat(config.getOrThrow("test.key", "message")).isEqualTo("test.value");
    }
}

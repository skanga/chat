package com.example.llm;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ConversationTest {

    @Test
    void shouldCreateConversationWithAllFields() {
        Conversation conv = new Conversation();
        
        conv.setId(12345L);
        conv.setTimestamp(Instant.parse("2023-01-01T12:00:00Z"));
        conv.setModel("gpt-4");
        conv.setPrompt("What is Java?");
        conv.setResponse("Java is a programming language...");
        conv.setPromptTokens(10);
        conv.setResponseTokens(50);
        conv.setTotalTokens(60);
        conv.setDurationMs(1500L);
        conv.setSchema("{\"type\":\"object\"}");
        conv.setTools(List.of("tool1", "tool2"));
        conv.setMetadata(Map.of("user", "test-user", "session", "abc123"));
        
        assertThat(conv.getId()).isEqualTo(12345L);
        assertThat(conv.getTimestamp()).isEqualTo(Instant.parse("2023-01-01T12:00:00Z"));
        assertThat(conv.getModel()).isEqualTo("gpt-4");
        assertThat(conv.getPrompt()).isEqualTo("What is Java?");
        assertThat(conv.getResponse()).isEqualTo("Java is a programming language...");
        assertThat(conv.getPromptTokens()).isEqualTo(10);
        assertThat(conv.getResponseTokens()).isEqualTo(50);
        assertThat(conv.getTotalTokens()).isEqualTo(60);
        assertThat(conv.getDurationMs()).isEqualTo(1500L);
        assertThat(conv.getSchema()).isEqualTo("{\"type\":\"object\"}");
        assertThat(conv.getTools()).hasSize(2);
        assertThat(conv.getMetadata()).hasSize(2);
    }

    @Test
    void shouldHandleNullValues() {
        Conversation conv = new Conversation();
        
        conv.setId(1L);
        conv.setTimestamp(Instant.now());
        conv.setModel("test-model");
        conv.setPrompt("test prompt");
        conv.setResponse("test response");
        // Don't set optional fields
        
        assertThat(conv.getPromptTokens()).isNull();
        assertThat(conv.getResponseTokens()).isNull();
        assertThat(conv.getTotalTokens()).isNull();
        assertThat(conv.getDurationMs()).isNull();
        assertThat(conv.getSchema()).isNull();
        assertThat(conv.getTools()).isNull();
        assertThat(conv.getMetadata()).isNull();
    }

    @Test
    void shouldHaveReasonableToString() {
        Conversation conv = new Conversation();
        conv.setId(123L);
        conv.setTimestamp(Instant.parse("2023-01-01T12:00:00Z"));
        conv.setModel("gpt-3.5-turbo");
        conv.setPrompt("This is a very long prompt that should be truncated in the toString method");
        conv.setResponse("This is also a very long response that should be truncated in the toString method");
        conv.setPromptTokens(100);
        conv.setResponseTokens(200);
        conv.setTotalTokens(300);
        conv.setDurationMs(2500L);
        
        String toString = conv.toString();
        
        assertThat(toString).contains("Conversation{");
        assertThat(toString).contains("id=123");
        assertThat(toString).contains("gpt-3.5-turbo");
        assertThat(toString).contains("prompt='This is a very long prompt");
        assertThat(toString).contains("response='This is also a very long response");
        assertThat(toString).contains("promptTokens=100");
        assertThat(toString).contains("responseTokens=200");
        assertThat(toString).contains("totalTokens=300");
        assertThat(toString).contains("durationMs=2500");
    }

    @Test
    void shouldAllowChainingSetters() {
        Conversation conv = new Conversation();
        
        // Test that setters return void (not chainable)
        conv.setId(1L);
        conv.setModel("test");
        conv.setPrompt("prompt");
        conv.setResponse("response");
        
        assertThat(conv.getId()).isEqualTo(1L);
        assertThat(conv.getModel()).isEqualTo("test");
        assertThat(conv.getPrompt()).isEqualTo("prompt");
        assertThat(conv.getResponse()).isEqualTo("response");
    }

    @Test
    void shouldHandleEmptyCollections() {
        Conversation conv = new Conversation();
        conv.setTools(List.of());
        conv.setMetadata(Map.of());
        
        assertThat(conv.getTools()).isEmpty();
        assertThat(conv.getMetadata()).isEmpty();
    }
}
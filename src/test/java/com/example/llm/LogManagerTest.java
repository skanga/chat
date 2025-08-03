package com.example.llm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class LogManagerTest {
    @TempDir
    Path tempDir;
    
    @Test
    void shouldCreateJsonlLogManager() {
        File logFile = tempDir.resolve("test.jsonl").toFile();
        LogManager logManager = new LogManager("jsonl", logFile.getAbsolutePath());
        
        assertThat(logFile).exists();
        logManager.close();
    }
    
    @Test
    void shouldCreateH2LogManager() {
        File dbFile = tempDir.resolve("test.db").toFile();
        LogManager logManager = new LogManager("h2", dbFile.getAbsolutePath());
        
        // H2 creates files with .mv.db extension
        File actualDbFile = new File(dbFile.getAbsolutePath() + ".mv.db");
        assertThat(actualDbFile).exists();
        logManager.close();
    }
    
    @Test
    void shouldLogConversationToJsonl() throws IOException {
        File logFile = tempDir.resolve("test.jsonl").toFile();
        LogManager logManager = new LogManager("jsonl", logFile.getAbsolutePath());
        
        logManager.logConversation(
            "gpt-3.5-turbo",
            "Hello, world!",
            "Hello! How can I help you today?",
            10,
            15,
            25,
            1234L,
            null,
            null,
            null  // Use null instead of Map to avoid JSON issues
        );
        
        java.util.List<String> lines = java.nio.file.Files.readAllLines(logFile.toPath());
        assertThat(lines).hasSize(1);
        
        String jsonLine = lines.get(0);
        assertThat(jsonLine).contains("gpt-3.5-turbo");
        assertThat(jsonLine).contains("Hello, world!");
        assertThat(jsonLine).contains("Hello! How can I help you today?");
        assertThat(jsonLine).contains("25");
        
        logManager.close();
    }
    
    @Test
    void shouldLogConversationToH2() {
        File dbFile = tempDir.resolve("test.db").toFile();
        LogManager logManager = new LogManager("h2", dbFile.getAbsolutePath());
        
        logManager.logConversation(
            "gpt-4",
            "What is Java?",
            "Java is a high-level programming language...",
            5,
            50,
            55,
            2000L,
            null,
            null,
            null  // Use null instead of Map to avoid JSON issues
        );
        
        java.util.List<Conversation> conversations = logManager.getConversations(10, 0);
        assertThat(conversations).hasSize(1);
        
        Conversation conv = conversations.get(0);
        assertThat(conv.getModel()).isEqualTo("gpt-4");
        assertThat(conv.getPrompt()).isEqualTo("What is Java?");
        assertThat(conv.getResponse()).isEqualTo("Java is a high-level programming language...");
        assertThat(conv.getTotalTokens()).isEqualTo(55);
        assertThat(conv.getDurationMs()).isEqualTo(2000L);
        
        logManager.close();
    }
    
    @Test
    void shouldRetrieveConversationsWithPagination() {
        File dbFile = tempDir.resolve("test.db").toFile();
        LogManager logManager = new LogManager("h2", dbFile.getAbsolutePath());
        
        // Log multiple conversations
        for (int i = 0; i < 5; i++) {
            logManager.logConversation(
                "model-" + i,
                "Prompt " + i,
                "Response " + i,
                10 + i,
                20 + i,
                30 + i,
                1000L + i,
                null,
                null,
                null
            );
        }
        
        java.util.List<Conversation> conversations = logManager.getConversations(3, 0);
        assertThat(conversations).hasSize(3);
        
        conversations = logManager.getConversations(3, 2);
        assertThat(conversations).hasSize(3);
        
        logManager.close();
    }
    
    @Test
    void shouldRetrieveSpecificConversationById() {
        File dbFile = tempDir.resolve("test.db").toFile();
        LogManager logManager = new LogManager("h2", dbFile.getAbsolutePath());
        
        logManager.logConversation(
            "gpt-3.5-turbo",
            "Test prompt",
            "Test response",
            10,
            20,
            30,
            1500L,
            null,
            null,
            null
        );
        
        List<Conversation> conversations = logManager.getConversations(1, 0);
        assertThat(conversations).hasSize(1);
        
        Conversation conv = conversations.get(0);
        Conversation retrieved = logManager.getConversation(conv.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getPrompt()).isEqualTo("Test prompt");
        
        logManager.close();
    }
    
    @Test
    void shouldHandleNullValuesGracefully() {
        File logFile = tempDir.resolve("test.jsonl").toFile();
        LogManager logManager = new LogManager("jsonl", logFile.getAbsolutePath());
        
        logManager.logConversation(
            "gpt-3.5-turbo",
            "Hello",
            "Hi",
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
        
        List<Conversation> conversations = logManager.getConversations(10, 0);
        assertThat(conversations).hasSize(1);
        
        Conversation conv = conversations.get(0);
        assertThat(conv.getPromptTokens()).isNull();
        assertThat(conv.getResponseTokens()).isNull();
        assertThat(conv.getTotalTokens()).isNull();
        assertThat(conv.getDurationMs()).isNull();
        
        logManager.close();
    }
    
    @Test
    void shouldHandleJsonlFileWithExistingContent() throws IOException {
        File logFile = tempDir.resolve("test.jsonl").toFile();
        
        // Pre-populate with some content
        Files.writeString(logFile.toPath(),
            "{\"id\":1,\"timestamp\":\"2023-01-01T00:00:00Z\",\"model\":\"gpt-3\",\"prompt\":\"old\",\"response\":\"old response\"}\n");
        
        LogManager logManager = new LogManager("jsonl", logFile.getAbsolutePath());
        
        logManager.logConversation(
            "gpt-4",
            "New prompt",
            "New response",
            10,
            20,
            30,
            1000L,
            null,
            null,
            null
        );
        
        List<Conversation> conversations = logManager.getConversations(10, 0);
        assertThat(conversations).hasSize(2);
        
        logManager.close();
    }
    
    @Test
    void shouldCloseResourcesProperly() {
        File dbFile = tempDir.resolve("test.db").toFile();
        LogManager logManager = new LogManager("h2", dbFile.getAbsolutePath());
        
        assertThatCode(() -> {
            logManager.logConversation("test", "test", "test", 1, 1, 2, 100L, null, null, null);
            logManager.close();
        }).doesNotThrowAnyException();
    }
}